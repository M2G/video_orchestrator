package com.example.video_orchestrator.orchestrator;

import com.example.video_orchestrator.fs.FileScanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class OrchestratorRunner implements CommandLineRunner {

    private final OrchestratorService orchestrator;
    private final FileScanner scanner;

    public OrchestratorRunner(OrchestratorService orchestrator, FileScanner scanner) {
        this.orchestrator = orchestrator;
        this.scanner = scanner;
    }

    @Override
    public void run(String... args) throws Exception {
        scanner.sync(Path.of("/Users/matthieu.pierrelouis/Works/video_orchestrator/src/main/resources/tmp/videos/processing"), "PROCESSING");
        scanner.sync(Path.of("/Users/matthieu.pierrelouis/Works/video_orchestrator/src/main/resources/tmp/videos/done"), "DONE");
        scanner.sync(Path.of("/Users/matthieu.pierrelouis/Works/video_orchestrator/src/main/resources/tmp/videos/error"), "ERROR");

        orchestrator.runOnce();
    }
}

