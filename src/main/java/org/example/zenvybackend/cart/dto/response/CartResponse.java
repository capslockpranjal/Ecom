package org.example.zenvybackend.cart.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CartResponse {

    private UUID id;
    private List<CartItemResponse> items;
    private Double subtotal;
    private Integer itemCount;
}
