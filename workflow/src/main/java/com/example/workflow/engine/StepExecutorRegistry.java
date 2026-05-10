package com.example.workflow.engine;
import com.example.workflow.model.StepType;
import java.util.EnumMap;
import jakarta.annotation.PostConstruct;

import java.util.Map;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class StepExecutorRegistry {
    private final DelayStepExecutor delayStepExecutor;
    private final EmailSendStepExecutor emailSendStepExecutor;
    private final EmailCheckExecutor emailCheckExecutor;
    private final HttpStepExecutor httpStepExecutor;
    private  final Map<StepType,StepExecutor> map= new EnumMap<>(StepType.class);

    @PostConstruct
    public void init(){
    map.put(StepType.EMAIL_SEND,emailSendStepExecutor);
    map.put(StepType.DELAY,delayStepExecutor);
    map.put(StepType.HTTP_CALL,httpStepExecutor);
    map.put(StepType.EMAIL_CHECK,emailCheckExecutor);
    }
    public StepExecutor getExecutor(StepType type){
        return map.get(type);
    }
}
