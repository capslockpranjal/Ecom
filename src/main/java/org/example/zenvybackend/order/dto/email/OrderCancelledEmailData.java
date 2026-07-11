package org.example.zenvybackend.order.dto.email;

import org.example.zenvybackend.order.entity.Order;

import java.util.UUID;

public record OrderCancelledEmailData(
        String customerEmail,
        UUID orderId,
        Double totalAmount
) {
    public static OrderCancelledEmailData from(Order order) {
        return new OrderCancelledEmailData(
                order.getCustomer().getUser().getEmail(),
                order.getId(),
                order.getTotalAmount()
        );
    }
}
