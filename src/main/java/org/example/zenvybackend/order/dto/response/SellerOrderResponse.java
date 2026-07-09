package org.example.zenvybackend.order.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.zenvybackend.order.enums.SellerOrderStatus;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class SellerOrderResponse {

    private UUID id;
    private UUID sellerId;
    private String sellerCompanyName;
    private SellerOrderStatus status;
    private Double subtotal;
    private List<OrderItemResponse> items;
}
