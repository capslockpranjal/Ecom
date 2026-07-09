package org.example.zenvybackend.order.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.zenvybackend.order.enums.OrderStatus;
import org.example.zenvybackend.order.enums.PaymentMethod;
import org.example.zenvybackend.order.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderResponse {

    private UUID id;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Double totalAmount;
    private String addressLine;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private String label;
    private LocalDateTime createdAt;
    private Boolean cancellable;
    private List<SellerOrderResponse> sellerOrders;
}
