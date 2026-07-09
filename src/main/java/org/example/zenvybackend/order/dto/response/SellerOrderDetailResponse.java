package org.example.zenvybackend.order.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.zenvybackend.order.enums.PaymentMethod;
import org.example.zenvybackend.order.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SellerOrderDetailResponse {

    private UUID sellerOrderId;
    private UUID orderId;
    private SellerOrderResponse sellerOrder;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String addressLine;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String label;
    private LocalDateTime orderCreatedAt;
}
