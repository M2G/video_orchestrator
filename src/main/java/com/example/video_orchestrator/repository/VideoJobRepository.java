package com.example.video_orchestrator.repository;

import com.example.video_orchestrator.model.VideoJob;

import java.util.List;

public interface VideoJobRepository {
    List<VideoJob> lockNextJobs(int limit, int maxRetry);
    void markProcessing(long id);
    void markDone(long id);
    void markRetry(long id, int delaySeconds);
    void markFailed(long id);
}