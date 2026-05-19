package com.example.workflow.Repository;
import com.example.workflow.model.WorkflowEntity;
import com.example.workflow.model.WorkflowRun;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.workflow.model.WorkflowStep;
import com.example.workflow.model.WorkflowRunStatus;
import java.util.UUID;
public interface  WorkflowRunRepository extends JpaRepository<WorkflowRun,UUID> {
    WorkflowRun findByCurrentstepAndStatus(WorkflowStep workflow_step,WorkflowRunStatus status);
    @EntityGraph(attributePaths = {"currentstep","workflow","workflow.user","active_List"})
    WorkflowRun findByWorkflowAndTriggerSource(WorkflowEntity workflow,String TriggerSource);
}
