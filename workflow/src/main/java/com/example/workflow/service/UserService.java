
package com.example.workflow.service;



import java.util.Date;

import java.util.Map;

import java.util.UUID;

import java.util.function.Function;



import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;



import com.example.workflow.Repository.UserRepository;



import io.jsonwebtoken.Claims;

import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    // We pull the same values as your JwtService to keep the cookies in sync
    @Value("${at_expiry}")
    private long atExpiry;
    @Value("${rt_expiry}")
    private long rtExpiry;

    @Transactional
    public void LoginUser(String email, String password, HttpServletResponse response) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (passwordEncoder.matches(password, user.getPassword())) {
            // 1. Generate Tokens using your existing JwtService
            Map<String, String> tokens = jwtService.CreateToken(user.getId());
            String at = tokens.get("accessToken");
            String rt = tokens.get("refreshToken");

            // 2. Set the state in the DB (Your "Double-Check" requirement)
            user.setRefreshToken(rt);
            userRepository.save(user);

            // 3. Set the Cookies for the Browser
            addCookie(response, "accessToken", at, (int) (atExpiry / 1000));
            addCookie(response, "refreshToken", rt, (int) (rtExpiry / 1000));
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
}