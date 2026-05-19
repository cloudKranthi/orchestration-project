package com.example.workflow.model;

import org.springframework.beans.factory.annotation.Value;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OAuthProviderConfig extends BaseEntity {
    @JoinColumn(name="user_id",nullable=false)
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    public UserEntity user;
    @Column(name="provider",nullable=false,unique=true)
    public String provider;
    @Column(name="clientid")
    public String clientId;
    @Column(name="clientsecret")
    public String clientSecret;
    @Column(name="authorizationurl")
    public String authorizationUrl;
    @Column(name="redirecturl")
    public String redirectUrl;
    @Column(name="tokenurl")
    public String tokenUrl;
  
}
