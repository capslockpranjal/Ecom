package org.example.zenvybackend.order.service;

import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.request.CreateReturnRequest;
import org.example.zenvybackend.order.dto.request.UpdateReturnStatusRequest;
import org.example.zenvybackend.order.dto.response.ReturnRequestResponse;

import java.util.UUID;

public interface ReturnService {

    ReturnRequestResponse createReturn(CreateReturnRequest request);

    PagedResponse<ReturnRequestResponse> getCustomerReturns(int pageOffset, int pageSize);

    PagedResponse<ReturnRequestResponse> getSellerReturns(int pageOffset, int pageSize);

    ReturnRequestResponse updateSellerReturnStatus(UUID returnId, UpdateReturnStatusRequest request);
}
