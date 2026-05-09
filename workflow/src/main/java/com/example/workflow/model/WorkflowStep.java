package com.example.workflow.model;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
@Table(name="workflow_steps",
    uniqueConstraints={
        @UniqueConstraint(columnNames={"workflow_id","step_order"})
    },indexes={
        @Index(name="idx_workflow_step_workflow",columnList="workflow_id,stepOrder")
    }
)
 @Entity
 @Getter
 @Setter
public class WorkflowStep  extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="workflow_id",nullable=false)
    private WorkflowEntity workflow;
    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;
     @Column(name = "step_key", nullable = false)
    private String stepkey;
    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical=true;
    @Enumerated(EnumType.STRING)
 @Column(name = "step_type", nullable = false)
    private StepType stepType;
          @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount=0;
    @Column(name = "attempt_count", nullable = false)
    private Integer presentCount=1;
    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkflowStepStatus status=WorkflowStepStatus.ACTIVE;
    @Lob
     @Column(name = "config_json")
    private String configJson;


}
