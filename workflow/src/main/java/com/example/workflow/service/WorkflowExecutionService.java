package com.example.workflow.service;
import com.example.workflow.Repository.WorkflowRepository;
import com.example.workflow.model.WorkflowRun;
import com.example.workflow.model.WorkflowStatus;
import com.example.workflow.model.StepRun;
import com.example.workflow.model.StepRunStatus;
import java.time.LocalDateTime;
import java.util.*;
import com.example.workflow.model.WorkflowRunStatus;
import com.example.workflow.engine.StepExecutor;
import com.example.workflow.engine.StepExecutorRegistry;
import com.example.workflow.model.WorkflowStep;

import com.example.workflow.Repository.WorkflowStepRepository;
import com.example.workflow.model.WorkflowEntity;
import com.example.workflow.model.UserEntity;
import com.example.workflow.Repository.WorkflowRunRepository;
import com.example.workflow.Repository.StepRunRepository;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class WorkflowExecutionService {
    private final WorkflowRepository workflowRepository;
    private final StepRunRepository stepRunRepository;
   private final WorkflowStepRepository workflowStepRepository;
   private final StepExecutorRegistry stepExecutorRegistry;
    private final WorkflowRunRepository workflowRunRepository;
    public WorkflowRun createWorkflowRun(String triggerSource,String triggerPayload,String WorkflowName){
        WorkflowRun workflowRun=new WorkflowRun();
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        WorkflowEntity workflow= workflowRepository.findByWorkflowNameAndUser_Email(WorkflowName,email);
        workflowRun.setTriggerSource(triggerSource);
        workflowRun.setTriggerPayload(triggerPayload);
        workflowRun.setStartedAt(LocalDateTime.now());
        workflowRun.setCurrentStepOrder(1);
        workflowRun.setStatus(WorkflowRunStatus.RUNNING);
        workflowRun.setWorkflow(workflow);
        workflowRunRepository.save(workflowRun);
        this.executeSteps(workflowRun);
        return workflowRun;
    }
    public void executeSteps(WorkflowRun workflowRun){
        Boolean con=true;
         while(con){
         WorkflowStep workflowStep=workflowStepRepository.findByIDAndStepOrder(workflowRun.getWorkflow().getId(), workflowRun.getCurrentStepOrder());
        if (workflowStep == null) {
            workflowRun.setStatus(WorkflowRunStatus.SUCCESS); // Fixed status
            workflowRun.setFinishedAt(LocalDateTime.now());
            workflowRunRepository.save(workflowRun);
            con = false;
            break;
        }
          StepRun stepRun = stepRunRepository.findByWorkflowRunAndStepOrder(workflowRun, workflowRun.getCurrentStepOrder())
                .orElseGet(() -> {
          StepRun newRun=new StepRun();
        
        newRun.setWorkflowRun(workflowRun);
        newRun.setStepOrder(workflowRun.getCurrentStepOrder());
        newRun.setInputJson(workflowRun.getTriggerSource());
        stepRunRepository.save(newRun);
        return newRun;
                });
       
        if(workflowStep==null){
            workflowRun.setStatus(WorkflowRunStatus.RUNNING);
            workflowRun.setFinishedAt(LocalDateTime.now());
            workflowRunRepository.save(workflowRun);
            con=false;
            break;
        }
        

        if(workflowStep.getAttemptCount()<stepRun.getAttemptCount()){

            workflowRun.setStatus(WorkflowRunStatus.FAILED);
             workflowRun.setLastError("Reached Maximum number of failed Attempts");
            stepRun.setStatus(StepRunStatus.FAILED);
            workflowStep.setPresentCount(workflowStep.getPresentCount()+1);
            workflowRunRepository.save(workflowRun);
            stepRunRepository.save(stepRun);
            con=false;
            break;
        }
        try{
            var executor=stepExecutorRegistry.getExecutor(workflowStep.getStepType());
            executor.executeStepRun(stepRun);
            workflowRun.setCurrentStepOrder(workflowRun.getCurrentStepOrder()+1);
            stepRun.setStatus(StepRunStatus.SUCCESS);
            workflowRunRepository.save(workflowRun);
            stepRunRepository.save(stepRun);
            
            
        }catch(Exception e){
            workflowRun.setStatus(WorkflowRunStatus.RETRY_PENDING);
            workflowRun.setLastError(e.getMessage());
            stepRun.setStatus(StepRunStatus.RETRY_PENDING);
            stepRun.setNextExecutionAt(LocalDateTime.now().plusMinutes(5));
            stepRun.setAttemptCount(stepRun.getAttemptCount()+1);
            workflowRunRepository.save(workflowRun);
            stepRunRepository.save(stepRun);
            con=false;
            break;
        }
    }
}
@Scheduled(cron="0 * * * * *")
public void releaseStepRun(){
    List<StepRun>remaining=stepRunRepository.findByNextExecutionAtBeforeAndStatus(LocalDateTime.now(),StepRunStatus.RETRY_PENDING);
    for(var stepRuns:remaining){
        executeSteps(stepRuns.getWorkflowRun());
    }
}
}
