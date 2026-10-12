package com.example.device.service;

public interface EmailJobEnqueueService {

    boolean enqueue(EmailJobEnqueueCommand command);
}
