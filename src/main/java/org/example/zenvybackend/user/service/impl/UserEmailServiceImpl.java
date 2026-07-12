package org.example.zenvybackend.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.common.email.EmailService;
import org.example.zenvybackend.user.service.UserEmailService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEmailServiceImpl implements UserEmailService {

    private final EmailService emailService;

    @Override
    @Async("mailExecutor")
    public void sendEmail(String to, String subject, String body) {
        try {
            emailService.sendEmail(to, subject, body);
        } catch (Exception ex) {
            log.error("Failed to send email to {}", to, ex);
        }
    }
}
