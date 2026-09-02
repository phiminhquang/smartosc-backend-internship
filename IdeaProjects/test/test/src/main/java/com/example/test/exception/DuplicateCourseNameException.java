package com.example.test.exception;

public class DuplicateCourseNameException extends RuntimeException {
    public DuplicateCourseNameException(String message) {
        super(message);
    }
}
