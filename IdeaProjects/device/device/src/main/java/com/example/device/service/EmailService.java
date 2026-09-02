package com.example.device.service;

public interface EmailService {

    void sendEmail(String to, String subject, String content);
}