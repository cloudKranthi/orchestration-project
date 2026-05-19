package com.example.workflow.model;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
    
    @Column(name = "trigger_payload")
    private String triggerPayload;
     
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="current_step_order")
    private WorkflowStep currentstep;
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
    @Column(columnDefinition="Text")
    private String contextJson="{}";
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name="active_list")
    private List<String> active_List=new ArrayList<>();
}
