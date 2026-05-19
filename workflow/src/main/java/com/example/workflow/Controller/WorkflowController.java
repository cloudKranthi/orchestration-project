package com.example.workflow.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.workflow.dto.WorkflowRunResponse;
import com.example.workflow.dto.WorkflowStepRequest;
import com.example.workflow.dto.WorkflowStepResponse;
import com.example.workflow.dto.WorkflowCreateRequest;
import com.example.workflow.dto.WorkflowRunRequest;
import com.example.workflow.dto.WorkflowMainResponse;
import com.example.workflow.Repository.WorkflowRepository;
import com.example.workflow.Repository.WorkflowRunRepository;
import com.example.workflow.model.UserEntity;
import com.example.workflow.model.WorkflowEntity;
import com.example.workflow.model.WorkflowRun;
import com.example.workflow.model.WorkflowStep;
import com.example.workflow.service.WorkflowCreationService;
import com.example.workflow.service.WorkflowExecutionService;

import io.micrometer.common.lang.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/user/workflow")
public class WorkflowController {
    private final WorkflowCreationService workflowCreationservice;
    private final WorkflowExecutionService workflowExecutionService;
    private final WorkflowRunRepository workflowRunRepository;
    private final WorkflowRepository workflowRepository;

    @PostMapping("/create-workflow")
    public ResponseEntity<?> createWorkflow(@RequestBody @NonNull WorkflowCreateRequest request) {
        try {
            WorkflowEntity workflow = workflowCreationservice.createWorkflow(
                request.email(), 
                request.name(), 
                request.description()
            );
            WorkflowMainResponse response=new WorkflowMainResponse(
                workflow.getUser().getEmail(),
                workflow.getWorkflowName(),
                workflow.getDescription()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/create-workflow-step")
    public ResponseEntity<?> createWorkflowStep(@RequestBody @NonNull WorkflowStepRequest req) {
        try {
            // Using the 'req' record getters explicitly fixes the un-findable variables
            WorkflowStep workflowStep = workflowCreationservice.createWorkflowStep(
                req.workflowName(), 
                req.on_true_step_key(), 
                req.on_false_step_key(), 
                req.stepKey(),
                req.isCritical(), 
                req.config_json(), 
                req.timeoutSeconds(), 
                req.attemptCount(), 
                req.stepOrder(), 
                req.depands_on(),
                req.stepType()
            );
        
            
            WorkflowStepResponse response = new WorkflowStepResponse(
    workflowStep.getWorkflow().getWorkflowName(),
    workflowStep.getStepkey(),
    workflowStep.getStepOrder(),
    workflowStep.getStepType(), // FIX: Pass your StepType enum getter here instead of status!
    workflowStep.getConfigJson(),
    workflowStep.getDepands_on()
);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/create-workflow-Run")
    public ResponseEntity<?> createWorkflowRun(@RequestBody @NonNull WorkflowRunRequest request) {
        try {
            
            WorkflowRun workflowRun = workflowCreationservice.createWorkflowRun(
                request.TriggerSource(), 
               request.TriggerPayload(),
                request.workflowName()
            );
            WorkflowRunResponse response = new WorkflowRunResponse(
                workflowRun.getWorkflow().getWorkflowName(),
                workflowRun.getTriggerSource(),
                workflowRun.getContextJson(),
                workflowRun.getCurrentstep().getStepkey(),
                workflowRun.getTriggerPayload(),
                workflowRun.getStatus()
            );
            

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/execute-workflow-Run")
    public ResponseEntity<String> executeWorkflow(@RequestBody @NonNull WorkflowRunRequest request) {
        try {
            String cleanWorkflowName = (request.workflowName() != null) ? request.workflowName().trim().replace("\r", "") : "";
            String email;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof UserEntity) {
            email = ((UserEntity) principal).getEmail();
        } else {
            email = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        
        // 3. CLEAN EMAIL GHOST STRINGS
        String cleanEmail = (email != null) ? email.trim().replace("\r", "") : "";
            WorkflowEntity workflow = workflowRepository.findByWorkflowNameAndUser_Email(cleanWorkflowName, cleanEmail);
            if (workflow == null) {
                throw new RuntimeException("No such Workflow present");
            }
            WorkflowRun workflowRun = workflowRunRepository.findByWorkflowAndTriggerSource(workflow, request.TriggerSource());
            if (workflowRun == null) {
                throw new RuntimeException("No such WorkflowRun present");
            }
            
            workflowExecutionService.executeSteps(workflowRun);
            return ResponseEntity.ok("Workflow execution service initiated");
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/get-workflow-Run")
    public ResponseEntity<?> getWorkflowRun(@RequestBody @NonNull WorkflowRunRequest request) {
        try {
            String cleanWorkflowName = (request.workflowName() != null) ? request.workflowName().trim().replace("\r", "") : "";
            String email;
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof UserEntity) {
            email = ((UserEntity) principal).getEmail();
        } else {
            email = SecurityContextHolder.getContext().getAuthentication().getName();
        }
        
        // 3. CLEAN EMAIL GHOST STRINGS
        String cleanEmail = (email != null) ? email.trim().replace("\r", "") : "";
            
            WorkflowEntity workflow = workflowRepository.findByWorkflowNameAndUser_Email(cleanWorkflowName, cleanEmail);
            if (workflow == null) {
                throw new RuntimeException("No such Workflow present");
            }
            WorkflowRun run = workflowRunRepository.findByWorkflowAndTriggerSource(workflow, request.TriggerSource());
            if (run == null) {
                throw new RuntimeException("No such WorkflowRun present");
            }
            
            // Map internal engine entity to our clean Response DTO format
            WorkflowRunResponse response = new WorkflowRunResponse(
                run.getWorkflow().getWorkflowName(),
                run.getTriggerSource(),
                run.getContextJson(),
                
                run.getTriggerPayload(),
                run.getCurrentstep().getStepkey(),
                run.getStatus()
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}