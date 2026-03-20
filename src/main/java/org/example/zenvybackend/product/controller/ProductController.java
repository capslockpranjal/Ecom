package org.example.zenvybackend.product.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.common.response.ApiResponse;
import org.example.zenvybackend.product.dto.request.AddProductRequest;
import org.example.zenvybackend.product.dto.request.AddProductVariationRequest;
import org.example.zenvybackend.product.dto.request.UpdateProductRequest;
import org.example.zenvybackend.product.dto.request.UpdateProductVariationRequest;
import org.example.zenvybackend.product.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<UUID> addProduct(@Valid @RequestBody AddProductRequest request) {
        return ApiResponse.success("Product created", productService.addProduct(request));
    }

    @PostMapping(value = "/variation", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> addVariation(@Valid @RequestBody AddProductVariationRequest request) {
        productService.addVariation(request);
        return ApiResponse.success("Variation added", null);
    }

    @PostMapping(value = "/variation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> addVariationMultipart(
            @Valid @RequestPart("data") AddProductVariationRequest request,
            @RequestPart("primaryImage") MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages", required = false) java.util.List<MultipartFile> secondaryImages
    ) {
        productService.addVariation(request, primaryImage, secondaryImages);
        return ApiResponse.success("Variation added", null);
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> updateProduct(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        productService.updateProduct(productId, request);
        return ApiResponse.success("Product updated successfully", null);
    }

    @PutMapping(value = "/variation/{variationId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> updateVariation(
            @PathVariable UUID variationId,
            @Valid @RequestBody UpdateProductVariationRequest request
    ) {
        productService.updateVariation(variationId, request);
        return ApiResponse.success("Product variation updated successfully", null);
    }

    @PutMapping(value = "/variation/{variationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> updateVariationMultipart(
            @PathVariable UUID variationId,
            @Valid @RequestPart("data") UpdateProductVariationRequest request,
            @RequestPart(value = "primaryImage", required = false) MultipartFile primaryImage,
            @RequestPart(value = "secondaryImages", required = false) java.util.List<MultipartFile> secondaryImages
    ) {
        productService.updateVariation(variationId, request, primaryImage, secondaryImages);
        return ApiResponse.success("Product variation updated successfully", null);
    }

    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<Void> deleteProduct(@PathVariable UUID productId) {

        productService.deleteProduct(productId);

        return ApiResponse.success("Product deleted successfully", null);
    }

    @GetMapping
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<?> getProducts(

            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String query
    ) {

        PageRequestDto dto = buildPageRequest(max, offset, sort, order, query);

        return ApiResponse.success(
                "Product list",
                productService.getProducts(productId, dto)
        );
    }

    @GetMapping("/{productId}/variations")
    @PreAuthorize("hasRole('SELLER')")
    public ApiResponse<?> getProductVariations(

            @PathVariable UUID productId,
            @RequestParam(required = false) UUID variationId,
            @RequestParam(required = false) Integer max,
            @RequestParam(required = false) Integer offset,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) String query
    ) {

        PageRequestDto dto = buildPageRequest(max, offset, sort, order, query);


        return ApiResponse.success(
                "Product variation details",
                productService.getProductVariations(productId, variationId, dto)
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
