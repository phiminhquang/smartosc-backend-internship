package com.example.device.exception;

import lombok.Data;
import lombok.Getter;

@Data

public class AppException extends RuntimeException {
    private ErrorCode errorCode;
    public AppException(ErrorCode errorCode) {

        super(errorCode.getMessage());
        this.errorCode = errorCode;

    }
}
