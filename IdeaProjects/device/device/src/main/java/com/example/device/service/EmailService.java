package com.example.device.service;

import java.util.Map;

public interface EmailService {

    void sendEmail(String to, String subject, String content);

    void sendHtmlEmail(
            String to,
            String subject,
            String templateName,
            Map<String, Object> variables
    );
}