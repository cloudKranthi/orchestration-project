package com.example.workflow;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableScheduling
@EnableAsync
@SpringBootApplication
public class WorkflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkflowApplication.class, args);
		System.out.println("------------------------------------");
        System.out.println("🚀 Orchestaton Workflow  ENGINE IS LIVE!");
        System.out.println("------------------------------------");
	}

}
