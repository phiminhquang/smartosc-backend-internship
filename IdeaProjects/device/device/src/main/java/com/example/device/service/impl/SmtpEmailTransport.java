package com.example.device.service.impl;

import com.example.device.service.EmailJobPermanentException;
import com.example.device.service.EmailTransport;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "smtp", matchIfMissing = true)
public class SmtpEmailTransport implements EmailTransport {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Override
    public void send(UUID jobId, String recipient, String subject, String html) {
        try {
            MimeMessage baseMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(baseMessage, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(html, true);
            MimeMessage message = new StableMessageIdMimeMessage(
                    baseMessage,
                    "<email-job-" + jobId + "@device.local>"
            );
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailJobPermanentException("Email job contains an invalid address", e);
        }
    }

    private static final class StableMessageIdMimeMessage extends MimeMessage {

        private final String stableMessageId;

        private StableMessageIdMimeMessage(MimeMessage source, String stableMessageId)
                throws MessagingException {
            super(source);
            this.stableMessageId = stableMessageId;
        }

        @Override
        protected void updateMessageID() throws MessagingException {
            setHeader("Message-ID", stableMessageId);
        }
    }
}
