package com.example.workflow.model;
import java.util.UUID;
public class WorkflowEntity extends BaseEntity {
    public String WorkflowName;
    public UUID owner_id;
    public String description;
     private WorkflowStatus status;
     
}
