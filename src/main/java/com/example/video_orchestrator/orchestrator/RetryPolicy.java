package com.example.video_orchestrator.orchestrator;

import org.springframework.stereotype.Component;

@Component
public class RetryPolicy {

    public int nextDelay(int retryCount) {
        return Math.min(60 * retryCount, 300);
    }

    public int maxRetry() {
        return 5;
    }
}
