package org.example.zenvybackend.order.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.order.dto.email.OrderCancelledEmailData;
import org.example.zenvybackend.order.dto.email.OrderPlacedEmailData;
import org.example.zenvybackend.order.dto.email.SellerOrderStatusEmailData;
import org.example.zenvybackend.order.service.OrderEmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEmailServiceImpl implements OrderEmailService {

    private final JavaMailSender mailSender;

    @Override
    @Async("mailExecutor")
    public void sendOrderPlacedEmail(OrderPlacedEmailData emailData) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailData.customerEmail());
            message.setSubject("Your Zenvy order was placed");
            message.setText(
                    "Thank you for your order.\n\n" +
                            "Order ID: " + emailData.orderId() + "\n" +
                            "Total: " + emailData.totalAmount() + "\n" +
                            "Payment: " + emailData.paymentMethod() + " (" + emailData.paymentStatus() + ")\n" +
                            "Delivery address: " + emailData.addressLine() + ", " + emailData.city()
            );
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send order placed email for order {}", emailData.orderId(), ex);
        }
    }

    @Override
    @Async("mailExecutor")
    public void sendOrderCancelledEmail(OrderCancelledEmailData emailData) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailData.customerEmail());
            message.setSubject("Your Zenvy order was cancelled");
            message.setText(
                    "Your order has been cancelled.\n\n" +
                            "Order ID: " + emailData.orderId() + "\n" +
                            "Total: " + emailData.totalAmount()
            );
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send order cancelled email for order {}", emailData.orderId(), ex);
        }
    }

    @Override
    @Async("mailExecutor")
    public void sendSellerOrderStatusEmail(SellerOrderStatusEmailData emailData) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailData.customerEmail());
            message.setSubject("Order update from " + emailData.sellerCompanyName());
            message.setText(
                    "Your order status was updated.\n\n" +
                            "Seller: " + emailData.sellerCompanyName() + "\n" +
                            "Order ID: " + emailData.orderId() + "\n" +
                            "New status: " + emailData.newStatus()
            );
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send seller order status email for seller order {}", emailData.sellerOrderId(), ex);
        }
    }
}
