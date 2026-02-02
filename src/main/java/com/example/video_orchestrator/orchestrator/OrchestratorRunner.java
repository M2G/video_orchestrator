package com.example.video_orchestrator.orchestrator;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class OrchestratorRunner {

    private final OrchestratorService orchestratorService;

    public OrchestratorRunner(OrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @Scheduled(fixedDelay = 5000)
    public void run() throws SQLException {
        orchestratorService.runOnce();
    }
}
