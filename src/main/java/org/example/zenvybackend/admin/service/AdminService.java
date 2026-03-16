package org.example.zenvybackend.admin.service;

import org.example.zenvybackend.admin.dto.AdminCustomerResponse;
import org.example.zenvybackend.admin.dto.AdminSellerResponse;

import java.util.List;
import java.util.UUID;

public interface AdminService {

    List<AdminCustomerResponse> listCustomers(int pageNo, int pageSize, String filter, String sortDirection);

    List<AdminSellerResponse> listSellers(int pageNo, int pageSize, String filter, String sortDirection);

    void activateCustomer(UUID userId);

    void deactivateCustomer(UUID userId);

    void activateSeller(UUID userId);

    void deactivateSeller(UUID userId);

}
