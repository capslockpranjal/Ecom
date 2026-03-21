package org.example.zenvybackend.product.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.service.ProductEmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEmailServiceImpl implements ProductEmailService {

    private final JavaMailSender mailSender;

    @Value("${admin.email}")
    private String adminEmail;

    @Override
    @Async("mailExecutor")
    public void sendProductCreatedEmail(Product product) {
        try {
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
        } catch (Exception ex) {
            log.error("Failed to send product created email for product {}", product.getId(), ex);
        }
    }
}
