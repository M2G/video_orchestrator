package com.example.video_orchestrator.runner;

import com.example.video_orchestrator.services.OrchestratorService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrchestratorRunner {

    private final OrchestratorService orchestrator;

    public OrchestratorRunner(OrchestratorService orchestrator) {
        this.orchestrator = orchestrator;
    }

    @Scheduled(fixedDelay = 1000)
    public void tick() {
        orchestrator.runOnce();
    }
}




