package com.example.workflow.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
@Entity
@Getter
@Setter
public class WorkflowEntity extends BaseEntity {

    public String workflowName;
        @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="user_id",nullable=false)
    public UserEntity user;
    public String description;
     private WorkflowStatus status=WorkflowStatus.ACTIVE;
     
}
