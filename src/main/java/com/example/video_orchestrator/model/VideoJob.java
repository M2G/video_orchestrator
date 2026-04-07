package com.example.video_orchestrator.model;

// Représentation d’un job vidéo
public record VideoJob(
        long id,
        String filename,
        int retryCount
) {}
