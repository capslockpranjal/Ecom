package org.example.zenvybackend.admin.service;

import org.example.zenvybackend.admin.dto.AdminCustomerResponse;

import org.example.zenvybackend.admin.dto.AdminSellerResponse;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.common.response.PagedResponse;


import java.util.UUID;

public interface AdminService {

    PagedResponse<AdminCustomerResponse> listCustomers(
            int pageOffset,
            int pageSize,
            String sort,
            String email
    );

    PagedResponse<AdminSellerResponse> listSellers(
            int pageOffset,
            int pageSize,
            String sort,
            String email
    );
    void activateCustomer(UUID userId);

    void deactivateCustomer(UUID userId);

    void activateSeller(UUID userId);

    void deactivateSeller(UUID userId);

    Object getProducts(UUID productId, UUID sellerId, UUID categoryId, PageRequestDto dto);

    void activateProduct(UUID productId);

    void deactivateProduct(UUID productId);

}
