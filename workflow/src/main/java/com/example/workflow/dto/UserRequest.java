package com.example.workflow.dto;

import io.micrometer.common.lang.NonNull;

public record UserRequest(@NonNull String email,String username, String phoneNumber, String password,String role ) {
    
}
