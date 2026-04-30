package com.example.workflow.model;
import java.util.UUID;
import jakarta.persistence.*;
import com.example.workflow.model.WorkflowEntity;
@Table(name="workflow_steps",
    uniqueConstraints={
        @UniqueConstraint(columnNames={"workflow_id","step_order"})
    },indexes={
        @Index(name="idx_workflow_step_workflow",columnList="workflow_id,stepOrder")
    }
)
 @Entity
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

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkflowStepStatus status=WorkflowStepStatus.ACTIVE;
    @Lob
     @Column(name = "config_json")
    private String configJson;


}
