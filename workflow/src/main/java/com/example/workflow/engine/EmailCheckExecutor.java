package com.example.workflow.engine;
import com.example.workflow.service.CredentialsService;
import com.example.workflow.Repository.UserCredentialsRepository;
import com.example.workflow.model.StepRun;
import com.example.workflow.model.StepRunStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Properties;

import com.example.workflow.Repository.StepRunRepository;

// Jakarta Mail Core (The "Machine")

import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;

// Jakarta Mail Search API (The "Filters")
import jakarta.mail.search.SearchTerm;
import jakarta.mail.search.AndTerm;
import jakarta.mail.search.SubjectTerm;
import jakarta.mail.search.FromStringTerm;

import com.example.workflow.utils.CryptoUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.workflow.model.WorkflowStep;
import com.example.workflow.Repository.WorkflowStepRepository;
import com.example.workflow.model.UserEntity;
import com.example.workflow.model.UserCredentialsEntity;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.node.ObjectNode;


@Component
@Slf4j
@RequiredArgsConstructor
public class EmailCheckExecutor  implements StepExecutor{
    private final CryptoUtils cryptoUtils;
    private final CredentialsService credentialsService;
    private final UserCredentialsRepository userCredentialsRepository;
    private final ObjectMapper objectMapper;
    private final WorkflowStepRepository workflowStepRepository;
    private final StepRunRepository stepRunRepository;
    @Override
    @Transactional
    public void executeStepRun(StepRun stepRun){
        try{
    WorkflowStep workflowStep=stepRun.getCurrentworkflowStep();
    JsonNode config=objectMapper.readTree(stepRun.getCurrentworkflowStep().getConfigJson());
    String Subject=config.path("SUBJECT").asText();
    String fromemail=config.path("FROM_EMAIL").asText();
    String provider=config.path("PROVIDER").asText();
     UserEntity user=stepRun.getWorkflowRun().getWorkflow().getUser();
    UserCredentialsEntity userCredentialsEntity=userCredentialsRepository.findByUserAndProvider(user, provider).orElseThrow(()->new RuntimeException("No such User credential present"));
    String decryptedString=cryptoUtils.decrypt(userCredentialsEntity.getEncryptedString());
    Properties props=new Properties();
    props.put("mail.store.protocol","imaps");
    Session session=Session.getInstance(props);

    Store store= session.getStore("imaps");
    if(provider.equals("GOOGLE"))
    store.connect("imaps.gmail.com",user.getEmail(),decryptedString);
    else if(provider.equals("OUTLOOK")){
        store.connect("imaps.outlook.com",user.getEmail(),decryptedString);
    }
    else{
         store.connect("imaps.provider.com",user.getEmail(),decryptedString);
    }
    Folder folder=store.getFolder("INBOX");
    folder.open(folder.READ_ONLY);
    SearchTerm fromTerm= new FromStringTerm(fromemail);
    SearchTerm SubjectTerm =new SubjectTerm(Subject);
    SearchTerm combinedTerm =new AndTerm(fromTerm,SubjectTerm);
    Message [] messages=folder.search(combinedTerm);
    if(messages.length>0){
         Message message=messages[messages.length-1];
       ObjectNode output=objectMapper.createObjectNode();
       output.put("subject",message.getSubject());
       output.put("found",true);
       stepRun.setOutputJson(output.toString());

       stepRun.setBooleanResult(true);
       stepRun.setStatus(StepRunStatus.SUCCESS);
        stepRunRepository.save(stepRun);
        
    }
    else{
         stepRun.setBooleanResult(false);
          
    }
    folder.close(false);
    store.close();
}catch(Exception e){
log.error("Email Step Failed: {}", e.getMessage());
            stepRun.setStatus(StepRunStatus.FAILED);
}
    }
}
