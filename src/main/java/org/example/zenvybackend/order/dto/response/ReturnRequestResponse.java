package org.example.zenvybackend.order.dto.response;

import lombok.Builder;
import lombok.Data;
import org.example.zenvybackend.order.enums.ReturnStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class ReturnRequestResponse {

    private UUID id;
    private UUID orderItemId;
    private UUID orderId;
    private UUID sellerOrderId;
    private String productName;
    private String brand;
    private Map<String, String> metadata;
    private Integer quantity;
    private String reason;
    private ReturnStatus status;
    private String sellerCompanyName;
    private LocalDateTime createdAt;
}
