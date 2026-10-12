package com.example.device.service;

import java.util.UUID;

public interface EmailTransport {

    void send(UUID jobId, String recipient, String subject, String html);
}
