package com.example.device.exception;

import com.example.device.observability.RequestCorrelation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class GlobalExceptionHandlerLoggingTest {

    @Test
    void unexpectedFailureLogsAllowListedMetadataWithoutExceptionMessage(CapturedOutput output) {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        RuntimeException exception = new RuntimeException(
                "password=NeverLog jwt=eyJhbGciOiJIUzI1NiJ9 raw-reset-token"
        );

        try (RequestCorrelation.Scope ignored = RequestCorrelation.open("request-error-123")) {
            var response = handler.handleException(exception);
            assertTrue(response.getBody() != null
                    && "request-error-123".equals(response.getBody().getRequestId()));
        }

        String logs = output.getAll();
        assertTrue(logs.contains("event=api_request_failed"));
        assertTrue(logs.contains("request_id=request-error-123"));
        assertTrue(logs.contains("error_code=9999"));
        assertTrue(logs.contains("http_status=500"));
        assertTrue(logs.contains("exception_type=RuntimeException"));
        assertFalse(logs.contains("NeverLog"));
        assertFalse(logs.contains("eyJhbGciOiJIUzI1NiJ9"));
        assertFalse(logs.contains("raw-reset-token"));
    }
}
