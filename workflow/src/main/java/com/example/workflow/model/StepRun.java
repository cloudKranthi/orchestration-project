package com.example.workflow.model;
import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="step_run")
public class StepRun extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="workflow_run_id",nullable=false)
    private WorkflowRun workflowRun;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="workflow_step_id",nullable=false)
    private WorkflowStep workflowStep;
    private String inputJson;
    private String outputJson;
    private LocalDateTime finished_at;
    private LocalDateTime next_Execution_at;
    private Integer attemptCount=0;
    private Long DurationMs;
    private String errorMessage;
    private Integer stepOrder;
    @Enumerated(EnumType.STRING)
    private StepRunStatus stepRunStatus=StepRunStatus.ACTIVE;


}
