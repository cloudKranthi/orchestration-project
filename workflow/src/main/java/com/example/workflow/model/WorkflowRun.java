package com.example.workflow.model;
import lombok.Setter;
import lombok.Getter;
import jakarta.persistence.*;
import java.util.*;
import java.time.LocalDateTime;
@Table(name="workflow_runs",indexes={
          @Index(name = "idx_workflow_run_workflow", columnList = "workflow_id"),
        @Index(name = "idx_workflow_run_status", columnList = "status")
})
@Entity
@Setter
@Getter
public class WorkflowRun extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="workflow_id",nullable=false)
    private WorkflowEntity workflow;
        @Column(name = "trigger_source", length = 100)
    private String triggerSource;
    @Lob
    @Column(name = "trigger_payload")
    private String triggerPayload;
     @Column(name = "current_step_order")
    private Integer currentStepOrder;
    @Column(name = "next_execution_at")
    private LocalDateTime nextExecutionAt;
     @Column(name = "started_at")
    private LocalDateTime startedAt;

      @Column(name = "finished_at")
    private LocalDateTime finishedAt;
    @Column(name = "last_error", length = 1000)
    private String lastError;
    @Enumerated(EnumType.STRING)
     @Column(name = "status", nullable = false)
    private WorkflowRunStatus status=WorkflowRunStatus.PENDING;
}
