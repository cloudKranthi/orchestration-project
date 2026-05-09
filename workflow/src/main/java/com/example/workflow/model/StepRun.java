package com.example.workflow.model;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@Entity
@Table(name="step_runs",
    indexes = {
        @Index(name = "idx_step_run_workflow_run", columnList = "workflow_run_id"),
        @Index(name = "idx_step_run_status", columnList = "status")
    }
)
public class StepRun extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="workflow_run_id",nullable=false)
    private WorkflowRun workflowRun;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="workflow_step_id",nullable=false)
    private WorkflowStep workflowStep;
    @Lob
    @Column(name = "input_json")
    private String inputJson;
    @Lob
    @Column(name="output_json")
    private String outputJson;
    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    @Column(name = "next_execution_at")
    private LocalDateTime nextExecutionAt;
      @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount=0;
      @Column(name = "duration_ms")
    private Long durationMs;
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
        @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepRunStatus status=StepRunStatus.PENDING;


}
