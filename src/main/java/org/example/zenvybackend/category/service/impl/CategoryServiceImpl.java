package org.example.zenvybackend.category.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.util.PageUtils;
import org.example.zenvybackend.category.dto.request.*;
import org.example.zenvybackend.category.dto.response.*;
import org.example.zenvybackend.category.entity.*;
import org.example.zenvybackend.category.repository.*;
import org.example.zenvybackend.category.service.CategoryService;
import org.example.zenvybackend.product.repository.ProductRepository;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CategoryMetadataFieldRepository fieldRepository;
    private final CategoryMetadataFieldValuesRepository valuesRepository;
    private final ProductVariationRepository productVariationRepository;

    // ================= CREATE CATEGORY =================
    @Override
    public UUID createCategory(CreateCategoryRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }

        Category parent = null;

        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
        }

        if (categoryRepository
                .findByNameIgnoreCaseAndParentCategoryAndIsDeletedFalse(
                        request.getName(), parent
                )
                .isPresent()) {

            throw new BadRequestException(
                    "Category already exists under this parent"
            );
        }

        if (parent != null &&
                productRepository.existsByCategoryAndIsDeletedFalse(parent)) {

            throw new BadRequestException(
                    "Cannot create subcategory. Parent category already has products."
            );
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .parentCategory(parent)
                .build();

        categoryRepository.save(category);

        return category.getId();
    }

    // ================= GET CATEGORIES =================
    @Override
    public Page<CategoryTreeResponse> getCategories(UUID categoryId, PageRequestDto dto) {

        Pageable pageable = PageUtils.getPageable(dto, List.of("name", "id"));

        Page<Category> categories;

        if (categoryId != null) {

            Category parent = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            categories = categoryRepository
                    .findByParentCategoryAndIsDeletedFalse(parent, pageable);

        } else {

            if (dto.getQuery() != null && !dto.getQuery().isBlank()) {

                categories = categoryRepository
                        .findByNameContainingIgnoreCaseAndIsDeletedFalse(dto.getQuery(), pageable);

            } else {

                categories = categoryRepository.findByIsDeletedFalse(pageable);
            }
        }

        return categories.map(this::buildCategoryTree);
    }

    // ================= UPDATE CATEGORY =================
    @Override
    public void updateCategory(UUID categoryId, UpdateCategoryRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Category parent = category.getParentCategory();

        Optional<Category> existing =
                categoryRepository.findByNameIgnoreCaseAndParentCategoryAndIsDeletedFalse(
                        request.getName(), parent
                );

        if (existing.isPresent() && !existing.get().getId().equals(categoryId)) {
            throw new BadRequestException("Category already exists under this parent");
        }

        category.setName(request.getName().trim());

        categoryRepository.save(category);
    }

    // ================= ADD METADATA FIELD =================
    @Override
    public UUID addMetadataField(String name) {

        if (name == null || name.isBlank())
            throw new BadRequestException("Field name cannot be empty");

        if (fieldRepository.findByNameIgnoreCaseAndIsDeletedFalse(name).isPresent())
            throw new BadRequestException("Metadata field already exists");

        CategoryMetadataField field = CategoryMetadataField.builder()
                .name(name)
                .build();

        fieldRepository.save(field);

        return field.getId();
    }

    // ================= GET METADATA FIELDS =================
    @Override
    public Page<CategoryMetadataField> getMetadataFields(PageRequestDto dto) {

        Pageable pageable = PageUtils.getPageable(dto, List.of("name", "id"));

        if (dto.getQuery() != null && !dto.getQuery().isBlank()) {

            return fieldRepository
                    .findByNameContainingIgnoreCaseAndIsDeletedFalse(dto.getQuery(), pageable);
        }

        return fieldRepository.findByIsDeletedFalse(pageable);
    }

    // ================= ADD METADATA VALUES =================
    @Override
    public void addMetadataValues(UUID categoryId, List<AddMetadataValueRequest> requests) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        for (AddMetadataValueRequest request : requests) {

            CategoryMetadataField field = fieldRepository.findById(request.getFieldId())
                    .orElseThrow(() -> new ResourceNotFoundException("Metadata field not found"));

            if (request.getValues() == null || request.getValues().isEmpty()) {
                throw new BadRequestException("At least one value must be provided");
            }

            Set<String> uniqueValues = new HashSet<>(request.getValues());

            if (uniqueValues.size() != request.getValues().size()) {
                throw new BadRequestException("Duplicate values are not allowed");
            }

            Optional<CategoryMetadataFieldValues> existing =
                    valuesRepository.findByCategoryAndFieldAndIsDeletedFalse(category, field);

            if (existing.isPresent()) {

                CategoryMetadataFieldValues entity = existing.get();

                Set<String> oldValues =
                        new HashSet<>(Arrays.asList(entity.getMetadataValues().split(",")));

                oldValues.addAll(uniqueValues);

                entity.setMetadataValues(String.join(",", oldValues));

                valuesRepository.save(entity);

            } else {

                CategoryMetadataFieldValuesId id =
                        new CategoryMetadataFieldValuesId(
                                category.getId(),
                                field.getId()
                        );

                CategoryMetadataFieldValues entity =
                        CategoryMetadataFieldValues.builder()
                                .id(id)
                                .category(category)
                                .field(field)
                                .metadataValues(String.join(",", uniqueValues))
                                .build();

                valuesRepository.save(entity);
            }
        }
    }

    // ================= BUILD TREE =================
    private CategoryTreeResponse buildCategoryTree(Category category) {

        List<Category> childrenCategories =
                categoryRepository.findByParentCategoryAndIsDeletedFalse(category);

        List<ChildCategoryResponse> children = childrenCategories.stream()
                .map(child -> ChildCategoryResponse.builder()
                        .id(child.getId())
                        .name(child.getName())
                        .build())
                .toList();

        List<CategoryMetadataFieldValues> metadataValues =
                valuesRepository.findByCategoryWithField(category);

        List<MetadataFieldResponse> metadata = metadataValues.stream()
                .map(v -> MetadataFieldResponse.builder()
                        .fieldId(v.getField().getId())
                        .name(v.getField().getName())
                        .values(Arrays.asList(v.getMetadataValues().split(",")))
                        .build())
                .toList();

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentChain(buildParentChain(category))
                .children(children)
                .metadataFields(metadata)
                .build();
    }

    private List<ParentCategoryResponse> buildParentChain(Category category) {

        List<ParentCategoryResponse> parents = new ArrayList<>();

        Category current = category.getParentCategory();

        while (current != null) {

            parents.add(
                    ParentCategoryResponse.builder()
                            .id(current.getId())
                            .name(current.getName())
                            .build()
            );

            current = current.getParentCategory();
        }

        Collections.reverse(parents);

        return parents;
    }

    // ================= CUSTOMER CATEGORY =================
    @Override
    public List<CustomerCategoryResponse> getCustomerCategories(UUID categoryId) {

        List<Category> categories;

        if (categoryId == null) {
            categories = categoryRepository.findByParentCategoryIsNullAndIsDeletedFalse();
        } else {
            Category parent = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            categories = categoryRepository.findByParentCategoryAndIsDeletedFalse(parent);
        }

        return categories.stream()
                .map(this::buildCustomerCategoryResponse)
                .toList();
    }

    private CustomerCategoryResponse buildCustomerCategoryResponse(Category category) {

        List<Category> allCategories = getAllSubCategories(category);

        // 🔹 Metadata (only current category - OK as per requirement)
        List<CategoryMetadataFieldValues> metadataValues =
                valuesRepository.findByCategoryWithField(category);

        List<MetadataFieldResponse> metadata = metadataValues.stream()
                .map(v -> MetadataFieldResponse.builder()
                        .fieldId(v.getField().getId())
                        .name(v.getField().getName())
                        .values(new ArrayList<>(new HashSet<>(
                                Arrays.asList(v.getMetadataValues().split(","))
                        )))
                        .build())
                .toList();

        // 🔹 Brands (ALL categories)
        List<String> brands = new ArrayList<>();

        for (Category cat : allCategories) {
            brands.addAll(productRepository.findDistinctBrandsByCategory(cat));
        }

        brands = brands.stream().distinct().toList();

        // 🔹 Price (ALL categories)
        Double minPrice = Double.MAX_VALUE;
        Double maxPrice = Double.MIN_VALUE;

        for (Category cat : allCategories) {

            Double min = productVariationRepository.findMinPriceByCategory(cat);
            Double max = productVariationRepository.findMaxPriceByCategory(cat);

            if (min != null) minPrice = Math.min(minPrice, min);
            if (max != null) maxPrice = Math.max(maxPrice, max);
        }

        if (minPrice == Double.MAX_VALUE) minPrice = 0.0;
        if (maxPrice == Double.MIN_VALUE) maxPrice = 0.0;

        return CustomerCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .metadataFields(metadata)
                .brands(brands)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
    }

    // ================= FILTERING =================
    @Override
    public FilteringResponse getFilteringData(UUID categoryId) {

        if (categoryId == null) {
            throw new BadRequestException("CategoryId is required");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Category> allCategories = getAllSubCategories(category);

        // 🔹 Metadata (only category level)
        List<CategoryMetadataFieldValues> metadata =
                valuesRepository.findByCategoryWithField(category);

        // 🔹 Brands (ALL levels)
        List<String> brands = new ArrayList<>();

        for (Category cat : allCategories) {
            brands.addAll(productRepository.findDistinctBrandsByCategory(cat));
        }

        brands = brands.stream().distinct().toList();

        // 🔹 Price (ALL levels)
        Double min = Double.MAX_VALUE;
        Double max = Double.MIN_VALUE;

        for (Category cat : allCategories) {

            Double minVal = productVariationRepository.findMinPriceByCategory(cat);
            Double maxVal = productVariationRepository.findMaxPriceByCategory(cat);

            if (minVal != null) min = Math.min(min, minVal);
            if (maxVal != null) max = Math.max(max, maxVal);
        }

        if (min == Double.MAX_VALUE) min = 0.0;
        if (max == Double.MIN_VALUE) max = 0.0;

        return FilteringResponse.builder()
                .metadata(metadata.stream().map(v ->
                        MetadataFieldResponse.builder()
                                .fieldId(v.getField().getId())
                                .name(v.getField().getName())
                                .values(new ArrayList<>(new HashSet<>(
                                        Arrays.asList(v.getMetadataValues().split(","))
                                )))
                                .build()
                ).toList())
                .brands(brands)
                .minPrice(min)
                .maxPrice(max)
                .build();
    }

    private List<Category> getAllSubCategories(Category category) {

        List<Category> result = new ArrayList<>();
        result.add(category);

        List<Category> children =
                categoryRepository.findByParentCategoryAndIsDeletedFalse(category);

        for (Category child : children) {
            result.addAll(getAllSubCategories(child));
        }

        return result;
    }
}
