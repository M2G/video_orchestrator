package com.example.video_orchestrator.job;

import com.example.postgresql.Queries.LockNextJobsRow;


public record VideoJob(
        long id,
        String filename,
        int retryCount
) {
    public static VideoJob from(LockNextJobsRow row) {
        return new VideoJob(
                row.id(),
                row.filename(),
                row.retryCount()
        );
    }
}
