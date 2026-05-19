package com.example.workflow.service;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
@Service

public class GeminiService {
    private final RestTemplate restTemplate=new RestTemplate();
    @Value("${gemini.api.key}")
    private String apikey;
   private final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";
   public String use(String prompt){
    String url=GEMINI_URL+apikey;

    Map<String,Object> body=Map.of("contents",List.of(Map.of("parts",List.of(Map.of("text",prompt)))));
    try{
        ResponseEntity<JsonNode> response=restTemplate.postForEntity(url,body,JsonNode.class);
        return response.getBody().path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
    }catch(Exception e){
       throw new RuntimeException("AI API Call failed: " + e.getMessage());
    }

   }
}
