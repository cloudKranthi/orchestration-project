
package com.example.workflow.service;

import com.example.workflow.exception.BusinessException;

import java.util.Date;

import java.util.Map;

import java.util.UUID;

import java.util.function.Function;
import com.example.workflow.model.UserEntity;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;



import com.example.workflow.Repository.UserRepository;



import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private  PasswordEncoder passwordEncoder= new BCryptPasswordEncoder();
    
    
    

    // We pull the same values as your JwtService to keep the cookies in sync
    @Value("${app.jwt.access-token-expiry}")
    private long atExpiry;
    @Value("${app.jwt.refresh-token-expiry}")
    private long rtExpiry;

    @Transactional
    public void LoginUser(String email, String password,HttpServletResponse resposne) {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if(user==null){
           throw new BusinessException("No such user found", HttpStatus.NOT_FOUND);
        }
        if (passwordEncoder.matches(password, user.getPassword())) {
            // 1. Generate Tokens using your existing JwtService
            Map<String, String> tokens = jwtService.CreateToken(user.getId());
            String at = tokens.get("accessToken");
            String rt = tokens.get("refreshToken");

            
            user.setRefreshToken(rt);
            userRepository.save(user);

            
            addCookie(resposne, "accessToken", at, (int) (atExpiry / 1000));
            addCookie(resposne, "refreshToken", rt, (int) (rtExpiry / 1000));
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // Should be true for HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAge); // Uses the converted at_expiry/rt_expiry
        response.addCookie(cookie);
    }
    @Transactional
    public UserEntity registerUser(String email,String phoneNumber,String password,String username,String role){
          UserEntity user=userRepository.findByEmail(email).orElse(null);
          if(user!=null){
            throw new RuntimeException("User already exists");
          }
          String cleanRole = (role != null) ? role.trim().replace("\r", "").toUpperCase() : "";
          if(!"ADMIN".equals(cleanRole) && !"USER".equals(cleanRole)){
            throw new BusinessException("No such role present", HttpStatus.BAD_REQUEST);
          }
          UserEntity newUser=new UserEntity();
          newUser.setEmail(email);
          newUser.setPhoneNumber(phoneNumber);
          newUser.setUsername(username);
          String encodePassword=passwordEncoder.encode(password);
          newUser.setPassword(encodePassword);
          newUser.setRole(role);
          userRepository.save(newUser);
          return newUser;

    }
}