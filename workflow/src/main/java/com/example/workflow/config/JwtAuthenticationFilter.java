package com.example.workflow.config;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.workflow.Repository.UserRepository;
import com.example.workflow.service.JwtService;
import java.util.List;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.example.workflow.model.UserEntity;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.NonNull;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter  extends OncePerRequestFilter{

   private final JwtService jwtService;
    private final UserRepository userRepository;
    
    @Override
    
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain)throws ServletException,IOException{
        String jwt="";
        Cookie cookies[]=request.getCookies();
        for(Cookie cookie:cookies){
         if(("accessToken").equals(cookie.getName())){jwt=cookie.getValue();
            break;
        }
        }   
        if(jwt!=null&&SecurityContextHolder.getContext()==null){
            if(jwtService.equals(jwt))
            try{
            String userId=jwtService.extractClaims(jwt,Claims::getSubject);
            
            UserEntity user=userRepository.findById(UUID.fromString(userId)).orElseThrow(()->new RuntimeException("No such user found"));
            if(user!=null&&jwtService.checkToken(jwt)){

            String role=user.getRole();
            var Authorities=List.of(new SimpleGrantedAuthority("ROLE_"+role));
                UsernamePasswordAuthenticationToken authToken=new UsernamePasswordAuthenticationToken(user, null,Authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
            catch(Exception e){
                logger.error("Could not set user authentication in security context", e);
            }
        } 
        filterChain.doFilter(request,response);
    }
}
