package com.example.workflow.dto;
import com.example.workflow.model.StepType;
import com.example.workflow.model.WorkflowRunStatus;
import java.util.List;
public record WorkflowStepRequest(String workflowName,String on_true_step_key,String on_false_step_key,String stepKey,
    Boolean isCritical,String config_json,Integer timeoutSeconds,Integer attemptCount,Integer stepOrder,List<String>depands_on,StepType stepType) {
    
}
