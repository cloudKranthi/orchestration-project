package com.example.workflow.dto;

public record WorkflowRunRequest(String TriggerSource,String TriggerPayload,String workflowName) {
    
}
