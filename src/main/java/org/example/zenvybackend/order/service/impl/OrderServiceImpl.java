package org.example.zenvybackend.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.cart.entity.Cart;
import org.example.zenvybackend.cart.entity.CartItem;
import org.example.zenvybackend.cart.repository.CartRepository;
import org.example.zenvybackend.cart.service.CartService;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.email.OrderCancelledEmailData;
import org.example.zenvybackend.order.dto.email.OrderPlacedEmailData;
import org.example.zenvybackend.order.dto.email.SellerOrderStatusEmailData;
import org.example.zenvybackend.order.dto.request.CheckoutRequest;
import org.example.zenvybackend.order.dto.request.UpdateSellerOrderStatusRequest;
import org.example.zenvybackend.order.dto.request.VerifyPaymentRequest;
import org.example.zenvybackend.order.dto.response.OrderResponse;
import org.example.zenvybackend.order.dto.response.OrderSummaryResponse;
import org.example.zenvybackend.order.dto.response.PaymentSessionResponse;
import org.example.zenvybackend.order.dto.response.SellerOrderDetailResponse;
import org.example.zenvybackend.order.entity.Order;
import org.example.zenvybackend.order.entity.OrderItem;
import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.order.enums.OrderStatus;
import org.example.zenvybackend.order.enums.PaymentMethod;
import org.example.zenvybackend.order.enums.PaymentStatus;
import org.example.zenvybackend.order.enums.SellerOrderStatus;
import org.example.zenvybackend.order.mapper.OrderMapper;
import org.example.zenvybackend.order.repository.OrderRepository;
import org.example.zenvybackend.order.repository.SellerOrderRepository;
import org.example.zenvybackend.order.service.OrderEmailService;
import org.example.zenvybackend.order.service.OrderFetchHelper;
import org.example.zenvybackend.order.service.OrderService;
import org.example.zenvybackend.order.service.PaymentGatewayService;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.entity.Address;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.AddressRepository;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final SellerOrderRepository sellerOrderRepository;
    private final CartRepository cartRepository;
    private final CartService cartService;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final SellerRepository sellerRepository;
    private final ProductVariationRepository productVariationRepository;
    private final OrderMapper orderMapper;
    private final OrderEmailService orderEmailService;
    private final PaymentGatewayService paymentGatewayService;
    private final OrderFetchHelper orderFetchHelper;

    @Override
    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {
        Customer customer = getCurrentCustomer();
        Cart cart = cartRepository.findByCustomerUserId(customer.getUserId())
                .orElseThrow(() -> new BadRequestException("Cart is empty"));

        List<CartItem> cartItems = cart.getItems().stream()
                .filter(item -> !Boolean.TRUE.equals(item.getIsDeleted()))
                .toList();

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), customer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        PaymentStatus paymentStatus = request.getPaymentMethod() == PaymentMethod.COD
                ? PaymentStatus.PAID
                : PaymentStatus.PENDING;

        Map<UUID, SellerOrder> sellerOrdersBySellerId = new LinkedHashMap<>();
        double orderTotal = 0.0;

        Order order = Order.builder()
                .customer(customer)
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .zipCode(address.getZipCode())
                .label(address.getLabel())
                .status(OrderStatus.PLACED)
                .paymentStatus(paymentStatus)
                .paymentMethod(request.getPaymentMethod())
                .totalAmount(0.0)
                .build();

        for (CartItem cartItem : cartItems) {
            ProductVariation variation = productVariationRepository
                    .findByIdForUpdate(cartItem.getProductVariation().getId())
                    .orElseThrow(() -> new BadRequestException("Product variation is no longer available"));

            validateVariationForCheckout(variation, cartItem.getQuantity());

            Product product = variation.getProduct();
            Seller seller = product.getSeller();
            double lineTotal = variation.getPrice() * cartItem.getQuantity();
            orderTotal += lineTotal;

            variation.setQuantityAvailable(variation.getQuantityAvailable() - cartItem.getQuantity());
            productVariationRepository.save(variation);

            SellerOrder sellerOrder = sellerOrdersBySellerId.computeIfAbsent(
                    seller.getUserId(),
                    sellerId -> SellerOrder.builder()
                            .order(order)
                            .seller(seller)
                            .status(SellerOrderStatus.PENDING)
                            .subtotal(0.0)
                            .build()
            );

            OrderItem orderItem = OrderItem.builder()
                    .sellerOrder(sellerOrder)
                    .productVariationId(variation.getId())
                    .productId(product.getId())
                    .productName(product.getName())
                    .brand(product.getBrand())
                    .metadata(variation.getMetadata())
                    .quantity(cartItem.getQuantity())
                    .unitPrice(variation.getPrice())
                    .lineTotal(lineTotal)
                    .isCancellable(Boolean.TRUE.equals(product.getIsCancellable()))
                    .isReturnable(Boolean.TRUE.equals(product.getIsReturnable()))
                    .build();

            sellerOrder.getItems().add(orderItem);
            sellerOrder.setSubtotal(sellerOrder.getSubtotal() + lineTotal);
        }

        order.setTotalAmount(orderTotal);
        order.getSellerOrders().addAll(sellerOrdersBySellerId.values());

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();

        if (paymentStatus == PaymentStatus.PENDING) {
            paymentGatewayService.createPaymentSession(savedOrder);
            orderRepository.save(savedOrder);
        }

        Order detailedOrder = orderFetchHelper.getOrderWithDetails(savedOrder.getId());

        if (paymentStatus == PaymentStatus.PAID) {
            orderFetchHelper.fetchCustomerUser(detailedOrder.getId());
            orderEmailService.sendOrderPlacedEmail(OrderPlacedEmailData.from(detailedOrder));
        }

        return orderMapper.toResponse(detailedOrder);
    }

    @Override
    @Transactional
    public PaymentSessionResponse getPaymentSession(UUID orderId) {
        Customer customer = getCurrentCustomer();
        Order order = orderRepository.findByIdAndCustomerUserId(orderId, customer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new BadRequestException("Payment session is only for online orders");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payment is already completed");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is cancelled");
        }

        PaymentSessionResponse session = paymentGatewayService.createPaymentSession(order);
        orderRepository.save(order);
        return session;
    }

    @Override
    @Transactional
    public OrderResponse verifyPayment(UUID orderId, VerifyPaymentRequest request) {
        Customer customer = getCurrentCustomer();
        Order order = orderRepository.findByIdAndCustomerUserId(orderId, customer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getPaymentMethod() != PaymentMethod.ONLINE) {
            throw new BadRequestException("Payment verification is only for online orders");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new BadRequestException("Payment is already completed");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is cancelled");
        }

        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new BadRequestException("Payment cannot be confirmed for this order");
        }

        paymentGatewayService.verifyAndCapturePayment(
                order,
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);

        Order detailedOrder = orderFetchHelper.getOrderWithDetails(order.getId());
        orderFetchHelper.fetchCustomerUser(detailedOrder.getId());
        orderEmailService.sendOrderPlacedEmail(OrderPlacedEmailData.from(detailedOrder));

        return orderMapper.toResponse(detailedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryResponse> getCustomerOrders(int pageOffset, int pageSize) {
        Customer customer = getCurrentCustomer();
        Page<Order> page = orderRepository.findByCustomerOrderByCreatedAtDesc(
                customer,
                PageRequest.of(pageOffset, pageSize)
        );
        orderFetchHelper.fetchSellerOrdersForSummary(page.getContent());

        return toPagedSummary(page);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getCustomerOrder(UUID orderId) {
        Customer customer = getCurrentCustomer();
        Order order = orderFetchHelper.getOrderWithDetailsForCustomer(orderId, customer.getUserId());
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional
    public void cancelCustomerOrder(UUID orderId) {
        Customer customer = getCurrentCustomer();
        Order order = orderFetchHelper.getOrderWithDetailsForCustomer(orderId, customer.getUserId());

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is already cancelled");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("Completed orders cannot be cancelled");
        }

        boolean hasNonPendingSellerOrder = order.getSellerOrders().stream()
                .anyMatch(sellerOrder -> sellerOrder.getStatus() != SellerOrderStatus.PENDING);

        if (hasNonPendingSellerOrder) {
            throw new BadRequestException("Order cannot be cancelled after seller confirmation");
        }

        boolean hasNonCancellableItem = order.getSellerOrders().stream()
                .flatMap(sellerOrder -> sellerOrder.getItems().stream())
                .anyMatch(item -> !Boolean.TRUE.equals(item.getIsCancellable()));

        if (hasNonCancellableItem) {
            throw new BadRequestException("Order contains non-cancellable items");
        }

        restoreStock(order);
        order.setStatus(OrderStatus.CANCELLED);
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        } else {
            order.setPaymentStatus(PaymentStatus.FAILED);
        }
        order.getSellerOrders().forEach(sellerOrder -> sellerOrder.setStatus(SellerOrderStatus.CANCELLED));
        orderRepository.save(order);

        Order detailedOrder = orderFetchHelper.getOrderWithDetails(order.getId());
        orderFetchHelper.fetchCustomerUser(detailedOrder.getId());
        orderEmailService.sendOrderCancelledEmail(OrderCancelledEmailData.from(detailedOrder));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryResponse> getSellerOrders(int pageOffset, int pageSize) {
        Seller seller = getCurrentSeller();
        Page<SellerOrder> page = sellerOrderRepository.findBySellerOrderByCreatedAtDesc(
                seller,
                PageRequest.of(pageOffset, pageSize)
        );

        List<OrderSummaryResponse> content = page.getContent().stream()
                .map(sellerOrder -> OrderSummaryResponse.builder()
                        .id(sellerOrder.getId())
                        .status(mapSellerOrderToOrderStatus(sellerOrder.getStatus()))
                        .paymentStatus(sellerOrder.getOrder().getPaymentStatus())
                        .paymentMethod(sellerOrder.getOrder().getPaymentMethod())
                        .totalAmount(sellerOrder.getSubtotal())
                        .createdAt(sellerOrder.getCreatedAt())
                        .sellerOrderCount(1)
                        .build())
                .toList();

        return PagedResponse.<OrderSummaryResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SellerOrderDetailResponse getSellerOrder(UUID sellerOrderId) {
        Seller seller = getCurrentSeller();
        SellerOrder sellerOrder = orderFetchHelper.getSellerOrderWithDetails(sellerOrderId, seller.getUserId());
        return orderMapper.toSellerOrderDetail(sellerOrder);
    }

    @Override
    @Transactional
    public void updateSellerOrderStatus(UUID sellerOrderId, UpdateSellerOrderStatusRequest request) {
        Seller seller = getCurrentSeller();
        SellerOrder sellerOrder = sellerOrderRepository.findByIdAndSellerUserIdWithDetails(sellerOrderId, seller.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller order not found"));

        if (sellerOrder.getOrder().getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order is cancelled");
        }

        if (sellerOrder.getOrder().getPaymentStatus() != PaymentStatus.PAID) {
            throw new BadRequestException("Order payment is not completed");
        }

        SellerOrderStatus currentStatus = sellerOrder.getStatus();
        SellerOrderStatus newStatus = request.getStatus();

        if (!isValidSellerStatusTransition(currentStatus, newStatus)) {
            throw new BadRequestException("Invalid seller order status transition");
        }

        sellerOrder.setStatus(newStatus);
        sellerOrderRepository.save(sellerOrder);

        orderEmailService.sendSellerOrderStatusEmail(SellerOrderStatusEmailData.from(sellerOrder, newStatus));

        Order order = orderRepository.findByIdWithSellerOrders(sellerOrder.getOrder().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        if (order.getSellerOrders().stream().allMatch(so -> so.getStatus() == SellerOrderStatus.DELIVERED)) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryResponse> getAdminOrders(int pageOffset, int pageSize) {
        Page<Order> page = orderRepository.findAllByOrderByCreatedAtDesc(
                PageRequest.of(pageOffset, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        );
        orderFetchHelper.fetchSellerOrdersForSummary(page.getContent());
        return toPagedSummary(page);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getAdminOrder(UUID orderId) {
        Order order = orderFetchHelper.getOrderWithDetails(orderId);
        return orderMapper.toResponse(order);
    }

    private Customer getCurrentCustomer() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Customer account is not activated");
        }

        return customerRepository.findByUser(user)
                .orElseThrow(() -> new BadRequestException("User is not a customer"));
    }

    private Seller getCurrentSeller() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Seller account is not activated");
        }

        return sellerRepository.findById(currentUserId)
                .orElseThrow(() -> new BadRequestException("User is not a seller"));
    }

    private void validateVariationForCheckout(ProductVariation variation, int quantity) {
        Product product = variation.getProduct();

        if (!Boolean.TRUE.equals(variation.getIsActive()) || Boolean.TRUE.equals(variation.getIsDeleted())) {
            throw new BadRequestException("Product variation is no longer available");
        }

        if (!Boolean.TRUE.equals(product.getIsActive()) || Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product is not available");
        }

        if (variation.getQuantityAvailable() < quantity) {
            throw new BadRequestException("Insufficient stock available");
        }
    }

    private void restoreStock(Order order) {
        for (SellerOrder sellerOrder : order.getSellerOrders()) {
            for (OrderItem item : sellerOrder.getItems()) {
                ProductVariation variation = productVariationRepository
                        .findByIdForUpdate(item.getProductVariationId())
                        .orElse(null);

                if (variation != null) {
                    variation.setQuantityAvailable(variation.getQuantityAvailable() + item.getQuantity());
                    productVariationRepository.save(variation);
                }
            }
        }
    }

    private boolean isValidSellerStatusTransition(SellerOrderStatus current, SellerOrderStatus next) {
        if (current == next) {
            return false;
        }

        return switch (current) {
            case PENDING -> next == SellerOrderStatus.CONFIRMED || next == SellerOrderStatus.CANCELLED;
            case CONFIRMED -> next == SellerOrderStatus.SHIPPED || next == SellerOrderStatus.CANCELLED;
            case SHIPPED -> next == SellerOrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    private OrderStatus mapSellerOrderToOrderStatus(SellerOrderStatus status) {
        return switch (status) {
            case DELIVERED -> OrderStatus.COMPLETED;
            case CANCELLED -> OrderStatus.CANCELLED;
            default -> OrderStatus.PLACED;
        };
    }

    private PagedResponse<OrderSummaryResponse> toPagedSummary(Page<Order> page) {
        List<OrderSummaryResponse> content = page.getContent().stream()
                .map(orderMapper::toSummary)
                .toList();

        return PagedResponse.<OrderSummaryResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
