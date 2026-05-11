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
import tools.jackson.databind.deser.std.FromStringWithRadixToNumberDeserializer;

import com.example.workflow.model.UserEntity;
import com.example.workflow.model.UserCredentialsEntity;



import org.springframework.stereotype.Component;


import tools.jackson.databind.node.ObjectNode;
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailCheckExecutor  implements StepExecutor{
    private final CryptoUtils cryptoUtils;
    private final CredentialsService credentialsService;
    private final UserCredentialsRepository userCredentialsRepository;
    private final ObjectMapper objectMapper;
    private final StepRunRepository stepRunRepository;
    @Override
    public void executeStepRun(StepRun stepRun){
        try{
    JsonNode config=objectMapper.readTree(stepRun.getWorkflowStep().getConfigJson());
    String Subject=config.path("SUBJECT").asText();
    String fromemail=config.path("FROM_EMAIL").asText();
    String provider=config.path("PROVIDER").asText();
     UserEntity user=stepRun.getWorkflowRun().getWorkflow().getUser();
    UserCredentialsEntity userCredentialsEntity=userCredentialsRepository.findByUserEntityAndProvider(user, provider).orElseThrow(()->new RuntimeException(e.getMessage()));
    String decryptedString=cryptoUtils.decrypt(userCredentialsEntity.getEncryptedString());
    Properties props=new Properties();
    props.put("mail.store.protocol","imaps");
    Session session=Session.getInstance(props);

    Store store= session.getStore("imaps");
    store.connect("imaps.gmail.com",user.getEmail(),decryptedString);
    Folder folder=store.getFolder("INBOX");
    folder.open(folder.READ_ONLY);
    SearchTerm fromTerm= new FromStringTerm(fromemail);
    SearchTerm SubjectTerm =new SubjectTerm(Subject);
    SearchTerm combinedTerm =new AndTerm(fromTerm,SubjectTerm);
    Message [] messages=folder.search(combinedTerm);
    if(messages.length>0){
         Message message=messages[messages.length-1];
       ObjectNode output=new objectMapper.createObjectNode();
       output.put("subject",message.getSubject());
       output.put("found",true);
       stepRun.setOutputJson(output.toString());
       stepRunRepository.save(stepRun);
       stepRun.setStatus(StepRunStatus.SUCCESS);
    }
    else{
        throw new RuntimeException("No email found");
    }
    folder.close(false);
    store.close();
}catch(Exception e){
log.error("Email Step Failed: {}", e.getMessage());
            stepRun.setStatus(StepRunStatus.FAILED);
}
    }
}
