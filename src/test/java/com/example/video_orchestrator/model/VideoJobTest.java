package com.example.video_orchestrator.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class VideoJobTest {

    @Test
    void retryCountCannotBeNegative() {
        VideoJob job = new VideoJob(1L, "video.mp4", -1);
        assertTrue(job.retryCount() >= 0);
    }
}
