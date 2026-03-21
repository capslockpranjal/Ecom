package org.example.zenvybackend.category.service;

import org.example.zenvybackend.category.dto.request.AddMetadataValueRequest;
import org.example.zenvybackend.category.dto.request.CreateCategoryRequest;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.category.dto.request.UpdateCategoryRequest;
import org.example.zenvybackend.category.dto.response.CategoryTreeResponse;
import org.example.zenvybackend.category.dto.response.CustomerCategoryResponse;
import org.example.zenvybackend.category.dto.response.FilteringResponse;
import org.example.zenvybackend.category.entity.CategoryMetadataField;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    UUID createCategory(CreateCategoryRequest request);

    Page<CategoryTreeResponse> getCategories(
            UUID categoryId,
            PageRequestDto dto
    );

    void updateCategory(UUID categoryId, UpdateCategoryRequest request);

    UUID addMetadataField(String name);

    Page<CategoryMetadataField> getMetadataFields(PageRequestDto dto);

    void addMetadataValues(UUID categoryId, List<AddMetadataValueRequest> requests);


    List<CustomerCategoryResponse> getCustomerCategories(UUID categoryId);

    FilteringResponse getFilteringData(UUID categoryId);
}
