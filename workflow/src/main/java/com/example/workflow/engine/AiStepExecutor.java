package com.example.workflow.engine;
import com.example.workflow.Repository.WorkflowRepository;
// Spring & Core Logic
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Data Handling
import org.springframework.context.annotation.Lazy;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.time.Duration;
import com.fasterxml.jackson.databind.JsonNode;
// JSON Processing (Jackson)

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;


// Your Internal Project Classes
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.workflow.service.GeminiService;
import com.example.workflow.model.WorkflowStep;
import com.example.workflow.model.WorkflowRun;
import com.example.workflow.service.WorkflowExecutionService;
import com.example.workflow.Repository.StepRunRepository;
import com.example.workflow.Repository.WorkflowRunRepository;
import com.example.workflow.Repository.WorkflowStepRepository;
import com.example.workflow.model.StepRun;
import com.example.workflow.model.WorkflowEntity;
import com.example.workflow.model.StepRunStatus;
import com.example.workflow.model.WorkflowRunStatus;
@Slf4j
@Component
@RequiredArgsConstructor
public class AiStepExecutor implements StepExecutor {
    private final WorkflowRepository workflowRepository;
    private final ObjectMapper objectMapper;
    private final WorkflowStepRepository workflowStepRepository;
    @Lazy
    private  WorkflowExecutionService workflowExecutionService;
    private final StepRunRepository stepRunRepository;
    private final WorkflowRunRepository workflowRunRepository;
    private final GeminiService geminiService;
    private final StringRedisTemplate redisTemplate;
    
    @Override
    @Transactional
    public void executeStepRun(StepRun stepRun) {
        log.info("Ai step executor started for {}",stepRun.getCurrentworkflowStep().getStepkey());
        String key="ai-service"+stepRun.getId().toString();
        Boolean acquired=redisTemplate.opsForValue().setIfAbsent(key,"IN_PROGRESS",Duration.ofMinutes(5));
        if(Boolean.FALSE.equals(acquired)){
            log.warn("StepRun {} is already being processed by another worker instance. Skipping.", stepRun.getId());
            return;
        }
        try{
    
        StringBuilder prompt=new StringBuilder();
        
        WorkflowStep current_step=stepRun.getCurrentworkflowStep();
        WorkflowRun workflowRun=stepRun.getWorkflowRun();
        String contextJson=workflowRun.getContextJson();
        JsonNode context=objectMapper.readTree(contextJson);
        List<String>depands=current_step.getDepands_on();
        JsonNode config=objectMapper.readTree(current_step.getConfigJson());
        prompt.append(config.path("prompt").asText());
        for(String current:depands){
            log.info("Entered  loop for stepRun {}",stepRun.getCurrentworkflowStep().getStepkey());
          String output=context.path(current).asText("");
          if(output.isEmpty()){
              log.info("Output is empty no such {} is present in contextJson",current);
            WorkflowStep work=workflowStepRepository.findByWorkflowAndStepkey(workflowRun.getWorkflow(), current);
            if(work==null){
                log.info("No such workflow dtep {} present",current);
                stepRun.setStatus(StepRunStatus.FAILED);
                workflowRun.setStatus(WorkflowRunStatus.FAILED);
            }
            else{
                workflowRun.getActive_List().add(current_step.getStepkey());
                workflowRun.setCurrentstep(work);
                workflowRunRepository.save(workflowRun);
                 log.info(" Before Executing workflowStep {} for TriggerSource {}",current,workflowRun.getTriggerSource());
                workflowExecutionService.singleStep(workflowRun);
                log.info("Executing workflowStep {} for TriggerSource {}",current,workflowRun.getTriggerSource());
                workflowRun=workflowRunRepository.findById(workflowRun.getId()).orElseThrow(()->new RuntimeException("No such workflowRun present and trigger failed"));
                contextJson=workflowRun.getContextJson();
                context=objectMapper.readTree(contextJson);
                workflowRun.setCurrentstep(work);
                workflowRunRepository.save(workflowRun);
                 prompt.append(context.path(current).asText(""));
                    log.info("Completed {} adding to the body for ai",current);
            }
          }
          else{
              prompt.append(output).append("\n");
              log.info("Completed {} adding to the body for ai",current);
          }
        }
        
      String output=  geminiService.use(prompt.toString());
      stepRun.setOutputJson(output);
      stepRun.setStatus(StepRunStatus.SUCCESS);
      stepRunRepository.save(stepRun);
        
        }catch(Exception e){
            log.error(e.getMessage());
            stepRun.setStatus(StepRunStatus.RETRY_PENDING);
            stepRunRepository.save(stepRun);
        }
    }
}
