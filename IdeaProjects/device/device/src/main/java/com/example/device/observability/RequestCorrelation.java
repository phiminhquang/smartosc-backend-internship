package com.example.device.observability;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

public final class RequestCorrelation {

    public static final String HEADER_NAME = "X-Request-ID";
    public static final String MDC_KEY = "request_id";
    public static final String NO_REQUEST_ID = "none";

    private static final Pattern SAFE_REQUEST_ID =
            Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,63}");

    private RequestCorrelation() {
    }

    public static String resolve(String candidate) {
        return candidate != null && SAFE_REQUEST_ID.matcher(candidate).matches()
                ? candidate
                : UUID.randomUUID().toString();
    }

    public static String currentId() {
        return MDC.get(MDC_KEY);
    }

    public static String forLog(String requestId) {
        return requestId == null ? NO_REQUEST_ID : requestId;
    }

    public static Scope open(String requestId) {
        String previous = MDC.get(MDC_KEY);
        if (requestId == null) {
            MDC.remove(MDC_KEY);
        } else {
            MDC.put(MDC_KEY, requestId);
        }
        return () -> {
            if (previous == null) {
                MDC.remove(MDC_KEY);
            } else {
                MDC.put(MDC_KEY, previous);
            }
        };
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {

        @Override
        void close();
    }
}
