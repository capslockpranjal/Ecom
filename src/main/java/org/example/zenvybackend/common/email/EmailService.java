package org.example.zenvybackend.common.email;

public interface EmailService {

    void sendEmail(String to, String subject, String body);
}
