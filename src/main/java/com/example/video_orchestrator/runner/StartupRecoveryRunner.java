package com.example.video_orchestrator.runner;

import com.example.video_orchestrator.repository.SqlcVideoJobRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupRecoveryRunner implements CommandLineRunner {

    private final SqlcVideoJobRepository repository;

    public StartupRecoveryRunner(SqlcVideoJobRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        repository.resetStuckJobs();
    }
}