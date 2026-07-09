package org.example.zenvybackend.order.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.order.config.RazorpayProperties;
import org.example.zenvybackend.order.dto.response.PaymentSessionResponse;
import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.order.enums.PaymentMethod;
import org.example.zenvybackend.order.service.PaymentGatewayService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RazorpayPaymentServiceImpl implements PaymentGatewayService {

    private static final String RAZORPAY_API = "https://api.razorpay.com/v1";

    private final RazorpayProperties razorpayProperties;
    private final ObjectMapper objectMapper;
    private final RestClient razorpayClient = RestClient.builder()
            .baseUrl(RAZORPAY_API)
            .build();

    @Override
    public PaymentSessionResponse createPaymentSession(Order order) {
        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new BadRequestException("Payment session is only for online orders");
        }

        if (!razorpayProperties.isConfigured()) {
            throw new BadRequestException("Online payment gateway is not configured");
        }

        String razorpayOrderId = order.getRazorpayOrderId();
        if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
            razorpayOrderId = createRazorpayOrder(order);
        }

        long amountPaise = Math.round(order.getTotalAmount() * 100);

        return PaymentSessionResponse.builder()
                .orderId(order.getId())
                .razorpayOrderId(razorpayOrderId)
                .razorpayKeyId(razorpayProperties.getKeyId())
                .amount(amountPaise)
                .currency(razorpayProperties.getCurrency())
                .paymentStatus(order.getPaymentStatus().name())
                .paymentMethod(order.getPaymentMethod().name())
                .requiresPayment(true)
                .message("Complete payment using Razorpay")
                .build();
    }

    @Override
    public void verifyAndCapturePayment(
            Order order,
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature
    ) {
        if (!razorpayProperties.isConfigured()) {
            throw new BadRequestException("Online payment gateway is not configured");
        }

        if (order.getRazorpayOrderId() == null || !order.getRazorpayOrderId().equals(razorpayOrderId)) {
            throw new BadRequestException("Payment order mismatch");
        }

        String payload = razorpayOrderId + "|" + razorpayPaymentId;
        String expectedSignature = hmacSha256(payload, razorpayProperties.getKeySecret());

        if (!expectedSignature.equals(razorpaySignature)) {
            throw new BadRequestException("Invalid payment signature");
        }

        order.setRazorpayPaymentId(razorpayPaymentId);
    }

    String createRazorpayOrder(Order order) {
        long amountPaise = Math.round(order.getTotalAmount() * 100);
        if (amountPaise < 100) {
            throw new BadRequestException("Order amount is too low for online payment");
        }

        Map<String, Object> body = Map.of(
                "amount", amountPaise,
                "currency", razorpayProperties.getCurrency(),
                "receipt", order.getId().toString(),
                "notes", Map.of("orderId", order.getId().toString())
        );

        try {
            String responseBody = razorpayClient.post()
                    .uri("/orders")
                    .header(HttpHeaders.AUTHORIZATION, basicAuthHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode json = objectMapper.readTree(responseBody);
            String razorpayOrderId = json.path("id").asText(null);
            if (razorpayOrderId == null || razorpayOrderId.isBlank()) {
                throw new BadRequestException("Failed to create payment order");
            }

            order.setRazorpayOrderId(razorpayOrderId);
            return razorpayOrderId;
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Failed to initiate online payment");
        }
    }

    private String basicAuthHeader() {
        String credentials = razorpayProperties.getKeyId() + ":" + razorpayProperties.getKeySecret();
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new BadRequestException("Payment verification failed");
        }
    }
}
