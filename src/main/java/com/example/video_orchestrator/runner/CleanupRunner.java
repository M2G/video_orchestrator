package com.example.video_orchestrator.runner;

import com.example.video_orchestrator.repository.SqlcVideoJobRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CleanupRunner {

    private final SqlcVideoJobRepository repository;

    public CleanupRunner(SqlcVideoJobRepository repository) {
        this.repository = repository;
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanup() {
        repository.resetStuckJobs();
    }
}