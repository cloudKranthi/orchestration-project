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
@RequiredArgsConstructor
@Service
public class JwtService {
    private final UserRepository userRepositry;
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    @Value("${app.jwt.access-token-expiry}")
    private long atExpiry;
    @Value("${app.jwt.refresh-token-expiry}")
    private long rtExpiry;

   public Map<String,String> CreateToken(UUID userId){
      String accessToken =Jwts.builder().setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis()+atExpiry))
      .setSubject(userId.toString()).signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).compact();
        String refreshToken =Jwts.builder().setIssuedAt(new Date(System.currentTimeMillis())).setExpiration(new Date(System.currentTimeMillis()+rtExpiry))
        .setSubject(userId.toString()).signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes())).compact();
        return Map.of( "accessToken",accessToken, "refreshToken",refreshToken);
   }
   public Boolean ValidateToken(String token){
    Boolean isUserValid=usercheck(token);
    Boolean isTokenValid=checkToken(token);
    return isUserValid && isTokenValid;
   }
   public Boolean usercheck(String Token){
    return userRepositry.existsById(UUID.fromString(extractClaims(Token, Claims::getSubject)));
   }
   public Boolean checkToken(String token){
    return extractClaims(token,Claims::getExpiration).after(new Date());
   }
   public <T> T extractClaims(String token,Function<Claims,T>claimsResolver){
    Claims claim=Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
    .build().parseClaimsJws(token).getBody();
    return claimsResolver.apply(claim);
   }
}
