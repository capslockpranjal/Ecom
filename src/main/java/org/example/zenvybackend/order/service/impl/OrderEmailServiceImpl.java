package org.example.zenvybackend.order.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.order.enums.SellerOrderStatus;
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
    public void sendOrderPlacedEmail(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getCustomer().getUser().getEmail());
            message.setSubject("Your Zenvy order was placed");
            message.setText(
                    "Thank you for your order.\n\n" +
                            "Order ID: " + order.getId() + "\n" +
                            "Total: " + order.getTotalAmount() + "\n" +
                            "Payment: " + order.getPaymentMethod() + " (" + order.getPaymentStatus() + ")\n" +
                            "Delivery address: " + order.getAddressLine() + ", " + order.getCity()
            );
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send order placed email for order {}", order.getId(), ex);
        }
    }

    @Override
    @Async("mailExecutor")
    public void sendOrderCancelledEmail(Order order) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(order.getCustomer().getUser().getEmail());
            message.setSubject("Your Zenvy order was cancelled");
            message.setText(
                    "Your order has been cancelled.\n\n" +
                            "Order ID: " + order.getId() + "\n" +
                            "Total: " + order.getTotalAmount()
            );
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send order cancelled email for order {}", order.getId(), ex);
        }
    }

    @Override
    @Async("mailExecutor")
    public void sendSellerOrderStatusEmail(SellerOrder sellerOrder, SellerOrderStatus newStatus) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(sellerOrder.getOrder().getCustomer().getUser().getEmail());
            message.setSubject("Order update from " + sellerOrder.getSeller().getCompanyName());
            message.setText(
                    "Your order status was updated.\n\n" +
                            "Seller: " + sellerOrder.getSeller().getCompanyName() + "\n" +
                            "Order ID: " + sellerOrder.getOrder().getId() + "\n" +
                            "New status: " + newStatus
            );
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send seller order status email for seller order {}", sellerOrder.getId(), ex);
        }
    }
}
