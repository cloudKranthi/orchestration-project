package com.example.workflow.Repository;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.workflow.model.StepRun;
import com.example.workflow.model.WorkflowEntity;
import java.util.List;

public interface WorkflowRepository extends JpaRepository<WorkflowEntity,UUID>{
   WorkflowEntity findByWorkflowNameAndUser_Email(String workflowName,String email);
}
