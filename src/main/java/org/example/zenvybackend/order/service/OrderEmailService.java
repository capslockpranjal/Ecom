package org.example.zenvybackend.order.service;

import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.order.enums.SellerOrderStatus;

public interface OrderEmailService {

    void sendOrderPlacedEmail(Order order);

    void sendOrderCancelledEmail(Order order);

    void sendSellerOrderStatusEmail(SellerOrder sellerOrder, SellerOrderStatus newStatus);
}
