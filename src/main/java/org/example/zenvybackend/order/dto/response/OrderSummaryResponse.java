package org.example.zenvybackend.order.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.zenvybackend.order.enums.OrderStatus;
import org.example.zenvybackend.order.enums.PaymentMethod;
import org.example.zenvybackend.order.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrderSummaryResponse {

    private UUID id;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private Double totalAmount;
    private LocalDateTime createdAt;
    private Integer sellerOrderCount;
}
