package com.example.device.service.impl;

import com.example.device.service.EmailJobPermanentException;
import com.example.device.service.EmailTransport;
import com.example.device.service.ResendApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "resend")
public class ResendEmailTransport implements EmailTransport {

    private final ResendApiClient apiClient;
    private final ObjectMapper objectMapper;
    private final URI apiUri;
    private final String apiKey;
    private final String from;
    private final Set<String> allowedRecipients;

    @Autowired
    public ResendEmailTransport(
            ObjectMapper objectMapper,
            ResendApiClient apiClient,
            @Value("${app.mail.resend.api-url:https://api.resend.com/emails}") String apiUrl,
            @Value("${app.mail.resend.api-key}") String apiKey,
            @Value("${app.mail.resend.from}") String from,
            @Value("${app.mail.resend.allowed-recipients}") String allowedRecipients
    ) {
        this(
                apiClient,
                objectMapper,
                apiUrl,
                apiKey,
                from,
                allowedRecipients
        );
    }

    ResendEmailTransport(
            ResendApiClient apiClient,
            ObjectMapper objectMapper,
            String apiUrl,
            String apiKey,
            String from,
            String allowedRecipients
    ) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
        this.apiUri = validateApiUri(apiUrl);
        this.apiKey = requireText(apiKey, "Resend API key is required");
        this.from = requireText(from, "Resend sender is required");
        this.allowedRecipients = parseAllowedRecipients(allowedRecipients);
        if (this.allowedRecipients.isEmpty()) {
            throw new IllegalArgumentException("At least one Resend demo recipient is required");
        }
    }

    @Override
    public void send(UUID jobId, String recipient, String subject, String html) {
        String normalizedRecipient = normalizeRecipient(recipient);
        if (!allowedRecipients.contains(normalizedRecipient)) {
            throw new EmailJobPermanentException("Email recipient is outside the Resend demo allow-list");
        }

        byte[] requestBody;
        try {
            requestBody = objectMapper.writeValueAsBytes(new ResendRequest(
                    from,
                    List.of(recipient),
                    subject,
                    html
            ));
        } catch (Exception e) {
            throw new EmailJobPermanentException("Email job cannot be serialized", e);
        }

        int statusCode = apiClient.send(
                apiUri,
                apiKey,
                "email-job-" + jobId,
                requestBody
        );
        classifyResponse(statusCode);
    }

    private static void classifyResponse(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        if (statusCode == 408 || statusCode == 425 || statusCode == 429 || statusCode >= 500) {
            throw new IllegalStateException("Resend API temporarily rejected the email job");
        }
        throw new EmailJobPermanentException("Resend API permanently rejected the email job");
    }

    private static URI validateApiUri(String value) {
        URI uri;
        try {
            uri = URI.create(requireText(value, "Resend API URL is required"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Resend API URL is invalid", e);
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        boolean loopbackHttp = "http".equalsIgnoreCase(scheme)
                && ("localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "::1".equals(host));
        if (!("https".equalsIgnoreCase(scheme) || loopbackHttp)
                || host == null
                || uri.getUserInfo() != null
                || uri.getFragment() != null) {
            throw new IllegalArgumentException("Resend API URL must use HTTPS");
        }
        return uri;
    }

    private static Set<String> parseAllowedRecipients(String value) {
        Set<String> recipients = new LinkedHashSet<>();
        Arrays.stream(requireText(value, "Resend demo recipient is required").split(","))
                .map(ResendEmailTransport::normalizeRecipient)
                .filter(recipient -> !recipient.isEmpty())
                .forEach(recipients::add);
        return Set.copyOf(recipients);
    }

    private static String normalizeRecipient(String value) {
        return requireText(value, "Email recipient is required").toLowerCase(Locale.ROOT);
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private record ResendRequest(String from, List<String> to, String subject, String html) {
    }
}
