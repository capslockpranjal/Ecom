package org.example.zenvybackend.user.service;

public interface UserEmailService {

    void sendEmail(String to, String subject, String body);
}
