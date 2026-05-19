package com.example.workflow.engine;
import java.util.UUID;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.transaction.annotation.Transactional;

import com.example.workflow.Repository.StepRunRepository;
import com.example.workflow.Repository.WorkflowRunRepository;
import com.example.workflow.model.StepRunStatus;
import com.example.workflow.service.WorkflowExecutionService;

import lombok.RequiredArgsConstructor;
@RequiredArgsConstructor
public class WorkflowJob  implements Job{
    private final WorkflowExecutionService executionService;
    private final WorkflowRunRepository workflowRunRepository;
    private final StepRunRepository stepRunRepository;
    @Override
    @Transactional
     public void execute(JobExecutionContext context){
        String key=context.getMergedJobDataMap().getString("WorkflowRunId");
        UUID workflowid=UUID.fromString(key);
        workflowRunRepository.findById(workflowid).ifPresent(workflowRun->{
          
           stepRunRepository.findByWorkflowRunAndCurrentworkflowStep(workflowRun,workflowRun.getCurrentstep()).ifPresent(stepRun->{;
            stepRun.setStatus(StepRunStatus.SUCCESS);
                    stepRunRepository.save(stepRun);
                    workflowRunRepository.save(workflowRun);
                    executionService.executeSteps(workflowRun);
            });
        });
     }
}
