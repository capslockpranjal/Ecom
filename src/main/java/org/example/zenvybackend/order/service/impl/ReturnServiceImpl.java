package org.example.zenvybackend.order.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.request.CreateReturnRequest;
import org.example.zenvybackend.order.dto.request.UpdateReturnStatusRequest;
import org.example.zenvybackend.order.dto.response.ReturnRequestResponse;
import org.example.zenvybackend.order.entity.OrderItem;
import org.example.zenvybackend.order.entity.ReturnRequest;
import org.example.zenvybackend.order.entity.SellerOrder;
import org.example.zenvybackend.order.enums.PaymentStatus;
import org.example.zenvybackend.order.enums.ReturnStatus;
import org.example.zenvybackend.order.enums.SellerOrderStatus;
import org.example.zenvybackend.order.mapper.ReturnMapper;
import org.example.zenvybackend.order.repository.OrderItemRepository;
import org.example.zenvybackend.order.repository.ReturnRequestRepository;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReturnServiceImpl implements org.example.zenvybackend.order.service.ReturnService {

    private static final List<ReturnStatus> ACTIVE_RETURN_STATUSES = List.of(
            ReturnStatus.REQUESTED,
            ReturnStatus.APPROVED
    );

    private final OrderItemRepository orderItemRepository;
    private final ReturnRequestRepository returnRequestRepository;
    private final ProductVariationRepository productVariationRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final UserRepository userRepository;
    private final ReturnMapper returnMapper;

    @Override
    @Transactional
    public ReturnRequestResponse createReturn(CreateReturnRequest request) {
        Customer customer = getCurrentCustomer();

        OrderItem orderItem = orderItemRepository
                .findByIdAndCustomerUserId(request.getOrderItemId(), customer.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));

        SellerOrder sellerOrder = orderItem.getSellerOrder();

        if (!sellerOrder.getOrder().getCustomer().getUserId().equals(customer.getUserId())) {
            throw new BadRequestException("Order item does not belong to customer");
        }

        if (!Boolean.TRUE.equals(orderItem.getIsReturnable())) {
            throw new BadRequestException("Item is not returnable");
        }

        if (sellerOrder.getStatus() != SellerOrderStatus.DELIVERED) {
            throw new BadRequestException("Item can only be returned after delivery");
        }

        if (returnRequestRepository.existsByOrderItemIdAndStatusIn(
                orderItem.getId(), ACTIVE_RETURN_STATUSES)) {
            throw new BadRequestException("Return already requested for this item");
        }

        ReturnRequest returnRequest = ReturnRequest.builder()
                .orderItem(orderItem)
                .customer(customer)
                .sellerOrder(sellerOrder)
                .quantity(orderItem.getQuantity())
                .reason(request.getReason())
                .status(ReturnStatus.REQUESTED)
                .build();

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return returnMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReturnRequestResponse> getCustomerReturns(int pageOffset, int pageSize) {
        Customer customer = getCurrentCustomer();
        Page<ReturnRequest> page = returnRequestRepository.findByCustomerOrderByCreatedAtDesc(
                customer,
                PageRequest.of(pageOffset, pageSize)
        );
        return toPagedResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReturnRequestResponse> getSellerReturns(int pageOffset, int pageSize) {
        Seller seller = getCurrentSeller();
        Page<ReturnRequest> page = returnRequestRepository.findBySellerOrderByCreatedAtDesc(
                seller,
                PageRequest.of(pageOffset, pageSize)
        );
        return toPagedResponse(page);
    }

    @Override
    @Transactional
    public ReturnRequestResponse updateSellerReturnStatus(UUID returnId, UpdateReturnStatusRequest request) {
        Seller seller = getCurrentSeller();
        ReturnRequest returnRequest = returnRequestRepository.findByIdAndSellerUserId(returnId, seller.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Return request not found"));

        if (returnRequest.getStatus() != ReturnStatus.REQUESTED) {
            throw new BadRequestException("Return request is no longer pending");
        }

        ReturnStatus newStatus = request.getStatus();
        if (newStatus != ReturnStatus.APPROVED && newStatus != ReturnStatus.REJECTED) {
            throw new BadRequestException("Invalid return status transition");
        }

        returnRequest.setStatus(newStatus);

        if (newStatus == ReturnStatus.APPROVED) {
            restoreStock(returnRequest);
            returnRequest.setStatus(ReturnStatus.COMPLETED);

            var order = returnRequest.getSellerOrder().getOrder();
            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }

        ReturnRequest saved = returnRequestRepository.save(returnRequest);
        return returnMapper.toResponse(saved);
    }

    private void restoreStock(ReturnRequest returnRequest) {
        OrderItem item = returnRequest.getOrderItem();
        ProductVariation variation = productVariationRepository
                .findByIdForUpdate(item.getProductVariationId())
                .orElse(null);

        if (variation != null) {
            variation.setQuantityAvailable(variation.getQuantityAvailable() + item.getQuantity());
            productVariationRepository.save(variation);
        }
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

    private PagedResponse<ReturnRequestResponse> toPagedResponse(Page<ReturnRequest> page) {
        List<ReturnRequestResponse> content = page.getContent().stream()
                .map(returnMapper::toResponse)
                .toList();

        return PagedResponse.<ReturnRequestResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
