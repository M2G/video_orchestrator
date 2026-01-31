package com.example.video_orchestrator.fs;

import com.example.postgresql.Queries;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class FileScanner {

    private final Queries queries;

    public FileScanner(Queries queries) {
        this.queries = queries;
    }

    public void sync(Path dir, String status) throws Exception {
        if (!Files.exists(dir)) return;

        Files.list(dir).forEach(path -> {
            try {
                queries.upsertVideoJob(path.getFileName().toString(), status);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
