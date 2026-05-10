package com.example.workflow.model;
import lombok.Getter;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Setter;
@Getter
@Setter
@Entity
public class UserCredentialsEntity extends BaseEntity {
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="user_id",nullable=false)
    public UserEntity user;
    private String provider;
    private String encryptedString;

}
