package com.example.device.service;

public class EmailJobPermanentException extends RuntimeException {

    public EmailJobPermanentException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmailJobPermanentException(String message) {
        super(message);
    }
}
