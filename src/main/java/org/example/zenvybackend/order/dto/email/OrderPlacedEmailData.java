package org.example.zenvybackend.order.dto.email;

import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.order.enums.PaymentMethod;
import org.example.zenvybackend.order.enums.PaymentStatus;

import java.util.UUID;

public record OrderPlacedEmailData(
        String customerEmail,
        UUID orderId,
        Double totalAmount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String addressLine,
        String city
) {
    public static OrderPlacedEmailData from(Order order) {
        return new OrderPlacedEmailData(
                order.getCustomer().getUser().getEmail(),
                order.getId(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getAddressLine(),
                order.getCity()
        );
    }
}
