package org.example.zenvybackend.order.service;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.order.repository.OrderRepository;
import org.example.zenvybackend.order.repository.SellerOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Loads order graphs in separate queries to avoid Hibernate MultipleBagFetchException
 * and pagination warnings when joining multiple List collections.
 */
@Component
@RequiredArgsConstructor
public class OrderFetchHelper {

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;

    public void fetchSellerOrdersForSummary(List<Order> orders) {
        if (orders.isEmpty()) {
            return;
        }
        List<UUID> orderIds = orders.stream().map(Order::getId).distinct().toList();
        orderRepository.findByIdsWithSellerOrders(orderIds);
    }

    public void fetchOrderItems(UUID orderId) {
        sellerOrderRepository.findByOrderIdInWithItems(List.of(orderId));
    }

    public Order getOrderWithDetails(UUID orderId) {
        Order order = orderRepository.findByIdWithSellerOrders(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        fetchOrderItems(orderId);
        return order;
    }

    public Order getOrderWithDetailsForCustomer(UUID orderId, UUID customerUserId) {
        Order order = orderRepository.findByIdWithSellerOrdersForCustomer(orderId, customerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        fetchOrderItems(orderId);
        return order;
    }

    public SellerOrder getSellerOrderWithDetails(UUID sellerOrderId, UUID sellerUserId) {
        SellerOrder sellerOrder = sellerOrderRepository.findByIdAndSellerUserId(sellerOrderId, sellerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found"));
        sellerOrderRepository.findByIdInWithItems(List.of(sellerOrderId));
        return sellerOrder;
    }
}
