package com.example.workflow.Repository;
import com.example.workflow.model.WorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface  WorkflowRunRepository extends JpaRepository<WorkflowRun,UUID> {
    
}
