package com.example.workflow.model;

import jakarta.persistence.*;
import java.util.*;
import java.time.LocalDateTime;
@Table(name="workflow_run")
@Entity
public class WorkflowRun extends BaseEntity {
    @ManyToOne(fetch=Fetch_Type.Lazy,optional=false)
    @JoinColumn(name="workflow_id",nullable=false)
    private WorkflowEntity workflow;
    private String trigger_source;
    private String trigger_payload;
    private Integer currentStepOrder;
    private LocalDateTime next_Execution_At;
    private LocalDateTime finished_At;
    private String lastError;
    @Enumerated(EnumType.STRING)
    private WorkflowRunStatus status;
}
