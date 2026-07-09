package org.example.zenvybackend.order.service;

import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.request.CheckoutRequest;
import org.example.zenvybackend.order.dto.request.UpdateSellerOrderStatusRequest;
import org.example.zenvybackend.order.dto.request.VerifyPaymentRequest;
import org.example.zenvybackend.order.dto.response.OrderResponse;
import org.example.zenvybackend.order.dto.response.OrderSummaryResponse;
import org.example.zenvybackend.order.dto.response.PaymentSessionResponse;
import org.example.zenvybackend.order.dto.response.SellerOrderDetailResponse;

import java.util.UUID;

public interface OrderService {

    OrderResponse checkout(CheckoutRequest request);

    PagedResponse<OrderSummaryResponse> getCustomerOrders(int pageOffset, int pageSize);

    OrderResponse getCustomerOrder(UUID orderId);

    void cancelCustomerOrder(UUID orderId);

    PagedResponse<OrderSummaryResponse> getSellerOrders(int pageOffset, int pageSize);

    SellerOrderDetailResponse getSellerOrder(UUID sellerOrderId);

    void updateSellerOrderStatus(UUID sellerOrderId, UpdateSellerOrderStatusRequest request);

    PagedResponse<OrderSummaryResponse> getAdminOrders(int pageOffset, int pageSize);

    OrderResponse getAdminOrder(UUID orderId);

    PaymentSessionResponse getPaymentSession(UUID orderId);

    OrderResponse verifyPayment(UUID orderId, VerifyPaymentRequest request);
}
