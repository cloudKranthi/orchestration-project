package com.example.workflow.engine;
import com.example.workflow.Repository.WorkflowRunRepository;
import com.example.workflow.service.WorkflowExecutionService;
import lombok.RequiredArgsConstructor;
import com.example.workflow.model.StepRun;
import com.example.workflow.model.StepRunStatus;
import com.example.workflow.model.WorkflowRun;

import com.example.workflow.Repository.StepRunRepository;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
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
          
           stepRunRepository.findByWorkflowRunAndStepOrder(workflowRun,workflowRun.getCurrentStepOrder()).ifPresent(stepRun->{;
            stepRun.setStatus(StepRunStatus.SUCCESS);
                    stepRunRepository.save(stepRun);

                
                    workflowRun.setCurrentStepOrder(workflowRun.getCurrentStepOrder() + 1);
                    workflowRunRepository.save(workflowRun);

                    
                    executionService.executeSteps(workflowRun);
            });
        });
     }
}
