package org.example.zenvybackend.order.dto.email;

import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.order.enums.SellerOrderStatus;

import java.util.UUID;

public record SellerOrderStatusEmailData(
        String customerEmail,
        UUID sellerOrderId,
        UUID orderId,
        String sellerCompanyName,
        SellerOrderStatus newStatus
) {
    public static SellerOrderStatusEmailData from(SellerOrder sellerOrder, SellerOrderStatus newStatus) {
        return new SellerOrderStatusEmailData(
                sellerOrder.getOrder().getCustomer().getUser().getEmail(),
                sellerOrder.getId(),
                sellerOrder.getOrder().getId(),
                sellerOrder.getSeller().getCompanyName(),
                newStatus
        );
    }
}
