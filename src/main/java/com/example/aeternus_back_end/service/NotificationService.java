package com.example.aeternus_back_end.service;

import com.example.aeternus_back_end.model.Inquiry;
import com.example.aeternus_back_end.model.MessageLog;
import com.example.aeternus_back_end.repository.InquiryRepository;
import com.example.aeternus_back_end.repository.MessageLogRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MessageLogRepository messageLogRepository;
    private final InquiryRepository inquiryRepository;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

    public void sendReply(String inquiryId, String message) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("Inquiry not found"));

        if (inquiry.getEmail() == null || inquiry.getEmail().isEmpty()) {
            throw new RuntimeException("Inquiry has no email address to send a reply");
        }

        sendEmail(inquiry, message);
        inquiry.setStatus("Replied_Email");
        inquiryRepository.save(inquiry);
    }

    private void sendEmail(Inquiry inquiry, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(inquiry.getEmail());
            helper.setSubject("Response from Aeternus");
            helper.setText(message, false);

            mailSender.send(mimeMessage);

            MessageLog log = MessageLog.builder()
                    .inquiry(inquiry)
                    .channel("email")
                    .recipient(inquiry.getEmail())
                    .messageBody(message)
                    .status("sent")
                    .sentAt(LocalDateTime.now())
                    .build();

            messageLogRepository.save(log);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email reply", e);
        }
    }
}
