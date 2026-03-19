package org.example.zenvybackend.category.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.category.dto.request.*;
import org.example.zenvybackend.category.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UUID> createCategory(
           @Valid @RequestBody CreateCategoryRequest request
    ) {

        return ApiResponse.success(
                "Category created",
                categoryService.createCategory(request)
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getCategories(

            @RequestParam(required = false) UUID categoryId,
           @Min(1) @RequestParam(required = false) Integer max,
           @Min(0) @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String query
    ) {

        PageRequestDto dto = buildPageRequest(max, offset, sort, order, query);

        return ApiResponse.success(
                "Category list",
                categoryService.getCategories(categoryId, dto)
        );
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateCategory(

            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request
    ) {

        categoryService.updateCategory(categoryId, request);

        return ApiResponse.success("Category updated", null);
    }

    @GetMapping("/metadata-field")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getMetadataFields(

           @Min(1) @RequestParam(required = false) Integer max,
           @Min(0) @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String query
    ) {

        PageRequestDto dto = buildPageRequest(max, offset, sort, order, query);

        return ApiResponse.success(
                "Metadata fields",
                categoryService.getMetadataFields(dto)
        );
    }

    @PostMapping("/metadata-field")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UUID> addMetadataField(
            @Valid @RequestBody AddMetadataFieldRequest request) {

        return ApiResponse.success(
                "Metadata field created",
                categoryService.addMetadataField(request.getName())
        );
    }

    @PostMapping("/{categoryId}/metadata")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> addMetadataValues(
            @PathVariable UUID categoryId,
            @RequestBody @NotEmpty List<@Valid AddMetadataValueRequest> requests) {

        categoryService.addMetadataValues(categoryId, requests);

        return ApiResponse.success("Metadata values added", null);
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<?> getSellerCategories(
            @RequestParam(required = false) UUID categoryId,
            @Min(1) @RequestParam(required = false) Integer max,
            @Min(0) @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String query
    ) {

        PageRequestDto dto = buildPageRequest(max, offset, sort, order, query);

        return ApiResponse.success(
                "Seller category list",
                categoryService.getCategories(categoryId, dto)
        );
    }

    @GetMapping("/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> getCustomerCategories(
            @RequestParam(required = false) UUID categoryId
    ) {

        return ApiResponse.success(
                "Customer categories",
                categoryService.getCustomerCategories(categoryId)
        );
    }

    // 🔹 FILTER API

    @GetMapping("/customer/{categoryId}/filters")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<?> getFilters(@PathVariable UUID categoryId) {

        return ApiResponse.success(
                "Filtering data",
                categoryService.getFilteringData(categoryId)
        );
    }

    private PageRequestDto buildPageRequest(
            Integer max, Integer offset,
            String sort, String order, String query
    ) {
        PageRequestDto dto = new PageRequestDto();
        dto.setMax(max);
        dto.setOffset(offset);
        dto.setSort(sort);
        dto.setOrder(order);
        dto.setQuery(query);
        return dto;
    }
}
