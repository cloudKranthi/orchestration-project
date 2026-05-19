package com.example.workflow.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.workflow.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@RequiredArgsConstructor
@Slf4j
@RestControllerAdvice
public class GlobalException {
    @Value("${app.mode}")
    private String mode;
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<?> handleBusinessException(BusinessException e,HttpServletRequest req){
        String random_id=UUID.randomUUID().toString();
        log.warn("OPERATIONAL ERROR[{}] Method[{}] URL[{}] Message[{}] Time[{}] ",random_id,req.getMethod(),req.getRequestURL(),e.getMessage(),LocalDateTime.now());
        ErrorResponse er=new ErrorResponse(
           "OPERATIONAL", random_id,req.getMethod(),req.getRequestURL().toString(),LocalDateTime.now(),e.getMessage(),getStackTrace(e));
        return  new ResponseEntity<>(er,e.getHttpStatus());
    }
     @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e,HttpServletRequest req){
        String random_id=UUID.randomUUID().toString();
        log.error("FATAL  ERROR[{}] Method[{}] URL[{}] Message[{}] Time[{}] ",random_id,req.getMethod(),req.getRequestURL(),e.getMessage(),LocalDateTime.now());
         ErrorResponse er=new ErrorResponse(
           "FATAL", random_id,req.getMethod(),req.getRequestURL().toString(),LocalDateTime.now(),e.getMessage(),getStackTrace(e));
        return new  ResponseEntity<>(er,HttpStatus.INTERNAL_SERVER_ERROR);
    }
    public String getStackTrace(Exception e){
        if("prod".equals(mode))return "";
        StringWriter stringWriter=new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
