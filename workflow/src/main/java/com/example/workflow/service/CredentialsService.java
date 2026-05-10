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
        try{
        String encryptString=cryptoUtils.encrypt(password);
        UserEntity user=userRepository.findByEmail(email).OrElseThrow(()->new RuntimeException("No such User found"));
        UserCredentialsEntity userCredentialsEntity=new UserCredentialsEntity();
        userCredentialsEntity.setProvider(provider);
        userCredentialsEntity.setUser(user);
        userCredentialsEntity.setEncryptedString(encryptString);
        userCredentialsRepository.save(userCredentialsEntity);
        }catch(Exception e){
            log.error(e.getMessage());
        }
    }
}
