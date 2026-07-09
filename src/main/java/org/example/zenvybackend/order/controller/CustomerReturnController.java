package org.example.zenvybackend.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.request.CreateReturnRequest;
import org.example.zenvybackend.order.dto.response.ReturnRequestResponse;
import org.example.zenvybackend.order.service.ReturnService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerReturnController {

    private final ReturnService returnService;
    private final MessageResolver messageResolver;

    @PostMapping
    public ApiResponse<ReturnRequestResponse> createReturn(@Valid @RequestBody CreateReturnRequest request) {
        return ApiResponse.success(
                messageResolver.get("response.return.requested", "Return requested"),
                returnService.createReturn(request)
        );
    }

    @GetMapping
    public PagedResponse<ReturnRequestResponse> getReturns(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return returnService.getCustomerReturns(pageOffset, pageSize);
    }
}
