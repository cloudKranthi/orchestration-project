package com.example.workflow.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;
import com.example.workflow.model.UserEntity;
import com.example.workflow.model.UserCredentialsEntity;
public interface UserCredentialsRepository  extends JpaRepository<UserCredentialsEntity, UUID>{

    Optional<UserCredentialsEntity> findByUserAndProvider(UserEntity user,String provider);
}
