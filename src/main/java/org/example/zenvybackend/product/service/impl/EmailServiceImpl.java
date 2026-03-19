package org.example.zenvybackend.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${admin.email}")
    private String adminEmail;

    @Override
    public void sendProductCreatedEmail(Product product) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(adminEmail);
        message.setSubject("New Product Created");

        message.setText(
                "A new product has been created:\n\n" +
                        "Name: " + product.getName() + "\n" +
                        "Brand: " + product.getBrand() + "\n" +
                        "Category: " + product.getCategory().getName() + "\n" +
                        "Seller: " + product.getSeller().getUser().getEmail()
        );

        mailSender.send(message);
    }
}
