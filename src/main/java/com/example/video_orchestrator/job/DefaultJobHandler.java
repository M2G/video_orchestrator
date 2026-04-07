package com.example.video_orchestrator.job;

import com.example.video_orchestrator.model.VideoJob;
import org.springframework.stereotype.Component;

@Component
public class DefaultJobHandler implements JobHandler {


    // Implémentation simple (ex: simulation)
    @Override
    public void handle(VideoJob job) throws Exception {

        // TODO: remplacer par ffmpeg / traitement réel
        Thread.sleep(500);
    }
}