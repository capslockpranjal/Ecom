package org.example.zenvybackend.product.service;

import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.product.dto.request.AddProductRequest;
import org.example.zenvybackend.product.dto.request.AddProductVariationRequest;
import org.example.zenvybackend.product.dto.request.UpdateProductRequest;
import org.example.zenvybackend.product.dto.request.UpdateProductVariationRequest;
import org.example.zenvybackend.product.dto.response.CustomerProductDetailResponse;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface ProductService {

    UUID addProduct(AddProductRequest request);

    void addVariation(AddProductVariationRequest request);

    void addVariation(AddProductVariationRequest request, MultipartFile primaryImage, java.util.List<MultipartFile> secondaryImages);

    void updateProduct(UUID productId, UpdateProductRequest request);

    void updateVariation(UUID variationId, UpdateProductVariationRequest request);

    void updateVariation(UUID variationId, UpdateProductVariationRequest request, MultipartFile primaryImage, java.util.List<MultipartFile> secondaryImages);

    void deleteProduct(UUID productId);

    Object getProducts(UUID productId, PageRequestDto dto);

    Object getProductVariations(UUID productId, UUID variationId, PageRequestDto dto);

    CustomerProductDetailResponse getCustomerProduct(UUID productId);

    Object getCustomerProducts(UUID categoryId, PageRequestDto dto);

    Object getSimilarCustomerProducts(UUID productId, PageRequestDto dto);
}
