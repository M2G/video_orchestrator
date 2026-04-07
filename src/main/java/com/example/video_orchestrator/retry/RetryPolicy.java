package com.example.video_orchestrator.retry;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@Component
public class RetryPolicy {

    // délai de base
    private static final int BASE = 5;
    // délai max (cap)
    private static final int MAX = 300;

    // Calcule le délai de retry
    public int nextDelaySeconds(int retryCount) {
        // backoff exponentiel
        int delay = (int) Math.pow(2, retryCount) * BASE;
        // limite max
        delay = Math.min(delay, MAX);

        // jitter aléatoire entre 0 et 5 secondes
        delay += ThreadLocalRandom.current().nextInt(0, 5);
        return delay;
    }
}