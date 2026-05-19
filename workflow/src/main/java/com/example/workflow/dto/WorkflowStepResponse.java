package com.example.workflow.dto;

import java.util.List;

import com.example.workflow.model.StepType;

public record WorkflowStepResponse(String workflowName,String stepKey,Integer stepOrder,StepType stepType,String configJson,List<String>depands_on) {
    
}
