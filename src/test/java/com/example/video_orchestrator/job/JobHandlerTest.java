package com.example.video_orchestrator.job;

import com.example.video_orchestrator.model.VideoJob;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class JobHandlerTest {

    @Test
    void shouldProcessJobWithoutError() throws Exception {
        JobHandler handler = new DefaultJobHandler();
        VideoJob job = new VideoJob(1L, "video.mp4", 0);

        handler.handle(job); // méthode existe
    }
}


