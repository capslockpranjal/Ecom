package org.example.zenvybackend.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.zenvybackend.order.enums.SellerOrderStatus;

@Data
public class UpdateSellerOrderStatusRequest {

    @NotNull
    private SellerOrderStatus status;
}
