package com.example.workflow.model;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
@Entity
@Getter
@Setter
public class UserEntity extends BaseEntity{
    @Column(nullable=false,unique=true)
    private String username;
    @Column(nullable=false,unique=true)
    private String email;
    @Column(nullable=false,unique=true)
    private String phoneNumber;
    @Column(nullable=false)
    private String password;
    private String refreshToken;
    @ElementCollection(fetch=FetchType.EAGER)
    private String role;
    
} 