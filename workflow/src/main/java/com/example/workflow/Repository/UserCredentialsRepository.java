package com.example.workflow.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import com.example.workflow.model.UserCredentialsEntity;
public interface UserCredentialsRepository  extends JpaRepository<UserCredentialsEntity, UUID>{
    
}
