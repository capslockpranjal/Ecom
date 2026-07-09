package org.example.zenvybackend.order.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.order.dto.response.ReturnRequestResponse;
import org.example.zenvybackend.order.entity.ReturnRequest;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ReturnMapper {

    private final ObjectMapper objectMapper;

    public ReturnRequestResponse toResponse(ReturnRequest request) {
        return ReturnRequestResponse.builder()
                .id(request.getId())
                .orderItemId(request.getOrderItem().getId())
                .orderId(request.getSellerOrder().getOrder().getId())
                .sellerOrderId(request.getSellerOrder().getId())
                .productName(request.getOrderItem().getProductName())
                .brand(request.getOrderItem().getBrand())
                .metadata(parseMetadata(request.getOrderItem().getMetadata()))
                .quantity(request.getQuantity())
                .reason(request.getReason())
                .status(request.getStatus())
                .sellerCompanyName(request.getSellerOrder().getSeller().getCompanyName())
                .createdAt(request.getCreatedAt())
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseMetadata(String metadata) {
        try {
            return objectMapper.readValue(metadata, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing return item metadata");
        }
    }
}
