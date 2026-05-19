package com.example.workflow.engine;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;
import java.util.Properties;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.example.workflow.model.StepRun;
import com.example.workflow.model.StepRunStatus;
import com.example.workflow.model.UserEntity;
import com.example.workflow.model.WorkflowRun;
import com.example.workflow.model.WorkflowRunStatus;
import com.example.workflow.model.WorkflowStep;
import com.example.workflow.service.WorkflowExecutionService;
import com.example.workflow.model.UserCredentialsEntity;
import com.example.workflow.Repository.UserCredentialsRepository;
import com.example.workflow.Repository.WorkflowRunRepository;
import com.example.workflow.Repository.WorkflowStepRepository;
import com.example.workflow.Repository.StepRunRepository;
import com.example.workflow.utils.CryptoUtils;
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSendStepExecutor implements StepExecutor{
    private final UserCredentialsRepository userCredentialsRepository;
    private final StepRunRepository stepRunRepository;
    @Lazy
    private  WorkflowExecutionService workflowExecutionService;
    private final WorkflowStepRepository workflowStepRepository; 
    private final WorkflowRunRepository workflowRunRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CryptoUtils cryptoUtils;
    @Override
    @Transactional
    public void executeStepRun(StepRun stepRun){
         String key="email-send"+stepRun.getId().toString();
         WorkflowRun workflowRun=stepRun.getWorkflowRun();
        Boolean acquired=redisTemplate.opsForValue().setIfAbsent(key,"IN_PROGRESS",Duration.ofMinutes(5));
        if(Boolean.FALSE.equals(acquired)){
            log.warn("StepRun {} is already being processed by another worker instance. Skipping.", stepRun.getId());
            return;
        }
        try{
        JsonNode config=objectMapper.readTree(stepRun.getCurrentworkflowStep().getConfigJson());
        String Provider=config.get("PROVIDER") != null ? config.get("PROVIDER").asText() : "GOOGLE";
        String To_Email=config.get("TO_EMAIL").asText();
        String contextJson=workflowRun.getContextJson();
        JsonNode context=objectMapper.readTree(contextJson);
        String Subject=config.get("SUBJECT").asText()!=null?config.get("SUBJECT").asText():"";
        String body=config.get("BODY").asText();
        if(body==null){
            body=config.get("body").asText();
        }
        String provider=config.get("PROVIDER").asText();
        UserEntity user=stepRun.getWorkflowRun().getWorkflow().getUser();
        UserCredentialsEntity userCredentialsEntity=userCredentialsRepository.findByUserAndProvider(user,provider).orElseThrow(()->new RuntimeException("No such User credential present"));
        String password=cryptoUtils.decrypt(userCredentialsEntity.getEncryptedString());
        List<String>depands=stepRun.getCurrentworkflowStep().getDepands_on();
        StringBuilder prompt=new StringBuilder();
        for(String current:depands){
            log.info("DEPANDS ON LOOP STARTED FOR stepRun { }",current);
          String output=context.path(current).asText("");
          if(output.isEmpty()){
            log.info("Output is empty no such {} is present in contextJson",current);
            WorkflowStep work=workflowStepRepository.findByWorkflowAndStepkey(workflowRun.getWorkflow(), current);
            if(work==null){
                log.info("No such workflow dtep {} present",current);
                stepRun.setStatus(StepRunStatus.FAILED);
                workflowRun.setStatus(WorkflowRunStatus.FAILED);
            }
            else{
                workflowRun.getActive_List().add(stepRun.getCurrentworkflowStep().getStepkey());
                workflowRun.setCurrentstep(work);
                workflowRunRepository.save(workflowRun);
                log.info(" Before Executing workflowStep {} for TriggerSource {}",current,workflowRun.getTriggerSource());
                workflowExecutionService.singleStep(workflowRun);
                log.info("Executing workflowStep {} for TriggerSource {}",current,workflowRun.getTriggerSource());
                workflowRun = workflowRunRepository.findById(workflowRun.getId())
            .orElseThrow(() -> new RuntimeException("Workflow run tracking context missing"));
                contextJson=workflowRun.getContextJson();
                context=objectMapper.readTree(contextJson);
                workflowRun.setCurrentstep(work);
                workflowRunRepository.save(workflowRun);
                 prompt.append(context.path(current).asText(""));
                 log.info("Completed {} adding to the body for email ",current);
            }
          }
          else{
              prompt.append(output).append("\n");
          }
        }
        body+=prompt.toString();
        Properties prop=new Properties();
        prop.put("mail.smtp.auth","true");
        prop.put("mail.smtp.starttls.enable","true");
        if(Provider.equals("GOOGLE")){
            prop.put("mail.smtp.host","smtp.gmail.com");
            prop.put("mail.smtp.port","587");
        }else if(Provider.equals("OUTLOOK")){
             prop.put("mail.smtp.host","smtp-mail.outlook.com");
            prop.put("mail.smtp.port","587");
        }
        else{
            throw new RuntimeException("No such mail service provider available");
        }
        prop.put("mail.smtp.connectiontimeout","5000");
        prop.put("mail.smtp.timeout","10000");
        prop.put("mail.smtp.writetimeout","10000");
        Session mailSession=Session.getInstance(prop);
        Message message=new MimeMessage(mailSession);
        message.setFrom(new InternetAddress(user.getEmail()));
        message.setRecipients(Message.RecipientType.TO,InternetAddress.parse(To_Email));
        message.setText(body);
        message.setSubject(Subject);
       Transport.send(message,user.getEmail(), password);
       log.info("Email successfully sent from {} to {}", user.getEmail(), To_Email);
       String successTrackingJson = "{\"delivery_status\":\"SENT\",\"sent_at\":\"" + java.time.Instant.now() + "\"}";
          stepRun.setOutputJson(successTrackingJson);
            stepRun.setStatus(StepRunStatus.SUCCESS);
            stepRunRepository.save(stepRun);
    }
    catch(Exception e){
        stepRun.setStatus(StepRunStatus.FAILED);
        log.error("Failed to send email for StepRun {}: {}", stepRun.getId(), e.getMessage());
    }
    }
}
