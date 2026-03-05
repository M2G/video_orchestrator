package com.example.video_orchestrator.model;

import com.example.postgresql.Queries.LockAndMarkProcessingRow;


public record VideoJob(
        long id,
        String filename,
        int retryCount
) {
    public static VideoJob from(LockAndMarkProcessingRow row) {
        return new VideoJob(
                row.id(),
                row.filename(),
                row.retryCount()
        );
    }
}
