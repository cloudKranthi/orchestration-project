package com.example.workflow.model;

import java.util.List;
import java.util.ArrayList;
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
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import lombok.Getter;
import lombok.Setter;

@Table(name="workflow_steps",
    uniqueConstraints={
        @UniqueConstraint(columnNames={"workflow_id","step_order"})
    },indexes={
        @Index(name="idx_workflow_step_workflow",columnList="workflow_id,step_order") // Fixed index naming match
    }
)
@Entity
@Getter
@Setter
public class WorkflowStep extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="workflow_id", nullable=false)
    private WorkflowEntity workflow;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "step_key", nullable = false)
    private String stepkey;

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_type", nullable = false)
    private StepType stepType;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;
    

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="next_step_linear")
    private WorkflowStep next_step;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="conditional_true")
    private WorkflowStep conditional_true;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="conditional_false")
    private WorkflowStep conditional_false;

    

    @Column(name = "timeout_seconds")
    private Integer timeoutSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkflowStepStatus status = WorkflowStepStatus.ACTIVE;

    
    @Column(name = "config_json")
    private String configJson;
        
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "workflow_step_dependencies",
        joinColumns = @JoinColumn(name = "workflow_step_id")
    )
    @Column(name = "depends_on_step_key")
    private List<String> depands_on = new ArrayList<>();
}