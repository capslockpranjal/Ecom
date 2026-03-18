package org.example.zenvybackend.product.controller;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.product.dto.request.*;
import org.example.zenvybackend.product.service.CategoryService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ApiResponse<UUID> createCategory(
            @RequestBody CreateCategoryRequest request
    ) {

        return ApiResponse.success(
                "Category created",
                categoryService.createCategory(request)
        );
    }

    @GetMapping
    public ApiResponse<?> getCategories(

            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String query
    ) {

        PageRequestDto dto = new PageRequestDto();

        dto.setMax(max);
        dto.setOffset(offset);
        dto.setSort(sort);
        dto.setOrder(order);
        dto.setQuery(query);

        return ApiResponse.success(
                "Category list",
                categoryService.getCategories(categoryId, dto)
        );
    }

    @PutMapping("/{categoryId}")
    public ApiResponse<Void> updateCategory(

            @PathVariable UUID categoryId,
            @RequestBody UpdateCategoryRequest request
    ) {

        categoryService.updateCategory(categoryId, request);

        return ApiResponse.success("Category updated", null);
    }

    @GetMapping("/metadata-field")
    public ApiResponse<?> getMetadataFields(

            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String query
    ) {

        PageRequestDto dto = new PageRequestDto();

        dto.setMax(max);
        dto.setOffset(offset);
        dto.setSort(sort);
        dto.setOrder(order);
        dto.setQuery(query);

        return ApiResponse.success(
                "Metadata fields",
                categoryService.getMetadataFields(dto)
        );
    }

    @PostMapping("/metadata-field")
    public ApiResponse<UUID> addMetadataField(
            @RequestBody AddMetadataFieldRequest request) {

        return ApiResponse.success(
                "Metadata field created",
                categoryService.addMetadataField(request.getName())
        );
    }

    @PostMapping("/{categoryId}/metadata")
    public ApiResponse<Void> addMetadataValues(
            @PathVariable UUID categoryId,
            @RequestBody AddMetadataValueRequest request) {

        categoryService.addMetadataValues(categoryId, request);

        return ApiResponse.success("Metadata values added", null);
    }
}
