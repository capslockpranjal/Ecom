package org.example.zenvybackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.zenvybackend.order.enums.PaymentMethod;

import java.util.UUID;

@Data
public class CheckoutRequest {

    @NotNull
    private UUID addressId;

    @NotNull
    private PaymentMethod paymentMethod;
}
