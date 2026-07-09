package org.example.zenvybackend.order.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.order.dto.response.OrderItemResponse;
import org.example.zenvybackend.order.dto.response.OrderResponse;
import org.example.zenvybackend.order.dto.response.OrderSummaryResponse;
import org.example.zenvybackend.order.dto.response.SellerOrderDetailResponse;
import org.example.zenvybackend.order.dto.response.SellerOrderResponse;
import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.order.entity.OrderItem;
import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.order.enums.OrderStatus;
import org.example.zenvybackend.order.enums.SellerOrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final ObjectMapper objectMapper;

    public OrderResponse toResponse(Order order) {
        List<SellerOrderResponse> sellerOrders = order.getSellerOrders().stream()
                .map(this::toSellerOrderResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .addressLine(order.getAddressLine())
                .city(order.getCity())
                .state(order.getState())
                .country(order.getCountry())
                .zipCode(order.getZipCode())
                .label(order.getLabel())
                .createdAt(order.getCreatedAt())
                .cancellable(isCancellable(order))
                .sellerOrders(sellerOrders)
                .build();
    }

    public OrderSummaryResponse toSummary(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .sellerOrderCount(order.getSellerOrders().size())
                .build();
    }

    public SellerOrderDetailResponse toSellerOrderDetail(SellerOrder sellerOrder) {
        Order order = sellerOrder.getOrder();
        return SellerOrderDetailResponse.builder()
                .sellerOrderId(sellerOrder.getId())
                .orderId(order.getId())
                .sellerOrder(toSellerOrderResponse(sellerOrder))
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .addressLine(order.getAddressLine())
                .city(order.getCity())
                .state(order.getState())
                .country(order.getCountry())
                .zipCode(order.getZipCode())
                .label(order.getLabel())
                .orderCreatedAt(order.getCreatedAt())
                .build();
    }

    private SellerOrderResponse toSellerOrderResponse(SellerOrder sellerOrder) {
        List<OrderItemResponse> items = sellerOrder.getItems().stream()
                .map(this::toOrderItemResponse)
                .toList();

        return SellerOrderResponse.builder()
                .id(sellerOrder.getId())
                .sellerId(sellerOrder.getSeller().getUserId())
                .sellerCompanyName(sellerOrder.getSeller().getCompanyName())
                .status(sellerOrder.getStatus())
                .subtotal(sellerOrder.getSubtotal())
                .items(items)
                .build();
    }

    private OrderItemResponse toOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productVariationId(item.getProductVariationId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .brand(item.getBrand())
                .metadata(parseMetadata(item.getMetadata()))
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .isCancellable(item.getIsCancellable())
                .isReturnable(item.getIsReturnable())
                .build();
    }

    private boolean isCancellable(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            return false;
        }

        boolean allSellerOrdersPending = order.getSellerOrders().stream()
                .allMatch(sellerOrder -> sellerOrder.getStatus() == SellerOrderStatus.PENDING);

        if (!allSellerOrdersPending) {
            return false;
        }

        return order.getSellerOrders().stream()
                .flatMap(sellerOrder -> sellerOrder.getItems().stream())
                .allMatch(item -> Boolean.TRUE.equals(item.getIsCancellable()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseMetadata(String metadata) {
        try {
            return objectMapper.readValue(metadata, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing order item metadata");
        }
    }
}
