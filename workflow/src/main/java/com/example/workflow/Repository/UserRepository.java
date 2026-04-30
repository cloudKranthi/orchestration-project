package com.example.workflow.Repository;
import com.example.workflow.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    
}
