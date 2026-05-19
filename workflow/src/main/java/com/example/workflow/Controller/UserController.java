package com.example.workflow.Controller;
import java.util.Map;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.workflow.dto.UserRequest;
import com.example.workflow.dto.UserResponse;
import com.example.workflow.model.UserEntity;
import com.example.workflow.service.CredentialsService;
import com.example.workflow.service.UserService;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final CredentialsService credentialsService;
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @NonNull UserRequest request){
        UserEntity user=userService.registerUser(request.email(), request.phoneNumber(), request.password(), request.username(),request.role());
        UserResponse response=new UserResponse(
            user.getEmail(),user.getUsername(),user.getPhoneNumber(),user.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> payload,HttpServletResponse response){
       String email=payload.get("email");
       String password=payload.get("password");
        userService.LoginUser(email, password, response);
        return ResponseEntity.ok("User logged in");
       
    }
    @PostMapping("/register-user-credentials")
    public ResponseEntity<?> registerCredential(@RequestBody @NonNull Map<String, String> payload){
        String provider=payload.get("provider");
        String password=payload.get("password");
       credentialsService.saveCredentials(provider, password);
       return ResponseEntity.ok("User credentials saved");
    }
    
}
