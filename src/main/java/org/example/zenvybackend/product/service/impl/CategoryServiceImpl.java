package org.example.zenvybackend.product.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.util.PageUtils;
import org.example.zenvybackend.product.dto.request.AddMetadataValueRequest;
import org.example.zenvybackend.product.dto.request.CreateCategoryRequest;
import org.example.zenvybackend.product.dto.request.PageRequestDto;
import org.example.zenvybackend.product.dto.request.UpdateCategoryRequest;
import org.example.zenvybackend.product.dto.response.*;
import org.example.zenvybackend.product.entity.Category;
import org.example.zenvybackend.product.entity.CategoryMetadataField;
import org.example.zenvybackend.product.entity.CategoryMetadataFieldValues;
import org.example.zenvybackend.product.entity.CategoryMetadataFieldValuesId;
import org.example.zenvybackend.product.repository.CategoryMetadataFieldRepository;
import org.example.zenvybackend.product.repository.CategoryMetadataFieldValuesRepository;
import org.example.zenvybackend.product.repository.CategoryRepository;
import org.example.zenvybackend.product.repository.ProductRepository;
import org.example.zenvybackend.product.service.CategoryService;
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

    @Override
    public UUID createCategory(CreateCategoryRequest request) {


        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }

        Category parent = null;

        if (request.getParentId() != null) {

            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Parent category not found"));
        }

        if (categoryRepository
                .findByNameAndParentCategory(request.getName(), parent)
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
                .name(request.getName().trim()) // ✅ trim added
                .parentCategory(parent)
                .build();

        categoryRepository.save(category);

        return category.getId();
    }

    @Override
    public Page<CategoryTreeResponse> getCategories(UUID categoryId, PageRequestDto dto) {


        String sort = (dto.getSort() == null || dto.getSort().isBlank()) ? "name" : dto.getSort();
        String order = (dto.getOrder() == null || dto.getOrder().isBlank()) ? "asc" : dto.getOrder();

        int max = (dto.getMax() == null || dto.getMax() <= 0) ? 10 : dto.getMax();
        int offset = (dto.getOffset() == null || dto.getOffset() < 0) ? 0 : dto.getOffset();

        List<String> allowedSortFields = List.of("name", "id");

        if (!allowedSortFields.contains(sort)) {
            throw new BadRequestException("Invalid sort field");
        }

        Pageable pageable = PageUtils.getPageable(
                dto,
                List.of("name", "id")
        );
        Page<Category> categories;

        if (categoryId != null) {

            Category parent = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

            categories = categoryRepository.findByParentCategory(parent, pageable);

        } else {

            if (dto.getQuery() != null && !dto.getQuery().isBlank()) {

                categories = categoryRepository
                        .findByNameContainingIgnoreCase(dto.getQuery(), pageable);

            } else {

                categories = categoryRepository.findAll(pageable);
            }
        }

        return categories.map(this::buildCategoryTree);
    }

    @Override
    public void updateCategory(UUID categoryId, UpdateCategoryRequest request) {


        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("Category name cannot be empty");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        Category parent = category.getParentCategory();

        Optional<Category> existing =
                categoryRepository.findByNameAndParentCategory(request.getName(), parent);


        if (existing.isPresent() && !existing.get().getId().equals(categoryId)) {

            throw new BadRequestException(
                    "Category already exists under this parent"
            );
        }

        category.setName(request.getName().trim());

        categoryRepository.save(category);
    }


    @Override
    public UUID addMetadataField(String name) {

        if (name == null || name.isBlank())
            throw new BadRequestException("Field name cannot be empty");

        if (fieldRepository.findByNameIgnoreCase(name).isPresent())
            throw new BadRequestException("Metadata field already exists");

        CategoryMetadataField field = CategoryMetadataField.builder()
                .name(name)
                .build();

        fieldRepository.save(field);

        return field.getId();
    }

    @Override
    public Page<CategoryMetadataField> getMetadataFields(PageRequestDto dto) {

        String sort = (dto.getSort() == null || dto.getSort().isBlank()) ? "name" : dto.getSort();
        String order = (dto.getOrder() == null || dto.getOrder().isBlank()) ? "asc" : dto.getOrder();

        int max = (dto.getMax() == null || dto.getMax() <= 0) ? 10 : dto.getMax();
        int offset = (dto.getOffset() == null || dto.getOffset() < 0) ? 0 : dto.getOffset();


        List<String> allowedSortFields = List.of("name", "id");

        if (!allowedSortFields.contains(sort)) {
            throw new BadRequestException("Invalid sort field");
        }

        Pageable pageable = PageUtils.getPageable(
                dto,
                List.of("name", "id")
        );
        if (dto.getQuery() != null && !dto.getQuery().isBlank()) {

            return fieldRepository.findByNameContainingIgnoreCase(
                    dto.getQuery(),
                    pageable
            );
        }

        return fieldRepository.findAll(pageable);
    }

    @Override
    public void addMetadataValues(UUID categoryId, AddMetadataValueRequest request) {

        // 1️⃣ Validate category
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        // 2️⃣ Validate field
        CategoryMetadataField field = fieldRepository.findById(request.getFieldId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Metadata field not found"));

        // 3️⃣ Validate input values
        if (request.getValues() == null || request.getValues().isEmpty()) {
            throw new BadRequestException("At least one value must be provided");
        }

        // 4️⃣ Remove duplicates from request
        Set<String> uniqueValues = new HashSet<>(request.getValues());

        if (uniqueValues.size() != request.getValues().size()) {
            throw new BadRequestException("Duplicate values are not allowed");
        }

        // 5️⃣ Check if metadata already exists for this category + field
        Optional<CategoryMetadataFieldValues> existing =
                valuesRepository.findByCategoryAndField(category, field);

        if (existing.isPresent()) {

            // 👉 Merge values instead of throwing error (better approach)

            CategoryMetadataFieldValues entity = existing.get();

            Set<String> oldValues =
                    new HashSet<>(Arrays.asList(entity.getMetadataValues().split(",")));

            oldValues.addAll(uniqueValues);

            entity.setMetadataValues(String.join(",", oldValues));

            valuesRepository.save(entity);

            return;
        }

        // 6️⃣ Create new entry
        String values = String.join(",", uniqueValues);

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
                        .metadataValues(values)
                        .build();

        valuesRepository.save(entity);

        valuesRepository.save(entity);
    }

    private CategoryTreeResponse buildCategoryTree(Category category) {

        List<ParentCategoryResponse> parents = buildParentChain(category);

        List<Category> childrenCategories = categoryRepository.findByParentCategory(category);

        List<ChildCategoryResponse> children = childrenCategories.stream()
                .map(child ->
                        ChildCategoryResponse.builder()
                                .id(child.getId())
                                .name(child.getName())
                                .build()
                ).toList();

        List<CategoryMetadataFieldValues> metadataValues =
                valuesRepository.findByCategoryWithField(category);

        List<MetadataFieldResponse> metadata = metadataValues.stream()
                .map(v ->
                        MetadataFieldResponse.builder()
                                .fieldId(v.getField().getId())
                                .name(v.getField().getName())
                                .values(Arrays.asList(v.getMetadataValues().split(",")))
                                .build()
                ).toList();

        return CategoryTreeResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentChain(parents)
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

}