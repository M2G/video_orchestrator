package com.example.video_orchestrator.job;

import org.springframework.stereotype.Component;

@Component
public class JobHandler {

    public void process(String filename) {
        System.out.println("Processing file: " + filename);

        if (filename.contains("fail")) {
            throw new RuntimeException("Simulated failure");
        }
    }
}
