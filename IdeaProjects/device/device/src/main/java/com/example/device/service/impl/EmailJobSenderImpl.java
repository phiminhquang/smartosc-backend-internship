package com.example.device.service.impl;

import com.example.device.service.EmailJobDelivery;
import com.example.device.service.EmailJobPermanentException;
import com.example.device.service.EmailJobSecretCipher;
import com.example.device.service.EmailJobSender;
import com.example.device.service.EmailTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateEngineException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailJobSenderImpl implements EmailJobSender {

    private static final TypeReference<Map<String, Object>> PAYLOAD_TYPE = new TypeReference<>() {
    };

    private final EmailTransport emailTransport;
    private final TemplateEngine templateEngine;
    private final ObjectMapper objectMapper;
    private final EmailJobSecretCipher secretCipher;

    @Value("${app.password-reset.base-url}")
    private String resetBaseUrl;

    @Override
    public void send(EmailJobDelivery delivery) {
        Map<String, Object> variables = prepareVariables(delivery);
        String html = renderTemplate(delivery.templateKey(), variables);
        emailTransport.send(
                delivery.id(),
                delivery.recipient(),
                delivery.subject(),
                html
        );
    }

    private Map<String, Object> prepareVariables(EmailJobDelivery delivery) {
        Map<String, Object> variables = new LinkedHashMap<>(readPayload(delivery.payload()));
        if (delivery.jobType() == com.example.device.enums.EmailJobType.PASSWORD_RESET) {
            String rawToken = secretCipher.decrypt(
                    delivery.jobType(),
                    delivery.secretCiphertext(),
                    delivery.secretNonce(),
                    delivery.secretKeyVersion()
            );
            String resetLink = UriComponentsBuilder.fromUriString(resetBaseUrl)
                    .queryParam("token", rawToken)
                    .build(true)
                    .toUriString();
            variables.put("resetLink", resetLink);
        }
        return variables;
    }

    private Map<String, Object> readPayload(String payload) {
        try {
            return objectMapper.readValue(payload, PAYLOAD_TYPE);
        } catch (Exception e) {
            throw new EmailJobPermanentException("Email job payload is invalid", e);
        }
    }

    private String renderTemplate(String templateKey, Map<String, Object> variables) {
        try {
            Context context = new Context();
            context.setVariables(variables);
            return templateEngine.process(templateKey, context);
        } catch (TemplateEngineException e) {
            throw new EmailJobPermanentException("Email job template is invalid", e);
        }
    }

}
