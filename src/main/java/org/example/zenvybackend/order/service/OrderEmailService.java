package org.example.zenvybackend.order.service;

import org.example.zenvybackend.order.dto.email.OrderCancelledEmailData;
import org.example.zenvybackend.order.dto.email.OrderPlacedEmailData;
import org.example.zenvybackend.order.dto.email.SellerOrderStatusEmailData;

public interface OrderEmailService {

    void sendOrderPlacedEmail(OrderPlacedEmailData emailData);

    void sendOrderCancelledEmail(OrderCancelledEmailData emailData);

    void sendSellerOrderStatusEmail(SellerOrderStatusEmailData emailData);
}
