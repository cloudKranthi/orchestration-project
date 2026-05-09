package com.example.workflow.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import com.example.workflow.model.StepRun;
import com.example.workflow.model.StepRunStatus;
import com.example.workflow.model.WorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StepRunRepository extends JpaRepository<StepRun, UUID> {

    
    List<StepRun> findByNextExecutionAtBeforeAndStatus(LocalDateTime time, StepRunStatus status);
    Optional<StepRun> findByWorkflowRunAndStepOrder(WorkflowRun workflowRun, Integer stepOrder);
}