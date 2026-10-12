package com.example.device.service.impl;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpEmailTransportTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void usesStableMessageIdDerivedFromJobId() throws Exception {
        UUID jobId = UUID.fromString("6ff2fe33-553d-48c3-8023-78bce074f815");
        MimeMessage baseMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(baseMessage);
        SmtpEmailTransport transport = new SmtpEmailTransport(mailSender);
        ReflectionTestUtils.setField(transport, "fromEmail", "no-reply@device.test");

        transport.send(
                jobId,
                "recipient@device.test",
                "Reminder",
                "<p>Reminder</p>"
        );

        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage sentMessage = messageCaptor.getValue();
        sentMessage.saveChanges();
        assertEquals(
                "<email-job-6ff2fe33-553d-48c3-8023-78bce074f815@device.local>",
                sentMessage.getMessageID()
        );
    }
}
