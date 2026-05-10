package com.example.workflow.engine;
import com.example.workflow.model.StepRun;
import org.springframework.stereotype.Component;
@Component
public class EmailSendStepExecutor implements StepExecutor{
    @Override
    public void executeStepRun(StepRun stepRun){
     System.out.println("Executing email Step Run "+stepRun.getId());
    }
}
