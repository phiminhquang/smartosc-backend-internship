package com.example.device.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.device.dto.response.ApiResponse;
import com.example.device.observability.RequestCorrelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(RateLimitExceededException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn(
                "event=api_request_rejected request_id={} error_code={} http_status={} exception_type={} retry_after_seconds={}",
                requestIdForLog(),
                errorCode.getCode(),
                errorCode.getHttpStatus().value(),
                exception.getClass().getSimpleName(),
                exception.getRetryAfterSeconds()
        );
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .requestId(RequestCorrelation.currentId())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatus())
                .header(HttpHeaders.RETRY_AFTER, Long.toString(exception.getRetryAfterSeconds()))
                .body(response);
    }

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.info(
                "event=api_request_rejected request_id={} error_code={} http_status={} exception_type={}",
                requestIdForLog(),
                errorCode.getCode(),
                errorCode.getHttpStatus().value(),
                exception.getClass().getSimpleName()
        );
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .requestId(RequestCorrelation.currentId())
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
                .map(e -> e.getDefaultMessage())
                .orElse(ErrorCode.INVALID_KEY.name());

        ErrorCode errorCode;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
        } catch (IllegalArgumentException e) {
            errorCode = ErrorCode.INVALID_KEY;
        }

        log.info(
                "event=api_request_rejected request_id={} error_code={} http_status={} exception_type={}",
                requestIdForLog(),
                errorCode.getCode(),
                errorCode.getHttpStatus().value(),
                exception.getClass().getSimpleName()
        );

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .requestId(RequestCorrelation.currentId())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        ErrorCode errorCode = ErrorCode.INVALID_PAGINATION_REQUEST;
        log.info(
                "event=api_request_rejected request_id={} error_code={} http_status={} exception_type={}",
                requestIdForLog(),
                errorCode.getCode(),
                errorCode.getHttpStatus().value(),
                exception.getClass().getSimpleName()
        );
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .requestId(RequestCorrelation.currentId())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(
            MaxUploadSizeExceededException exception
    ) {
        ErrorCode errorCode = ErrorCode.FILE_TOO_LARGE;
        log.info(
                "event=api_request_rejected request_id={} error_code={} http_status={} exception_type={}",
                requestIdForLog(),
                errorCode.getCode(),
                errorCode.getHttpStatus().value(),
                exception.getClass().getSimpleName()
        );
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .requestId(RequestCorrelation.currentId())
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
        log.error(
                "event=api_request_failed request_id={} error_code={} http_status={} exception_type={}",
                requestIdForLog(),
                errorCode.getCode(),
                errorCode.getHttpStatus().value(),
                exception.getClass().getSimpleName()
        );

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .requestId(RequestCorrelation.currentId())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLock(
            ObjectOptimisticLockingFailureException exception) {

        ErrorCode errorCode = ErrorCode.DEVICE_CONCURRENTLY_MODIFIED;
        log.warn(
                "event=api_request_conflict request_id={} error_code={} http_status={} exception_type={}",
                requestIdForLog(),
                errorCode.getCode(),
                errorCode.getHttpStatus().value(),
                exception.getClass().getSimpleName()
        );

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .requestId(RequestCorrelation.currentId())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    private String requestIdForLog() {
        return RequestCorrelation.forLog(RequestCorrelation.currentId());
    }

}
