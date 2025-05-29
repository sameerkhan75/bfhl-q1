package com.example.bfhl.dto;

public record WebhookResponse(
    String webhook,
    String accessToken
) { }