package com.example.aeternus_back_end.controller;

import com.example.aeternus_back_end.model.Inquiry;
import com.example.aeternus_back_end.repository.InquiryRepository;
import com.example.aeternus_back_end.service.NotificationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryRepository inquiryRepository;
    private final NotificationService notificationService;
    private static final Logger log = LoggerFactory.getLogger(InquiryController.class);

    // Public submission endpoint
    @PostMapping
    public ResponseEntity<Inquiry> submitInquiry(@RequestBody Inquiry inquiry) {
        inquiry.setDate(LocalDateTime.now());
        inquiry.setStatus("Pending");
        return ResponseEntity.ok(inquiryRepository.save(inquiry));
    }

    // Admin endpoints
    @GetMapping
    public ResponseEntity<List<Inquiry>> getAllInquiries() {
        return ResponseEntity.ok(inquiryRepository.findAll());
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<?> sendReply(@PathVariable String id, @RequestBody ReplyRequest request) {
        try {
            notificationService.sendReply(id, request.getMessage());
            return ResponseEntity.ok(Map.of("message", "Reply sent successfully"));
        } catch (Exception e) {
            log.error("Error while sending reply for inquiry {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Internal server error",
                    "details", e.getMessage()
            ));
        }
    }
}

@Data
class ReplyRequest {
    private String message;
}
