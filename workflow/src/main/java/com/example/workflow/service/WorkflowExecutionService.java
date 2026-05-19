package com.example.workflow.service;

import com.example.workflow.Repository.*;
import com.example.workflow.engine.StepExecutorRegistry;
import com.example.workflow.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class WorkflowExecutionService {
    private final StepRunRepository stepRunRepository;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final StepExecutorRegistry stepExecutorRegistry;
    private final WorkflowRunRepository workflowRunRepository;

    @Async
    @Transactional
    public void executeSteps(WorkflowRun runParam) {
        log.info("Started executeSteps function");
        // Safe reload inside the background thread transaction context window
        WorkflowRun workflowRun = workflowRunRepository.findById(runParam.getId())
                .orElseThrow(() -> new RuntimeException("Workflow Run not found: " + runParam.getId()));

        WorkflowStep workflowStep = workflowRun.getCurrentstep();

        // 1. WORKFLOW FINISHED (Terminal Check)
        if (workflowStep == null) {
            workflowRun.setStatus(WorkflowRunStatus.SUCCESS);
            workflowRun.setFinishedAt(LocalDateTime.now());
            workflowRunRepository.save(workflowRun);
            log.info("[WORKFLOW TRACE] [FINISHED] Run ID: {}, Status: SUCCESS", workflowRun.getId());
            return;
        }

        // Initialize or Fetch the StepRun record execution log row
        StepRun stepRun = stepRunRepository.findByWorkflowRunAndCurrentworkflowStep(workflowRun, workflowStep)
                .orElseGet(() -> {
                    StepRun newRun = new StepRun();
                    newRun.setWorkflowRun(workflowRun);
                    newRun.setCurrentworkflowStep(workflowStep);
                    newRun.setInputJson(workflowRun.getTriggerPayload());
                    newRun.setStatus(StepRunStatus.RUNNING);
                    return stepRunRepository.save(newRun);
                });

        // 2. DEPENDENCY WAITING / LOOP PROTECTION
        if (workflowRun.getActive_List().contains(workflowStep.getStepkey())) {
            log.error("[WORKFLOW TRACE] [LOOP_DETECTED] Step Key: {} already executed. Aborting.", workflowStep.getStepkey());
            workflowRun.setStatus(WorkflowRunStatus.FAILED);
            stepRun.setStatus(StepRunStatus.FAILED);
            workflowRunRepository.save(workflowRun);
            stepRunRepository.save(stepRun);
            return;
        }

        // Max Retry Violation Guard
        if (stepRun.getAttemptCount() > workflowStep.getAttemptCount()) {
            log.error("[WORKFLOW TRACE] [RETRY_FAILED] Step Run ID: {} exhausted max attempts.", stepRun.getId());
            workflowRun.setStatus(WorkflowRunStatus.FAILED);
            workflowRun.setLastError("Reached Maximum number of failed Attempts");
            stepRun.setStatus(StepRunStatus.FAILED);
            workflowRunRepository.save(workflowRun);
            stepRunRepository.save(stepRun);
            return;
        }

        // 3. DISTRIBUTED SAFETY LOCKING
        String key = "executor-lock:" + stepRun.getId();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "LOCKED", Duration.ofMinutes(5));
        if (Boolean.FALSE.equals(acquired)) {
            log.warn("[WORKFLOW TRACE] [SKIPPED] StepRun {} is locked by another worker.", stepRun.getId());
            return;
        }

        log.info("[WORKFLOW TRACE] [STARTED] Run ID: {}, Workflow: {}, Step: {}", 
                 workflowRun.getId(), workflowRun.getWorkflow().getWorkflowName(), workflowStep.getStepkey());

        try {
            workflowRun.setStatus(WorkflowRunStatus.RUNNING);
            workflowRunRepository.save(workflowRun);

            // 4. EXTERNAL ACTION EXECUTED
            var executor = stepExecutorRegistry.getExecutor(workflowStep.getStepType());
            executor.executeStepRun(stepRun);

            // Case A: Step Explicitly Flagged Failure
            if (stepRun.getStatus() == StepRunStatus.FAILED) {
                log.error("[WORKFLOW TRACE] [STEP_FAILED] Step Key: {} failed execution.", workflowStep.getStepkey());
                workflowRun.setStatus(WorkflowRunStatus.FAILED);
                workflowRun.setLastError(stepRun.getErrorMessage());
                workflowRunRepository.save(workflowRun);
                return;
            }

            // Case B: Step Requested Retry Scheduled
            if (stepRun.getStatus() == StepRunStatus.RETRY_PENDING) {
                stepRun.setAttemptCount(stepRun.getAttemptCount() + 1);
                stepRun.setNextExecutionAt(LocalDateTime.now().plusMinutes(5));
                workflowRun.setStatus(WorkflowRunStatus.RETRY_PENDING);
                
                stepRunRepository.save(stepRun);
                workflowRunRepository.save(workflowRun);
                log.warn("[WORKFLOW TRACE] [RETRY_SCHEDULED] Step Key: {} scheduled for retry.", workflowStep.getStepkey());
                return;
            }

            // Case C: Step Processing Completed Successfully
            if (stepRun.getStatus() == StepRunStatus.SUCCESS) {
                log.info("[WORKFLOW TRACE] [STEP_COMPLETED] Step Key: {} finished with status: SUCCESS", workflowStep.getStepkey());
                
                String newContextJson = updateConfigJson(workflowRun.getContextJson(), workflowStep.getStepkey(), stepRun.getOutputJson());
                workflowRun.setContextJson(newContextJson);
                workflowRun.getActive_List().add(workflowStep.getStepkey());

                // Calculate Next Step Linkage
                if (workflowStep.getNext_step() == null && workflowStep.getConditional_true() != null) {
                    Boolean result = stepRun.getBooleanResult();
                    WorkflowStep nextStep = (result != null && result) ? workflowStep.getConditional_true() : workflowStep.getConditional_false();
                    workflowRun.setCurrentstep(nextStep);
                } else if (workflowStep.getNext_step() != null) {
                    workflowRun.setCurrentstep(workflowStep.getNext_step());
                } else {
                    workflowRun.setStatus(WorkflowRunStatus.SUCCESS);
                    workflowRun.setFinishedAt(LocalDateTime.now());
                }

                workflowRunRepository.save(workflowRun);
                stepRunRepository.save(stepRun);

                // CASCADE TO NEXT STEP LINK: If there is another step to run, trigger a fresh thread clean!
                if (workflowRun.getCurrentstep() != null && workflowRun.getStatus() == WorkflowRunStatus.RUNNING) {
                    // Self-invocation proxy pointer allows Spring to intercept the @Async context cleanly
                    // For scratch builds, calling your endpoint internally or via an appEvent is best, 
                    //
                    log.info("[WORKFLOW TRACE] [STEP_ENTERED] Handoff to next step chain link.");
                     executeSteps(runParam);
     
                } else {
                    log.info("[WORKFLOW TRACE] [FINISHED] Run ID: {}, Final Status: {}", workflowRun.getId(), workflowRun.getStatus());
                }
            }
        } catch (Exception e) {
            log.error("Execution failed critically: {}", e.getMessage(), e);
            workflowRun.setStatus(WorkflowRunStatus.RETRY_PENDING);
            stepRun.setStatus(StepRunStatus.RETRY_PENDING);
            stepRun.setAttemptCount(stepRun.getAttemptCount() + 1);
            stepRun.setNextExecutionAt(LocalDateTime.now().plusMinutes(5));
            workflowRunRepository.save(workflowRun);
            stepRunRepository.save(stepRun);
        } finally {
            redisTemplate.delete(key); // Always release the lock
        }
    }

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void releaseStepRun() {
        List<StepRun> remaining = stepRunRepository.findByNextExecutionAtBeforeAndStatus(LocalDateTime.now(), StepRunStatus.RETRY_PENDING);
        for (StepRun sr : remaining) {
            // Let the scheduled cron worker pick up retry executions cleanly
            executeSteps(sr.getWorkflowRun());
        }
    }

    public String updateConfigJson(String currentValue, String StepKey, String output) {
        try {
            ObjectNode out = (currentValue == null || currentValue.isEmpty()) 
                ? objectMapper.createObjectNode() 
                : (ObjectNode) objectMapper.readTree(currentValue);
            
            try {
                JsonNode config = objectMapper.readTree(output);
                out.set(StepKey, config);
            } catch (Exception e) {
                out.put(StepKey, output);
            }
            return out.toString();
        } catch (Exception e) {
            log.error("Vault Update Failed: {}", e.getMessage());
            return currentValue;
        }
    }
    public void singleStep(WorkflowRun workflowRun){
        WorkflowStep currentStep=workflowRun.getCurrentstep();
        if(currentStep==null){
            log.info("No workflow present");
            return;
        }
             StepRun stepRun = stepRunRepository.findByWorkflowRunAndCurrentworkflowStep(workflowRun, currentStep)
                .orElseGet(() -> {
                    StepRun newRun = new StepRun();
                    newRun.setWorkflowRun(workflowRun);
                    newRun.setCurrentworkflowStep(currentStep);
                    newRun.setInputJson(workflowRun.getTriggerPayload());
                    newRun.setStatus(StepRunStatus.RUNNING);
                    return stepRunRepository.save(newRun);
                });
                 if (stepRun.getAttemptCount() > currentStep.getAttemptCount()) {
            log.error("[WORKFLOW TRACE] [RETRY_FAILED] Step Run ID: {} exhausted max attempts.", stepRun.getId());
            workflowRun.setStatus(WorkflowRunStatus.FAILED);
            workflowRun.setLastError("Reached Maximum number of failed Attempts");
            stepRun.setStatus(StepRunStatus.FAILED);
            workflowRunRepository.save(workflowRun);
            stepRunRepository.save(stepRun);
            return;
        }
        log.info("[WORKFLOW TRACE] [STARTED] Run ID: {}, Workflow: {}, Step: {}", 
                 workflowRun.getId(), workflowRun.getWorkflow().getWorkflowName(), currentStep.getStepkey());
        try{ 
                       workflowRun.setStatus(WorkflowRunStatus.RUNNING);
            workflowRunRepository.save(workflowRun);

            // 4. EXTERNAL ACTION EXECUTED
            var executor = stepExecutorRegistry.getExecutor(currentStep.getStepType());
            executor.executeStepRun(stepRun);

            // Case A: Step Explicitly Flagged Failure
            if (stepRun.getStatus() == StepRunStatus.FAILED) {
                log.error("[WORKFLOW TRACE] [STEP_FAILED] Step Key: {} failed execution.", currentStep.getStepkey());
                workflowRun.setStatus(WorkflowRunStatus.FAILED);
                workflowRun.setLastError(stepRun.getErrorMessage());
                workflowRunRepository.save(workflowRun);
                return;
            }

            // Case B: Step Requested Retry Scheduled
            if (stepRun.getStatus() == StepRunStatus.RETRY_PENDING) {
                stepRun.setAttemptCount(stepRun.getAttemptCount() + 1);
                stepRun.setNextExecutionAt(LocalDateTime.now().plusMinutes(5));
                workflowRun.setStatus(WorkflowRunStatus.RETRY_PENDING);
                
                stepRunRepository.save(stepRun);
                workflowRunRepository.save(workflowRun);
                log.warn("[WORKFLOW TRACE] [RETRY_SCHEDULED] Step Key: {} scheduled for retry.", currentStep.getStepkey());
                return;
            }
             if (stepRun.getStatus() == StepRunStatus.SUCCESS) {
                log.info("[WORKFLOW TRACE] [STEP_COMPLETED] Step Key: {} finished with status: SUCCESS", currentStep.getStepkey());
                
                String newContextJson = updateConfigJson(workflowRun.getContextJson(), currentStep.getStepkey(), stepRun.getOutputJson());
                workflowRun.setContextJson(newContextJson);
                workflowRun.getActive_List().add(currentStep.getStepkey());
                    workflowRun.setStatus(WorkflowRunStatus.SUCCESS);
                    workflowRun.setFinishedAt(LocalDateTime.now());
                

                workflowRunRepository.save(workflowRun);
                stepRunRepository.save(stepRun);
             }
            
        }catch(Exception e){
               log.error("Execution failed critically: {}", e.getMessage(), e);
            workflowRun.setStatus(WorkflowRunStatus.RETRY_PENDING);
            stepRun.setStatus(StepRunStatus.RETRY_PENDING);
            stepRun.setAttemptCount(stepRun.getAttemptCount() + 1);
            stepRun.setNextExecutionAt(LocalDateTime.now().plusMinutes(5));
            workflowRunRepository.save(workflowRun);
            stepRunRepository.save(stepRun);
        }
        
    }
}