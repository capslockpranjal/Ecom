package org.example.zenvybackend.order.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class PaymentSessionResponse {

    private UUID orderId;
    private String paymentStatus;
    private String paymentMethod;
    private boolean requiresPayment;
    private String message;
}
