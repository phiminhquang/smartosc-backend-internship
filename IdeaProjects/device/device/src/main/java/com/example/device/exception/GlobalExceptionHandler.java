package com.example.device.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.device.dto.response.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

@ExceptionHandler(AppException.class)
public  ResponseEntity<ApiResponse<Void>> handleAppexception(AppException exception){
    ErrorCode errorCode = exception.getErrorCode();
    ApiResponse<Void> response = ApiResponse.<Void>builder()
            .code(errorCode.getCode())
            .message(errorCode.getMessage())
            .build();
    return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        String enumKey = exception.getFieldErrors()
                .stream()
                .findFirst()
                .map( e -> e.getDefaultMessage())
                .orElse(ErrorCode.INVALID_KEY.name());

        ErrorCode errorCode ;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        }catch (IllegalArgumentException e){
            errorCode = ErrorCode.INVALID_KEY;
        }

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(
            Exception exception
    ) {
        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_EXCEPTION;

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }
}