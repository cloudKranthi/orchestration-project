package com.example.workflow.dto;

import java.time.LocalDateTime;

public record ErrorResponse(String Type,String id,String url,String method,LocalDateTime requestTime,String message,String stackTrace) {
    
}
