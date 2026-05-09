package com.example.workflow.service;

import javax.management.RuntimeErrorException;

import com.example.workflow.model.StepRunStatus;
import com.example.workflow.Repository.WorkflowStepRepository;

import org.springframework.stereotype.Service;

import com.example.workflow.Repository.UserRepository;
import com.example.workflow.Repository.WorkflowRepository;
import com.example.workflow.model.UserEntity;
import com.example.workflow.model.WorkflowEntity;
import com.example.workflow.model.WorkflowStep;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkflowCreationService {
    private final UserRepository userRepository;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    @Transactional
    public WorkflowEntity createWorkflow(String email,String name,String Description){
      
             UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No such user found"));
      WorkflowEntity workflow=new WorkflowEntity();
      workflow.setUser(user);
      workflow.setWorkflowName(name);
      workflow.setDescription(Description);
      
      return workflowRepository.save(workflow);
    }
    @Transactional
    public WorkflowStep createWorkflowStep(String workflowname,Integer steporder,String stepKey,
    Boolean isCritical,WorkflowStepStatus status,String config_json,Integer timeoutSeconds,Integer attemptCount
    ){
        WorkflowEntity workflow=workflowRepository.findByWorkflowName(workflowname).OrElse(()->new RuntimeException("No such workflow exists"));
        if(steporder==null||steporder<=0){
            throw new RuntimeException("No such step Order exists");
        }
        if(stepKey==null){
            throw new RuntimeException("Step Key cant be null ");
        }
        if(config_json==null){
            throw new RuntimeException("Config json cant be null");
        }
        isCritical=isCritical!=null?isCritical:true;
        attemptCount=attemptCount>0?attemptCount:5;
        WorkflowStep workflowStep=new WorkflowStep();
        workflowStep.setStepOrder(steporder);
        workflowStep.setStepkey(stepKey);
        workflowStep.setConfigJson(config_json);
        workflowStep.setWorkflow(workflow);
        workflowStep.setTimeoutSeconds(timeoutSeconds);
        workflowStep.setIsCritical(isCritical);
        workflowStep.setAttemptCount(attemptCount);
        workflowStepRepository.save(workflowStep);
    }
}
