
package com.example.workflow.engine;
import org.springframework.scheduling.Trigger;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import java.time.ZoneId;
import java.util.Date;
import com.example.workflow.model.StepRun;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;


@Component

@RequiredArgsConstructor
public class DelayStepExecutor implements StepExecutor {
    private final ObjectMapper objectMapper;
    private final Scheduler scheduler;

    @Override
    public void executeStepRun(StepRun stepRun) {
    
        JsonNode config = objectMapper.readTree(stepRun.getWorkflowStep().getConfigJson());
        
        int delay = config.path("DELAY").asInt(0);
        String unit = config.path("UNIT").asText("DAYS").toUpperCase(); 

        LocalDateTime exp = unit.equals("MINUTES") 
            ? LocalDateTime.now().plusMinutes(delay) 
            : LocalDateTime.now().plusDays(delay);

    
        JobDetail job = JobBuilder.newJob(WorkflowJob.class)
            .withIdentity("job-" + stepRun.getWorkflowRun().getId())
            .usingJobData("workflowRunId", stepRun.getWorkflowRun().getId().toString())
            .build();

        
        Trigger trigger = TriggerBuilder.newTrigger()
            .withIdentity("trigger-" + stepRun.getId())
            .startAt(Date.from(exp.atZone(ZoneId.systemDefault()).toInstant()))
            .build();
        
        scheduler.scheduleJob(job, trigger);
        
        
    }
}