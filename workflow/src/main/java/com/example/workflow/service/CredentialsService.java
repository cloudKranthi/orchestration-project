package com.example.workflow.service;
import com.example.workflow.model.UserEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.workflow.utils.CryptoUtils;
import com.example.workflow.Repository.UserCredentialsRepository;
import com.example.workflow.Repository.UserRepository;
import com.example.workflow.model.UserCredentialsEntity;
import org.springframework.stereotype.Service;
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialsService {
    private final UserCredentialsRepository userCredentialsRepository;
    private final UserRepository userRepository;
    private final CryptoUtils cryptoUtils;
    public void saveCredentials(String provider,String password){
        String email=SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("The email fetched from Security context is {}",email);
        String cleanEmail = (email != null) ? email.trim().replace("\r", "") : "";
    String cleanProvider = (provider != null) ? provider.trim().toUpperCase().replace("\r", "") : "";
        try{
        String encryptString=cryptoUtils.encrypt(password);
        log.info("The email is {} and provider is {} ",cleanEmail,cleanProvider);
        UserEntity user=userRepository.findByEmail(cleanEmail).orElseThrow(()->new RuntimeException("No such User found"));
        UserCredentialsEntity userCredentialsEntity=new UserCredentialsEntity();
        userCredentialsEntity.setProvider(cleanProvider);
        userCredentialsEntity.setUser(user);
        userCredentialsEntity.setEncryptedString(encryptString);
        userCredentialsRepository.save(userCredentialsEntity);
        }catch(Exception e){
            log.error(e.getMessage());
        }
    }
}
