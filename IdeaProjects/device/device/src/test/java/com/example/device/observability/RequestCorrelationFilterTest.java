package com.example.device.observability;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(OutputCaptureExtension.class)
class RequestCorrelationFilterTest {

    private final RequestCorrelationFilter filter = new RequestCorrelationFilter();

    @Test
    void preservesSafeRequestIdAcrossContextHeaderAndLog(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        request.addHeader(RequestCorrelation.HEADER_NAME, "client-request_123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (ignoredRequest, ignoredResponse) -> {
            assertEquals("client-request_123", RequestCorrelation.currentId());
            response.setStatus(202);
        };

        filter.doFilter(request, response, chain);

        assertEquals("client-request_123", response.getHeader(RequestCorrelation.HEADER_NAME));
        assertNull(RequestCorrelation.currentId());
        assertTrue(output.getAll().contains("event=http_request_completed request_id=client-request_123"));
        assertTrue(output.getAll().contains("path=/api/test http_status=202"));
    }

    @Test
    void replacesUnsafeRequestIdAndDoesNotLogInjectedContent(CapturedOutput output) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        request.addHeader(RequestCorrelation.HEADER_NAME, "unsafe\nforged-log=true");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) -> {
        });

        String generated = response.getHeader(RequestCorrelation.HEADER_NAME);
        assertTrue(generated != null && generated.matches("[0-9a-f-]{36}"));
        assertFalse(output.getAll().contains("forged-log=true"));
        assertNull(RequestCorrelation.currentId());
    }
}
