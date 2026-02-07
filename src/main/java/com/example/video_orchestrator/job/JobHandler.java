package com.example.video_orchestrator.job;

import com.example.video_orchestrator.model.VideoJob;

public interface JobHandler {
    void handle(VideoJob job) throws Exception;
}
