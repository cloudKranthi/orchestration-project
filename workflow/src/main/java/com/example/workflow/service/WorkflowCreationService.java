package com.example.workflow.service;

import javax.management.RuntimeErrorException;

import com.example.workflow.model.StepRunStatus;
import com.example.workflow.model.StepType;
import com.example.workflow.Repository.WorkflowStepRepository;
import com.example.workflow.exception.BusinessException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.example.workflow.Repository.UserRepository;
import com.example.workflow.Repository.WorkflowRepository;
import com.example.workflow.Repository.WorkflowRunRepository;
import com.example.workflow.model.UserEntity;
import com.example.workflow.model.WorkflowEntity;
import com.example.workflow.model.WorkflowRun;
import com.example.workflow.model.WorkflowRunStatus;
import com.example.workflow.model.WorkflowStep;
import com.example.workflow.model.WorkflowStepStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowCreationService {
    private final UserRepository userRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowRunRepository workflowRunRepository;

    @Transactional
    public WorkflowEntity createWorkflow(String email, String name, String Description) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No such user found"));
        WorkflowEntity workflow = new WorkflowEntity();
        workflow.setUser(user);
        workflow.setWorkflowName(name);
        workflow.setDescription(Description);
        
        return workflowRepository.save(workflow);
    }

    @Transactional
    public WorkflowStep createWorkflowStep(String workflowname, String on_true_step_key, String on_false_step_key, String stepKey,
    Boolean isCritical, String config_json, Integer timeoutSeconds, Integer attemptCount, Integer stepOrder, List<String> depands_on, StepType stepType) {
        
        String cleanWorkflowName = (workflowname != null) ? workflowname.trim().replace("\r", "") : "";
        
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserEntity) ? ((UserEntity) principal).getEmail() : SecurityContextHolder.getContext().getAuthentication().getName();

        WorkflowEntity workflow = workflowRepository.findByWorkflowNameAndUser_Email(cleanWorkflowName, email);
        if (workflow == null) {
            throw new RuntimeException("Workflow not found for user");
        }
        
        if (stepKey == null) throw new RuntimeException("Step Key cant be null ");
        if (config_json == null) throw new RuntimeException("Config json cant be null");

        isCritical = isCritical != null ? isCritical : true;
        attemptCount = (attemptCount != null && attemptCount > 0) ? attemptCount : 5;
        
        WorkflowStep workflowStep = new WorkflowStep();
        workflowStep.setStepkey(stepKey.trim().replace("\r", ""));
        workflowStep.setStepOrder(stepOrder);
        workflowStep.setConfigJson(config_json);
        workflowStep.setWorkflow(workflow); 
        workflowStep.setTimeoutSeconds(timeoutSeconds);
        workflowStep.setIsCritical(isCritical);
        workflowStep.setStepType(stepType);
        
        if (depands_on != null) {
            workflowStep.getDepands_on().addAll(depands_on);
        }
        workflowStep.setAttemptCount(attemptCount);

        if (on_true_step_key != null && !on_true_step_key.trim().isEmpty() && 
            on_false_step_key != null && !on_false_step_key.trim().isEmpty()) {
            
            WorkflowStep is_true = workflowStepRepository.findByWorkflowAndStepkey(workflow, on_true_step_key.trim());
            WorkflowStep is_false = workflowStepRepository.findByWorkflowAndStepkey(workflow, on_false_step_key.trim());
            
            if (is_true != null && is_false != null) {
                workflowStep.setConditional_true(is_true);
                workflowStep.setConditional_false(is_false);
            }
        }

        workflowStepRepository.save(workflowStep);
        
        if (stepOrder != null && stepOrder > 1) {
            WorkflowStep previous = workflowStepRepository.findByWorkflowAndStepOrder(workflow, stepOrder - 1).orElse(null);
            if (previous != null) {
                previous.setNext_step(workflowStep); 
                workflowStepRepository.save(previous);
            }
        }
        
        return workflowStep;
    }
    
    @Transactional
    public WorkflowRun createWorkflowRun(String triggerSource, String triggerPayload, String workflowName) {
        // 1. CLEAN WORKFLOWNAME GHOST STRINGS
        String cleanWorkflowName = (workflowName != null) ? workflowName.trim().replace("\r", "") : "";
    
        // 2. Extract context principal securely
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = "";
        if (principal instanceof UserEntity) {
            email = ((UserEntity) principal).getEmail();
        } else {
            email = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        
        // 3. CLEAN EMAIL GHOST STRINGS
        String cleanEmail = (email != null) ? email.trim().replace("\r", "") : "";
        
        // 4. Query clean database values
        WorkflowEntity workflow = workflowRepository.findByWorkflowNameAndUser_Email(cleanWorkflowName, cleanEmail);
        if (workflow == null) {
            throw new BusinessException("No such workflow present ", HttpStatus.NOT_FOUND);
        }
        
        WorkflowStep workflow_step = workflowStepRepository.findByWorkflowAndStepOrder(workflow, 1)
                .orElseThrow(() -> new RuntimeException("No such workflow step present"));

        WorkflowRun workflowRun = new WorkflowRun();
        workflowRun.setTriggerSource(triggerSource != null ? triggerSource.trim() : "UNKNOWN");
        workflowRun.setTriggerPayload(triggerPayload);
        workflowRun.setStartedAt(LocalDateTime.now());
        workflowRun.setCurrentstep(workflow_step);
        workflowRun.setStatus(WorkflowRunStatus.RUNNING);
        workflowRun.setWorkflow(workflow);
        
        workflowRunRepository.save(workflowRun);
        
        return workflowRun;
    }
}