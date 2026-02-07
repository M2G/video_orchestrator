package com.example.video_orchestrator.job;

import com.example.video_orchestrator.model.VideoJob;
import org.springframework.stereotype.Component;

@Component
public class DefaultJobHandler implements JobHandler {

    @Override
    public void handle(VideoJob job) throws Exception {
        // traitement réel
    }
}