package com.example.workflow.dto;
import com.example.workflow.model.WorkflowRunStatus;
public record WorkflowRunResponse(String workName,String TriggerSource,String contextJson,String TriggerPayload,String current_step_key,WorkflowRunStatus status) {}
