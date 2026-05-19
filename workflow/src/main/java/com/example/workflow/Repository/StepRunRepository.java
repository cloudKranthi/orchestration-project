package com.example.workflow.Repository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import com.example.workflow.model.StepRun;
import com.example.workflow.model.StepRunStatus;
import com.example.workflow.model.WorkflowRun;
import com.example.workflow.model.WorkflowRunStatus;
import com.example.workflow.model.WorkflowStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StepRunRepository extends JpaRepository<StepRun, UUID> {

    StepRun findByCurrentworkflowStepAndStatus(WorkflowStep workflow_step,WorkflowRunStatus status);
    @Query("SELECT sr FROM StepRun sr " +
           "JOIN FETCH sr.currentworkflowStep " +
           "WHERE sr.nextExecutionAt < :time AND sr.status = :status")
    List<StepRun> findByNextExecutionAtBeforeAndStatus(LocalDateTime time, StepRunStatus status);
    Optional<StepRun> findByWorkflowRunAndCurrentworkflowStep(WorkflowRun workflowRun, WorkflowStep workflowStep);
}