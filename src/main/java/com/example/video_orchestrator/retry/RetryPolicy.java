package com.example.video_orchestrator.retry;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class RetryPolicy {

    private static final int BASE = 5;
    private static final int MAX = 300;

    public int nextDelaySeconds(int retryCount) {
        int delay = (int) Math.pow(2, retryCount) * BASE;
        delay = Math.min(delay, MAX);

        // jitter anti-avalanche
        delay += ThreadLocalRandom.current().nextInt(0, 5);
        return delay;
    }
}