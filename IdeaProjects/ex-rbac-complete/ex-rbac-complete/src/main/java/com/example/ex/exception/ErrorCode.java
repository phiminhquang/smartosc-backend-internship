package com.example.ex.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid validation key", HttpStatus.BAD_REQUEST),

    EMAIL_EXISTED(1002, "Email already exists", HttpStatus.CONFLICT),
    EMAIL_INVALID(1003, "Email is invalid", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User does not exist", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Email or password is incorrect", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),

    ROLE_NOT_EXISTED(1008, "Role does not exist", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(1009, "Role already exists", HttpStatus.CONFLICT),
    ROLE_NAME_INVALID(1010, "Role name is invalid", HttpStatus.BAD_REQUEST),
    NAME_INVALID(1011, "Name must not be blank and must be at most 100 characters", HttpStatus.BAD_REQUEST),
    TOKEN_INVALID(1012, "Token must not be blank", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
