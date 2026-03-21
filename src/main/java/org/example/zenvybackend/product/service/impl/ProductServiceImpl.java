package org.example.zenvybackend.product.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.category.dto.response.ParentCategoryResponse;
import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.category.entity.CategoryMetadataFieldValues;
import org.example.zenvybackend.category.repository.CategoryMetadataFieldValuesRepository;
import org.example.zenvybackend.category.repository.CategoryRepository;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.exception.UnauthorizedException;
import org.example.zenvybackend.common.storage.ImageStorageService;
import org.example.zenvybackend.common.util.JsonUtil;
import org.example.zenvybackend.common.util.PageUtils;
import org.example.zenvybackend.product.dto.request.AddProductRequest;
import org.example.zenvybackend.product.dto.request.AddProductVariationRequest;
import org.example.zenvybackend.product.dto.request.UpdateProductRequest;
import org.example.zenvybackend.product.dto.request.UpdateProductVariationRequest;
import org.example.zenvybackend.product.dto.response.CustomerProductCategoryResponse;
import org.example.zenvybackend.product.dto.response.CustomerProductDetailResponse;
import org.example.zenvybackend.product.dto.response.CustomerProductListItemResponse;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.product.mapper.ProductMapper;
import org.example.zenvybackend.product.mapper.ProductVariationMapper;
import org.example.zenvybackend.product.repository.ProductRepository;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.example.zenvybackend.product.service.ProductEmailService;
import org.example.zenvybackend.product.service.ProductService;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductServiceImpl implements ProductService {

    private static final List<String> PRODUCT_SORT_FIELDS = List.of("createdAt", "name", "brand", "isActive");
    private static final List<String> VARIATION_SORT_FIELDS = List.of("createdAt", "price", "quantityAvailable");
    private static final List<String> CUSTOMER_PRODUCT_SORT_FIELDS = List.of("createdAt", "name", "brand");
    private static final TypeReference<Map<String, String>> STRING_MAP_TYPE = new TypeReference<>() { };

    private final ProductEmailService emailService;
    private final ProductRepository productRepository;
    private final ProductVariationRepository productVariationRepository;
    private final ProductMapper productMapper;
    private final ProductVariationMapper productVariationMapper;
    private final SellerRepository sellerRepository;
    private final CustomerRepository customerRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;
    private final ObjectMapper objectMapper;
    private final ImageStorageService imageStorageService;

    @Override
    public UUID addProduct(AddProductRequest request) {
        Seller seller = getCurrentActiveSeller();
        String name = normalizeRequired(request.getName(), "Product name is required");
        String brand = normalizeRequired(request.getBrand(), "Brand is required");
        String description = normalizeOptional(request.getDescription());

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (categoryRepository.existsByParentCategoryAndIsDeletedFalse(category)) {
            throw new BadRequestException("Category must be a leaf category");
        }

        if (productRepository.existsActiveDuplicate(name, brand, category, seller)) {
            throw new BadRequestException("Product already exists");
        }

        Product product = Product.builder()
                .name(name)
                .brand(brand)
                .description(description)
                .isCancellable(Boolean.TRUE.equals(request.getIsCancellable()))
                .isReturnable(Boolean.TRUE.equals(request.getIsReturnable()))
                .category(category)
                .seller(seller)
                .isActive(false)
                .build();

        Product savedProduct;
        try {
            savedProduct = productRepository.save(product);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Product already exists");
        }

        try {
            emailService.sendProductCreatedEmail(savedProduct);
        } catch (Exception ex) {
            log.error("Failed to send product creation email for product {}", savedProduct.getId(), ex);
        }

        return savedProduct.getId();
    }

    @Override
    public void addVariation(AddProductVariationRequest request) {
        addVariation(request, null, null);
    }

    @Override
    public void addVariation(AddProductVariationRequest request, MultipartFile primaryImageFile, List<MultipartFile> secondaryImageFiles) {
        Seller seller = getCurrentActiveSeller();
        Product product = getOwnedProduct(request.getProductId(), seller);

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product is not active");
        }

        validatePrimaryImageInput(request.getPrimaryImageName(), primaryImageFile);

        String metadata;
        try {
            metadata = JsonUtil.normalize(request.getMetadata());
        } catch (RuntimeException ex) {
            throw new BadRequestException("Invalid metadata format");
        }

        Map<String, String> normalizedMetadata = readMetadata(metadata);
        validateMetadata(product.getCategory(), metadata);

        if (productVariationRepository.existsByProductAndMetadataAndIsDeletedFalse(product, metadata)) {
            throw new BadRequestException("Variation already exists");
        }

        List<ProductVariation> existingVariations =
                productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product);

        if (!existingVariations.isEmpty()) {
            Map<String, String> previousMetadata = readMetadata(existingVariations.get(0).getMetadata());

            if (!normalizedMetadata.keySet().equals(previousMetadata.keySet())) {
                throw new BadRequestException("Metadata structure must be same across variations");
            }
        }

        validateSecondaryImagesInput(request.getSecondaryImages(), secondaryImageFiles);

        ProductVariation variation = ProductVariation.builder()
                .product(product)
                .quantityAvailable(request.getQuantityAvailable())
                .price(request.getPrice())
                .metadata(metadata)
                .isActive(true)
                .build();

        try {
            ProductVariation savedVariation = productVariationRepository.save(variation);
            if (savedVariation == null) {
                savedVariation = variation;
            }
            storeVariationImages(product, savedVariation, primaryImageFile, secondaryImageFiles, false);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Variation already exists");
        }
    }

    @Override
    public void updateProduct(UUID productId, UpdateProductRequest request) {
        Seller seller = getCurrentActiveSeller();
        Product product = getOwnedProduct(productId, seller);

        boolean hasUpdatableField =
                request.getName() != null
                        || request.getDescription() != null
                        || request.getIsCancellable() != null
                        || request.getIsReturnable() != null;

        if (!hasUpdatableField) {
            throw new BadRequestException("At least one field must be provided for update");
        }

        if (request.getName() != null) {
            String updatedName = normalizeRequired(request.getName(), "Product name is required");
            boolean nameChanged = !updatedName.equalsIgnoreCase(product.getName().trim());

            if (nameChanged && productRepository.existsActiveDuplicate(
                    updatedName,
                    product.getBrand(),
                    product.getCategory(),
                    seller
            )) {
                throw new BadRequestException("Product already exists");
            }

            product.setName(updatedName);
        }

        if (request.getDescription() != null) {
            product.setDescription(normalizeOptional(request.getDescription()));
        }

        if (request.getIsCancellable() != null) {
            product.setIsCancellable(request.getIsCancellable());
        }

        if (request.getIsReturnable() != null) {
            product.setIsReturnable(request.getIsReturnable());
        }

        productRepository.save(product);
    }

    @Override
    public void updateVariation(UUID variationId, UpdateProductVariationRequest request) {
        updateVariation(variationId, request, null, null);
    }

    @Override
    public void updateVariation(UUID variationId, UpdateProductVariationRequest request, MultipartFile primaryImageFile, List<MultipartFile> secondaryImageFiles) {
        Seller seller = getCurrentActiveSeller();
        ProductVariation variation = getOwnedVariation(variationId, seller);
        Product product = variation.getProduct();

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product is not active");
        }

        boolean hasUpdatableField =
                request.getQuantityAvailable() != null
                        || request.getPrice() != null
                        || request.getMetadata() != null
                        || request.getPrimaryImageName() != null
                        || request.getSecondaryImages() != null
                        || request.getIsActive() != null
                        || hasFile(primaryImageFile)
                        || secondaryImageFiles != null;

        if (!hasUpdatableField) {
            throw new BadRequestException("At least one field must be provided for update");
        }

        if (request.getQuantityAvailable() != null) {
            if (request.getQuantityAvailable() < 0) {
                throw new BadRequestException("Quantity available must be 0 or more");
            }
            variation.setQuantityAvailable(request.getQuantityAvailable());
        }

        if (request.getPrice() != null) {
            if (request.getPrice() < 0) {
                throw new BadRequestException("Price must be 0 or more");
            }
            variation.setPrice(request.getPrice());
        }

        if (request.getPrimaryImageName() != null || hasFile(primaryImageFile)) {
            validatePrimaryImageInput(request.getPrimaryImageName(), primaryImageFile);
        }

        if (request.getSecondaryImages() != null || secondaryImageFiles != null) {
            validateSecondaryImagesInput(request.getSecondaryImages(), secondaryImageFiles);
        }

        if (request.getIsActive() != null) {
            variation.setIsActive(request.getIsActive());
        }

        if (request.getMetadata() != null) {
            String metadata;
            try {
                metadata = JsonUtil.normalize(request.getMetadata());
            } catch (RuntimeException ex) {
                throw new BadRequestException("Invalid metadata format");
            }

            Map<String, String> normalizedMetadata = readMetadata(metadata);
            validateMetadata(product.getCategory(), metadata);

            List<ProductVariation> existingVariations = productVariationRepository.findByProductAndIsDeletedFalse(product);

            Map<String, String> referenceMetadata = existingVariations.stream()
                    .filter(existing -> !existing.getId().equals(variation.getId()))
                    .findFirst()
                    .map(existing -> readMetadata(existing.getMetadata()))
                    .orElse(null);

            if (referenceMetadata != null && !normalizedMetadata.keySet().equals(referenceMetadata.keySet())) {
                throw new BadRequestException("Metadata structure must be same across variations");
            }

            if (productVariationRepository.existsByProductAndMetadataAndIsDeletedFalseAndIdNot(product, metadata, variation.getId())) {
                throw new BadRequestException("Variation already exists");
            }

            variation.setMetadata(metadata);
        }

        try {
            ProductVariation savedVariation = productVariationRepository.save(variation);
            if (savedVariation == null) {
                savedVariation = variation;
            }
            storeVariationImages(product, savedVariation, primaryImageFile, secondaryImageFiles, request.getSecondaryImages() != null);
        } catch (DataIntegrityViolationException ex) {
            throw new BadRequestException("Variation already exists");
        }
    }

    @Override
    public void deleteProduct(UUID productId) {
        Seller seller = getCurrentActiveSeller();
        Product product = getOwnedProduct(productId, seller);

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product already deleted");
        }

        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getProducts(UUID productId, PageRequestDto dto) {
        Seller seller = getCurrentActiveSeller();

        if (productId != null) {
            return List.of(productMapper.toResponse(getOwnedProduct(productId, seller)));
        }

        Pageable pageable = PageUtils.getPageable(dto, PRODUCT_SORT_FIELDS);
        Page<Product> productPage;

        if (dto.getQuery() != null && !dto.getQuery().isBlank()) {
            productPage = productRepository.findBySellerAndNameContainingIgnoreCaseAndIsDeletedFalse(
                    seller,
                    dto.getQuery().trim(),
                    pageable
            );
        } else {
            productPage = productRepository.findBySellerAndIsDeletedFalse(seller, pageable);
        }

        return productPage.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getProductVariations(UUID productId, UUID variationId, PageRequestDto dto) {
        Seller seller = getCurrentActiveSeller();
        Product product = getOwnedProduct(productId, seller);

        if (variationId != null) {
            ProductVariation variation = productVariationRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(variationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Variation not found"));

            if (!variation.getProduct().getId().equals(product.getId())) {
                throw new BadRequestException("Variation does not belong to this product");
            }

            return List.of(productVariationMapper.toResponse(variation));
        }

        Pageable pageable = PageUtils.getPageable(dto, VARIATION_SORT_FIELDS);

        if (dto.getQuery() != null && !dto.getQuery().isBlank()) {
            return productVariationRepository
                    .searchByProductAndQuery(product, dto.getQuery().trim(), pageable)
                    .map(productVariationMapper::toResponse);
        }

        return productVariationRepository
                .findByProductAndIsDeletedFalseAndIsActiveTrue(product, pageable)
                .map(productVariationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerProductDetailResponse getCustomerProduct(UUID productId) {
        getCurrentActiveCustomer();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (Boolean.TRUE.equals(product.getIsDeleted()) || !Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product is not available");
        }

        List<ProductVariation> activeVariations =
                productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product);

        if (activeVariations.isEmpty()) {
            throw new BadRequestException("Product has no active variations");
        }

        return CustomerProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .isCancellable(product.getIsCancellable())
                .isReturnable(product.getIsReturnable())
                .category(buildCustomerCategory(product.getCategory()))
                .variations(activeVariations.stream()
                        .map(productVariationMapper::toResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Object getCustomerProducts(UUID categoryId, PageRequestDto dto) {
        getCurrentActiveCustomer();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        List<Category> categories = getCategoryAndDescendants(category);
        Pageable pageable = PageUtils.getPageable(dto, CUSTOMER_PRODUCT_SORT_FIELDS);

        return productRepository.findActiveCustomerVisibleProductsByCategories(categories, pageable)
                .map(product -> CustomerProductListItemResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .description(product.getDescription())
                        .brand(product.getBrand())
                        .isCancellable(product.getIsCancellable())
                        .isReturnable(product.getIsReturnable())
                        .category(buildCustomerCategory(product.getCategory()))
                        .primaryImages(getPrimaryImages(product))
                        .build());
    }

    @Override
    @Transactional(readOnly = true)
    public Object getSimilarCustomerProducts(UUID productId, PageRequestDto dto) {
        getCurrentActiveCustomer();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (Boolean.TRUE.equals(product.getIsDeleted()) || !Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product is not available");
        }

        Pageable pageable = PageUtils.getPageable(dto, CUSTOMER_PRODUCT_SORT_FIELDS);

        return productRepository.findSimilarActiveCustomerVisibleProducts(product.getCategory(), product.getId(), pageable)
                .map(similarProduct -> CustomerProductListItemResponse.builder()
                        .id(similarProduct.getId())
                        .name(similarProduct.getName())
                        .description(similarProduct.getDescription())
                        .brand(similarProduct.getBrand())
                        .isCancellable(similarProduct.getIsCancellable())
                        .isReturnable(similarProduct.getIsReturnable())
                        .category(buildCustomerCategory(similarProduct.getCategory()))
                        .primaryImages(getPrimaryImages(similarProduct))
                        .build());
    }

    private Seller getCurrentActiveSeller() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        Seller seller = sellerRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (seller.getUser() == null || !Boolean.TRUE.equals(seller.getUser().getIsActive())) {
            throw new UnauthorizedException("Seller account is not activated");
        }

        return seller;
    }

    private Customer getCurrentActiveCustomer() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        Customer customer = customerRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (customer.getUser() == null || !Boolean.TRUE.equals(customer.getUser().getIsActive())) {
            throw new UnauthorizedException("Customer account is not activated");
        }

        return customer;
    }

    private Product getOwnedProduct(UUID productId, Seller seller) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSeller().getUserId().equals(seller.getUserId())) {
            throw new UnauthorizedException("Not your product");
        }

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product is deleted");
        }

        return product;
    }

    private ProductVariation getOwnedVariation(UUID variationId, Seller seller) {
        ProductVariation variation = productVariationRepository.findByIdAndIsDeletedFalse(variationId)
                .orElseThrow(() -> new ResourceNotFoundException("Variation not found"));

        Product product = variation.getProduct();
        if (!product.getSeller().getUserId().equals(seller.getUserId())) {
            throw new UnauthorizedException("Not your product variation");
        }

        if (Boolean.TRUE.equals(product.getIsDeleted())) {
            throw new BadRequestException("Product is deleted");
        }

        return variation;
    }

    private void validateMetadata(Category category, String metadataJson) {
        Map<String, Set<String>> allowedValues = categoryMetadataFieldValuesRepository.findByCategoryWithField(category)
                .stream()
                .collect(Collectors.toMap(
                        value -> value.getField().getName().trim().toLowerCase(),
                        this::extractAllowedValues,
                        (left, right) -> {
                            left.addAll(right);
                            return left;
                        },
                        LinkedHashMap::new
                ));

        Map<String, String> metadata = readMetadata(metadataJson);

        if (metadata.isEmpty()) {
            throw new BadRequestException("At least one metadata field is required");
        }

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String rawKey = entry.getKey();
            String rawValue = entry.getValue();

            if (rawKey == null || rawKey.trim().isEmpty()) {
                throw new BadRequestException("Invalid metadata field");
            }

            if (rawValue == null || rawValue.trim().isEmpty()) {
                throw new BadRequestException("Invalid value for " + rawKey);
            }

            String fieldName = rawKey.trim().toLowerCase();
            String fieldValue = rawValue.trim();

            if (!allowedValues.containsKey(fieldName)) {
                throw new BadRequestException("Invalid field: " + rawKey);
            }

            if (!allowedValues.get(fieldName).contains(fieldValue)) {
                throw new BadRequestException("Invalid value for " + rawKey);
            }
        }

        if (metadata.size() != allowedValues.size()) {
            throw new BadRequestException("Metadata structure mismatch");
        }
    }

    private Set<String> extractAllowedValues(CategoryMetadataFieldValues value) {
        return Arrays.stream(value.getMetadataValues().split(","))
                .map(String::trim)
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, String> readMetadata(String metadataJson) {
        try {
            return objectMapper.readValue(metadataJson, STRING_MAP_TYPE);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid metadata format");
        }
    }

    private boolean isSupportedImage(String imageName) {
        return imageName.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp)$");
    }

    private CustomerProductCategoryResponse buildCustomerCategory(Category category) {
        return CustomerProductCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .parentChain(buildParentChain(category))
                .build();
    }

    private List<String> getPrimaryImages(Product product) {
        return productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product)
                .stream()
                .map(variation -> imageStorageService.getVariationPrimaryImageUrl(product.getId(), variation.getId()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
    }

    private void storeVariationImages(
            Product product,
            ProductVariation variation,
            MultipartFile primaryImageFile,
            List<MultipartFile> secondaryImageFiles,
            boolean clearSecondaryImages
    ) {
        if (hasFile(primaryImageFile)) {
            imageStorageService.storeVariationPrimaryImage(product.getId(), variation.getId(), primaryImageFile);
        }

        if (secondaryImageFiles != null) {
            imageStorageService.replaceVariationSecondaryImages(product.getId(), variation.getId(), secondaryImageFiles);
        } else if (clearSecondaryImages) {
            imageStorageService.replaceVariationSecondaryImages(product.getId(), variation.getId(), List.of());
        }
    }

    private void validatePrimaryImageInput(String requestImageName, MultipartFile primaryImageFile) {
        if (hasFile(primaryImageFile)) {
            String primaryImage = normalizeRequired(primaryImageFile.getOriginalFilename(), "Primary image is required");
            if (!isSupportedImage(primaryImage)) {
                throw new BadRequestException("Invalid primary image format");
            }
            return;
        }

        String primaryImage = normalizeRequired(requestImageName, "Primary image is required");
        if (!isSupportedImage(primaryImage)) {
            throw new BadRequestException("Invalid primary image format");
        }
    }

    private void validateSecondaryImagesInput(List<String> imageNames, List<MultipartFile> imageFiles) {
        if (imageFiles != null) {
            for (MultipartFile image : imageFiles) {
                String imageName = normalizeRequired(image.getOriginalFilename(), "Secondary image is required");
                if (!isSupportedImage(imageName)) {
                    throw new BadRequestException("Invalid secondary image: " + imageName);
                }
            }
            return;
        }

        if (imageNames == null) {
            return;
        }

        for (String imageName : imageNames) {
            if (imageName == null || !isSupportedImage(imageName.trim())) {
                throw new BadRequestException("Invalid secondary image: " + imageName);
            }
        }
    }

    private boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private List<Category> getCategoryAndDescendants(Category category) {
        List<Category> categories = new java.util.ArrayList<>();
        categories.add(category);

        for (Category child : categoryRepository.findByParentCategoryAndIsDeletedFalse(category)) {
            categories.addAll(getCategoryAndDescendants(child));
        }

        return categories;
    }

    private List<ParentCategoryResponse> buildParentChain(Category category) {
        List<ParentCategoryResponse> parents = new java.util.ArrayList<>();
        Category current = category.getParentCategory();

        while (current != null) {
            parents.add(ParentCategoryResponse.builder()
                    .id(current.getId())
                    .name(current.getName())
                    .build());
            current = current.getParentCategory();
        }

        java.util.Collections.reverse(parents);
        return parents;
    }

    private String normalizeRequired(String value, String message) {
        String normalized = normalizeOptional(value);
        if (normalized == null) {
            throw new BadRequestException(message);
        }
        return normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
