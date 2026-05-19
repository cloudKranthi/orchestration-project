package com.example.workflow.model;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
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
    private WorkflowStep  currentworkflowStep;
    
    @Column(name = "input_json",columnDefinition = "TEXT")
    private String inputJson;
  
    @Column(name="output_json",columnDefinition = "TEXT")
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
        
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepRunStatus status=StepRunStatus.PENDING;
    @Column(name = "boolean_result")
private Boolean booleanResult;


}
