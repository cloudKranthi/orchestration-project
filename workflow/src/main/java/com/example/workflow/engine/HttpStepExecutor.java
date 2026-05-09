package com.example.workflow.engine;
import org.springframework.stereotype.Component;
import com.example.workflow.model.StepRun;
@Component

public class HttpStepExecutor implements StepExecutor{
    @Override
    public void executeStepRun(StepRun stepRun){
        System.out.println("Implementing httpStep "+stepRun.getId());
    }
}
