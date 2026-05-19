package com.example.workflow.model;
import jakarta.persistence.Column;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
@Getter
@Setter
@MappedSuperclass
public  abstract class BaseEntity{
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable=false,nullable=false)
    @Id
   public UUID id;
   @CreationTimestamp
   @Column(updatable=false,nullable=false)
   public LocalDateTime createdAt;
   @UpdateTimestamp
   @Column(updatable=false,nullable=false)
   public LocalDateTime updatedAt;
}