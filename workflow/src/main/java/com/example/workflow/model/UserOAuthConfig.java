package com.example.workflow.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;


@Entity
public class UserOAuthConfig  extends BaseEntity{
    @JoinColumn(name="user_id",nullable=false)
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    public UserEntity user;
    @Column(name="provider",nullable=false,unique=true)
    public String provider;
    @Column(name="accesstoken")
    public String accessToken;
    @Column(name="refreshtoken")
    public String refreshToken;
    @Column(name="expiredAt")
    public LocalDateTime expiredAt;
      @Column(name="status")
    public OAuthStatus status;



}
