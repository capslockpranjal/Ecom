package org.example.zenvybackend.order.service;

import org.example.zenvybackend.order.dto.response.PaymentSessionResponse;
import org.example.zenvybackend.order.entity.Order;

public interface PaymentGatewayService {

    PaymentSessionResponse createPaymentSession(Order order);

    void verifyAndCapturePayment(
            Order order,
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    );
}
