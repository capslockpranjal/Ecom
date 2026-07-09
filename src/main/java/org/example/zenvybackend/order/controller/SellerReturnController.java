package org.example.zenvybackend.order.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.i18n.MessageResolver;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.order.dto.request.UpdateReturnStatusRequest;
import org.example.zenvybackend.order.dto.response.ReturnRequestResponse;
import org.example.zenvybackend.order.service.ReturnService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/seller/returns")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SELLER')")
public class SellerReturnController {

    private final ReturnService returnService;
    private final MessageResolver messageResolver;

    @GetMapping
    public PagedResponse<ReturnRequestResponse> getReturns(
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return returnService.getSellerReturns(pageOffset, pageSize);
    }

    @PatchMapping("/{returnId}/status")
    public ApiResponse<ReturnRequestResponse> updateStatus(
            @PathVariable UUID returnId,
            @Valid @RequestBody UpdateReturnStatusRequest request
    ) {
        return ApiResponse.success(
                messageResolver.get("response.return.status_updated", "Return status updated"),
                returnService.updateSellerReturnStatus(returnId, request)
        );
    }
}
