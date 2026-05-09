package com.example.workflow.Repository;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.workflow.model.WorkflowEntity;
import com.example.workflow.model.WorkflowStep;
public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {
    WorkflowStep findByIDAndStepOrder(UUID id,Integer order);
   
}
