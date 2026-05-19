
package com.example.workflow.engine;
import org.quartz.Trigger;
import org.quartz.JobDetail;
import org.springframework.stereotype.Component;

import java.text.NumberFormat.Style;
import java.time.LocalDateTime;
import org.springframework.transaction.annotation.Transactional;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TriggerBuilder;
import java.time.ZoneId;
import java.util.Date;
import com.example.workflow.model.StepRun;
import com.example.workflow.model.StepRunStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
@RequiredArgsConstructor
public class DelayStepExecutor implements StepExecutor {
    private final ObjectMapper objectMapper;
    private final Scheduler scheduler;
    @Override
    @Transactional
    public void executeStepRun(StepRun stepRun) {
        JsonNode config;
try {
    config = objectMapper.readTree(stepRun.getCurrentworkflowStep().getConfigJson());
} catch (JsonProcessingException e) {
    throw new RuntimeException("Failed to parse Config JSON for step: " + stepRun.getId(), e);
}
        try{
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
        
        scheduler.scheduleJob(job,trigger);
        }catch(Exception e){
           log.error(e.getMessage());
           stepRun.setStatus(StepRunStatus.RETRY_PENDING);
        }
        
    }
}