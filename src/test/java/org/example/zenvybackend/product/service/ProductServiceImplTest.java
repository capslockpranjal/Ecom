package org.example.zenvybackend.product.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.category.entity.CategoryMetadataField;
import org.example.zenvybackend.category.entity.CategoryMetadataFieldValues;
import org.example.zenvybackend.category.entity.CategoryMetadataFieldValuesId;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.category.repository.CategoryMetadataFieldValuesRepository;
import org.example.zenvybackend.category.repository.CategoryRepository;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.exception.UnauthorizedException;
import org.example.zenvybackend.common.storage.ImageStorageService;
import org.example.zenvybackend.product.dto.request.AddProductRequest;
import org.example.zenvybackend.product.dto.request.AddProductVariationRequest;
import org.example.zenvybackend.product.dto.request.CustomerProductFilterDto;
import org.example.zenvybackend.product.dto.request.UpdateProductRequest;
import org.example.zenvybackend.product.dto.request.UpdateProductVariationRequest;
import org.example.zenvybackend.product.dto.response.CustomerProductDetailResponse;
import org.example.zenvybackend.product.dto.response.CustomerProductListItemResponse;
import org.example.zenvybackend.product.dto.response.ProductResponse;
import org.example.zenvybackend.product.dto.response.ProductVariationResponse;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.product.mapper.ProductMapper;
import org.example.zenvybackend.product.mapper.ProductVariationMapper;
import org.example.zenvybackend.product.repository.ProductRepository;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.example.zenvybackend.product.service.ProductEmailService;
import org.example.zenvybackend.product.service.impl.ProductServiceImpl;
import org.example.zenvybackend.security.service.CustomUserDetails;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductEmailService emailService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariationRepository productVariationRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductVariationMapper productVariationMapper;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMetadataFieldValuesRepository categoryMetadataFieldValuesRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private ProductServiceImpl productService;

    private UUID sellerId;
    private Seller seller;
    private Customer customer;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        User user = new User();
        user.setId(sellerId);
        user.setEmail("seller@example.com");
        user.setIsActive(true);

        seller = new Seller();
        seller.setUserId(sellerId);
        seller.setUser(user);

        customer = new Customer();
        customer.setUserId(sellerId);
        customer.setUser(user);

        CustomUserDetails userDetails = new CustomUserDetails(
                sellerId,
                user.getEmail(),
                List.of(new SimpleGrantedAuthority("ROLE_SELLER"))
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addProduct_savesInactiveProductWithDefaultFlagsAndSendsEmail() {
        AddProductRequest request = new AddProductRequest();
        request.setName("  Running Shoe  ");
        request.setBrand("  Nike  ");
        request.setDescription("  Daily trainer  ");
        request.setCategoryId(UUID.randomUUID());

        Category category = new Category();
        category.setId(request.getCategoryId());
        category.setName("Shoes");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentCategoryAndIsDeletedFalse(category)).thenReturn(false);
        when(productRepository.existsActiveDuplicate("Running Shoe", "Nike", category, seller)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        UUID productId = productService.addProduct(request);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product savedProduct = captor.getValue();

        assertNotNull(productId);
        assertEquals("Running Shoe", savedProduct.getName());
        assertEquals("Nike", savedProduct.getBrand());
        assertEquals("Daily trainer", savedProduct.getDescription());
        assertFalse(savedProduct.getIsCancellable());
        assertFalse(savedProduct.getIsReturnable());
        assertFalse(savedProduct.getIsActive());
        assertEquals(category, savedProduct.getCategory());
        assertEquals(seller, savedProduct.getSeller());
        verify(emailService).sendProductCreatedEmail(savedProduct);
    }

    @Test
    void addProduct_rejectsInactiveSeller() {
        seller.getUser().setIsActive(false);

        AddProductRequest request = new AddProductRequest();
        request.setName("Product");
        request.setBrand("Brand");
        request.setCategoryId(UUID.randomUUID());

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));

        assertThrows(UnauthorizedException.class, () -> productService.addProduct(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void addProduct_rejectsNonLeafCategory() {
        AddProductRequest request = new AddProductRequest();
        request.setName("Product");
        request.setBrand("Brand");
        request.setCategoryId(UUID.randomUUID());

        Category category = new Category();
        category.setId(request.getCategoryId());

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentCategoryAndIsDeletedFalse(category)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> productService.addProduct(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void addProduct_rejectsDuplicateProduct() {
        AddProductRequest request = new AddProductRequest();
        request.setName("  Product  ");
        request.setBrand(" Brand ");
        request.setCategoryId(UUID.randomUUID());

        Category category = new Category();
        category.setId(request.getCategoryId());

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(request.getCategoryId())).thenReturn(Optional.of(category));
        when(categoryRepository.existsByParentCategoryAndIsDeletedFalse(category)).thenReturn(false);
        when(productRepository.existsActiveDuplicate("Product", "Brand", category, seller)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> productService.addProduct(request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void addVariation_savesActiveVariationWhenMetadataMatchesCategory() throws Exception {
        UUID productId = UUID.randomUUID();
        AddProductVariationRequest request = new AddProductVariationRequest();
        request.setProductId(productId);
        request.setQuantityAvailable(5);
        request.setPrice(1299.0);
        request.setPrimaryImageName("shoe.JPG");
        LinkedHashMap<String, String> requestMetadata = new LinkedHashMap<>();
        requestMetadata.put("size", "M");
        requestMetadata.put("color", "Blue");
        request.setMetadata(requestMetadata);
        request.setSecondaryImages(List.of("  side.png ", "top.jpeg"));

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsActive(true);
        product.setIsDeleted(false);

        CategoryMetadataField sizeField = new CategoryMetadataField();
        sizeField.setId(UUID.randomUUID());
        sizeField.setName("size");

        CategoryMetadataField colorField = new CategoryMetadataField();
        colorField.setId(UUID.randomUUID());
        colorField.setName("color");

        CategoryMetadataFieldValues sizeValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(category.getId(), sizeField.getId()))
                .category(category)
                .field(sizeField)
                .metadataValues("S,M,L")
                .build();

        CategoryMetadataFieldValues colorValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(category.getId(), colorField.getId()))
                .category(category)
                .field(colorField)
                .metadataValues("Blue,Black")
                .build();

        LinkedHashMap<String, String> normalizedMetadata = new LinkedHashMap<>();
        normalizedMetadata.put("color", "Blue");
        normalizedMetadata.put("size", "M");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryMetadataFieldValuesRepository.findByCategoryWithField(category))
                .thenReturn(List.of(sizeValues, colorValues));
        when(objectMapper.readValue(eq("{\"color\":\"Blue\",\"size\":\"M\"}"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(normalizedMetadata);
        when(productVariationRepository.existsByProductAndMetadataAndIsDeletedFalse(product, "{\"color\":\"Blue\",\"size\":\"M\"}"))
                .thenReturn(false);
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product))
                .thenReturn(List.of());
        productService.addVariation(request);

        ArgumentCaptor<ProductVariation> captor = ArgumentCaptor.forClass(ProductVariation.class);
        verify(productVariationRepository).save(captor.capture());
        ProductVariation savedVariation = captor.getValue();

        assertEquals(product, savedVariation.getProduct());
        assertEquals(5, savedVariation.getQuantityAvailable());
        assertEquals(1299.0, savedVariation.getPrice());
        assertEquals("{\"color\":\"Blue\",\"size\":\"M\"}", savedVariation.getMetadata());
        assertEquals(Boolean.TRUE, savedVariation.getIsActive());
    }

    @Test
    void addVariation_rejectsEmptyMetadataMap() throws Exception {
        UUID productId = UUID.randomUUID();
        AddProductVariationRequest request = new AddProductVariationRequest();
        request.setProductId(productId);
        request.setQuantityAvailable(0);
        request.setPrice(0.0);
        request.setPrimaryImageName("image.png");
        request.setMetadata(new LinkedHashMap<>());

        Category category = new Category();
        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsActive(true);
        product.setIsDeleted(false);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryMetadataFieldValuesRepository.findByCategoryWithField(category)).thenReturn(List.of());
        when(objectMapper.readValue(eq("{}"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(new LinkedHashMap<>());

        assertThrows(BadRequestException.class, () -> productService.addVariation(request));
        verify(productVariationRepository, never()).save(any(ProductVariation.class));
    }

    @Test
    void addVariation_rejectsMetadataStructureMismatchAgainstExistingVariation() throws Exception {
        UUID productId = UUID.randomUUID();
        AddProductVariationRequest request = new AddProductVariationRequest();
        request.setProductId(productId);
        request.setQuantityAvailable(5);
        request.setPrice(10.0);
        request.setPrimaryImageName("image.png");
        LinkedHashMap<String, String> requestMetadata = new LinkedHashMap<>();
        requestMetadata.put("color", "Blue");
        requestMetadata.put("size", "M");
        request.setMetadata(requestMetadata);

        Category category = new Category();
        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsActive(true);
        product.setIsDeleted(false);

        CategoryMetadataField sizeField = new CategoryMetadataField();
        sizeField.setId(UUID.randomUUID());
        sizeField.setName("size");

        CategoryMetadataField colorField = new CategoryMetadataField();
        colorField.setId(UUID.randomUUID());
        colorField.setName("color");

        CategoryMetadataFieldValues sizeValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(UUID.randomUUID(), sizeField.getId()))
                .category(category)
                .field(sizeField)
                .metadataValues("M")
                .build();

        CategoryMetadataFieldValues colorValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(UUID.randomUUID(), colorField.getId()))
                .category(category)
                .field(colorField)
                .metadataValues("Blue")
                .build();

        ProductVariation existingVariation = new ProductVariation();
        existingVariation.setMetadata("{\"size\":\"M\"}");

        LinkedHashMap<String, String> currentMetadata = new LinkedHashMap<>();
        currentMetadata.put("color", "Blue");
        currentMetadata.put("size", "M");

        LinkedHashMap<String, String> existingMetadata = new LinkedHashMap<>();
        existingMetadata.put("size", "M");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryMetadataFieldValuesRepository.findByCategoryWithField(category))
                .thenReturn(List.of(sizeValues, colorValues));
        when(objectMapper.readValue(eq("{\"color\":\"Blue\",\"size\":\"M\"}"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(currentMetadata);
        when(objectMapper.readValue(eq("{\"size\":\"M\"}"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(existingMetadata);
        when(productVariationRepository.existsByProductAndMetadataAndIsDeletedFalse(product, "{\"color\":\"Blue\",\"size\":\"M\"}"))
                .thenReturn(false);
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product))
                .thenReturn(List.of(existingVariation));

        assertThrows(BadRequestException.class, () -> productService.addVariation(request));
        verify(productVariationRepository, never()).save(any(ProductVariation.class));
    }

    @Test
    void addVariation_allowsSubsetOfCategoryMetadataFieldsForFirstVariation() throws Exception {
        UUID productId = UUID.randomUUID();
        AddProductVariationRequest request = new AddProductVariationRequest();
        request.setProductId(productId);
        request.setQuantityAvailable(3);
        request.setPrice(499.0);
        request.setPrimaryImageName("image.png");
        LinkedHashMap<String, String> requestMetadata = new LinkedHashMap<>();
        requestMetadata.put("color", "Blue");
        request.setMetadata(requestMetadata);

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsActive(true);
        product.setIsDeleted(false);

        CategoryMetadataField sizeField = new CategoryMetadataField();
        sizeField.setId(UUID.randomUUID());
        sizeField.setName("size");

        CategoryMetadataField colorField = new CategoryMetadataField();
        colorField.setId(UUID.randomUUID());
        colorField.setName("color");

        CategoryMetadataFieldValues sizeValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(category.getId(), sizeField.getId()))
                .category(category)
                .field(sizeField)
                .metadataValues("S,M,L")
                .build();

        CategoryMetadataFieldValues colorValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(category.getId(), colorField.getId()))
                .category(category)
                .field(colorField)
                .metadataValues("Blue,Black")
                .build();

        LinkedHashMap<String, String> normalizedMetadata = new LinkedHashMap<>();
        normalizedMetadata.put("color", "Blue");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryMetadataFieldValuesRepository.findByCategoryWithField(category))
                .thenReturn(List.of(sizeValues, colorValues));
        when(objectMapper.readValue(eq("{\"color\":\"Blue\"}"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(normalizedMetadata);
        when(productVariationRepository.existsByProductAndMetadataAndIsDeletedFalse(product, "{\"color\":\"Blue\"}"))
                .thenReturn(false);
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product))
                .thenReturn(List.of());

        productService.addVariation(request);

        ArgumentCaptor<ProductVariation> captor = ArgumentCaptor.forClass(ProductVariation.class);
        verify(productVariationRepository).save(captor.capture());
        assertEquals("{\"color\":\"Blue\"}", captor.getValue().getMetadata());
    }

    @Test
    void addVariation_rejectsDuplicateVariationFromConstraintViolation() throws Exception {
        UUID productId = UUID.randomUUID();
        AddProductVariationRequest request = new AddProductVariationRequest();
        request.setProductId(productId);
        request.setQuantityAvailable(5);
        request.setPrice(10.0);
        request.setPrimaryImageName("image.png");
        LinkedHashMap<String, String> requestMetadata = new LinkedHashMap<>();
        requestMetadata.put("size", "M");
        request.setMetadata(requestMetadata);

        Category category = new Category();
        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsActive(true);
        product.setIsDeleted(false);

        CategoryMetadataField sizeField = new CategoryMetadataField();
        sizeField.setId(UUID.randomUUID());
        sizeField.setName("size");

        CategoryMetadataFieldValues sizeValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(UUID.randomUUID(), sizeField.getId()))
                .category(category)
                .field(sizeField)
                .metadataValues("M")
                .build();

        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("size", "M");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(categoryMetadataFieldValuesRepository.findByCategoryWithField(category)).thenReturn(List.of(sizeValues));
        when(objectMapper.readValue(eq("{\"size\":\"M\"}"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(metadata);
        when(productVariationRepository.existsByProductAndMetadataAndIsDeletedFalse(product, "{\"size\":\"M\"}"))
                .thenReturn(false);
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product))
                .thenReturn(List.of());
        when(productVariationRepository.save(any(ProductVariation.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(BadRequestException.class, () -> productService.addVariation(request));
    }

    @Test
    void getProducts_returnsSellerScopedPageWithCategoryDetails() {
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Shoes");

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSeller(seller);
        product.setCategory(category);
        product.setName("Runner");
        product.setBrand("Nike");
        product.setIsDeleted(false);

        ProductResponse response = ProductResponse.builder()
                .id(product.getId())
                .name("Runner")
                .brand("Nike")
                .categoryId(category.getId())
                .categoryName("Shoes")
                .build();

        PageRequestDto dto = new PageRequestDto();
        dto.setMax(5);
        dto.setOffset(0);
        dto.setSort("createdAt");
        dto.setOrder("desc");
        dto.setQuery("  run ");

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 5), 1);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findBySellerAndNameContainingIgnoreCaseAndIsDeletedFalse(eq(seller), eq("run"), any()))
                .thenReturn(page);
        when(productMapper.toResponse(product)).thenReturn(response);

        Object result = productService.getProducts(null, dto);

        Page<?> resultPage = (Page<?>) result;
        assertEquals(1, resultPage.getTotalElements());
        assertSame(response, resultPage.getContent().get(0));
    }

    @Test
    void getProducts_returnsSpecificOwnedProduct() {
        UUID productId = UUID.randomUUID();
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Shoes");

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsDeleted(false);

        ProductResponse response = ProductResponse.builder()
                .id(productId)
                .categoryId(category.getId())
                .categoryName("Shoes")
                .build();

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(response);

        Object result = productService.getProducts(productId, new PageRequestDto());

        List<?> resultList = (List<?>) result;
        assertEquals(1, resultList.size());
        assertSame(response, resultList.get(0));
    }

    @Test
    void getProducts_rejectsProductOwnedByAnotherSeller() {
        UUID productId = UUID.randomUUID();

        Seller otherSeller = new Seller();
        otherSeller.setUserId(UUID.randomUUID());
        otherSeller.setUser(new User());

        Product product = new Product();
        product.setId(productId);
        product.setSeller(otherSeller);
        product.setCategory(new Category());
        product.setIsDeleted(false);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedException.class, () -> productService.getProducts(productId, new PageRequestDto()));
    }

    @Test
    void getProducts_rejectsDeletedProductLookup() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(new Category());
        product.setIsDeleted(true);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> productService.getProducts(productId, new PageRequestDto()));
    }

    @Test
    void deleteProduct_softDeletesOwnedProduct() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(new Category());
        product.setIsDeleted(false);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        productService.deleteProduct(productId);

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void deleteProduct_rejectsProductOwnedByAnotherSeller() {
        UUID productId = UUID.randomUUID();

        Seller otherSeller = new Seller();
        otherSeller.setUserId(UUID.randomUUID());
        otherSeller.setUser(new User());

        Product product = new Product();
        product.setId(productId);
        product.setSeller(otherSeller);
        product.setCategory(new Category());
        product.setIsDeleted(false);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedException.class, () -> productService.deleteProduct(productId));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void deleteProduct_rejectsInvalidProductId() {
        UUID productId = UUID.randomUUID();

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(productId));
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void updateProduct_updatesProvidedFieldsForOwnedProduct() {
        UUID productId = UUID.randomUUID();

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setBrand("Nike");
        product.setName("Runner");
        product.setDescription("Old");
        product.setIsCancellable(false);
        product.setIsReturnable(false);
        product.setIsDeleted(false);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("  Runner Pro ");
        request.setDescription("  Better cushioning ");
        request.setIsCancellable(true);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsActiveDuplicate("Runner Pro", "Nike", category, seller)).thenReturn(false);

        productService.updateProduct(productId, request);

        assertEquals("Runner Pro", product.getName());
        assertEquals("Better cushioning", product.getDescription());
        assertEquals(Boolean.TRUE, product.getIsCancellable());
        assertEquals(Boolean.FALSE, product.getIsReturnable());
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_rejectsRequestWithoutUpdatableFields() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(new Category());
        product.setBrand("Nike");
        product.setName("Runner");
        product.setIsDeleted(false);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> productService.updateProduct(productId, new UpdateProductRequest()));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_rejectsDuplicateUpdatedName() {
        UUID productId = UUID.randomUUID();

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setBrand("Nike");
        product.setName("Runner");
        product.setIsDeleted(false);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setName(" Runner Max ");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsActiveDuplicate("Runner Max", "Nike", category, seller)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> productService.updateProduct(productId, request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_rejectsProductOwnedByAnotherSeller() {
        UUID productId = UUID.randomUUID();

        Seller otherSeller = new Seller();
        otherSeller.setUserId(UUID.randomUUID());
        otherSeller.setUser(new User());

        Product product = new Product();
        product.setId(productId);
        product.setSeller(otherSeller);
        product.setCategory(new Category());
        product.setBrand("Nike");
        product.setName("Runner");
        product.setIsDeleted(false);

        UpdateProductRequest request = new UpdateProductRequest();
        request.setDescription("Updated");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(UnauthorizedException.class, () -> productService.updateProduct(productId, request));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateVariation_updatesProvidedFieldsForOwnedVariation() throws Exception {
        UUID variationId = UUID.randomUUID();

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsActive(true);
        product.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(variationId);
        variation.setProduct(product);
        variation.setQuantityAvailable(5);
        variation.setPrice(10.0);
        variation.setMetadata("{\"size\":\"M\"}");
        variation.setIsActive(true);
        variation.setIsDeleted(false);

        CategoryMetadataField sizeField = new CategoryMetadataField();
        sizeField.setId(UUID.randomUUID());
        sizeField.setName("size");

        CategoryMetadataFieldValues sizeValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(category.getId(), sizeField.getId()))
                .category(category)
                .field(sizeField)
                .metadataValues("S,M,L")
                .build();

        UpdateProductVariationRequest request = new UpdateProductVariationRequest();
        request.setQuantityAvailable(9);
        request.setPrice(15.5);
        LinkedHashMap<String, String> requestMetadata = new LinkedHashMap<>();
        requestMetadata.put("size", "L");
        request.setMetadata(requestMetadata);
        request.setPrimaryImageName(" hero.JPG ");
        request.setSecondaryImages(List.of("side.png"));
        request.setIsActive(false);

        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("size", "L");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productVariationRepository.findByIdAndIsDeletedFalse(variationId)).thenReturn(Optional.of(variation));
        when(categoryMetadataFieldValuesRepository.findByCategoryWithField(category)).thenReturn(List.of(sizeValues));
        when(objectMapper.readValue(eq("{\"size\":\"L\"}"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(metadata);
        when(productVariationRepository.findByProductAndIsDeletedFalse(product)).thenReturn(List.of(variation));
        when(productVariationRepository.existsByProductAndMetadataAndIsDeletedFalseAndIdNot(product, "{\"size\":\"L\"}", variationId))
                .thenReturn(false);
        productService.updateVariation(variationId, request);

        assertEquals(9, variation.getQuantityAvailable());
        assertEquals(15.5, variation.getPrice());
        assertEquals("{\"size\":\"L\"}", variation.getMetadata());
        assertEquals(Boolean.FALSE, variation.getIsActive());
        verify(productVariationRepository).save(variation);
    }

    @Test
    void updateVariation_rejectsRequestWithoutFields() {
        UUID variationId = UUID.randomUUID();

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSeller(seller);
        product.setCategory(new Category());
        product.setIsActive(true);
        product.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(variationId);
        variation.setProduct(product);
        variation.setIsDeleted(false);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productVariationRepository.findByIdAndIsDeletedFalse(variationId)).thenReturn(Optional.of(variation));

        assertThrows(BadRequestException.class, () -> productService.updateVariation(variationId, new UpdateProductVariationRequest()));
        verify(productVariationRepository, never()).save(any(ProductVariation.class));
    }

    @Test
    void updateVariation_rejectsNegativePrice() {
        UUID variationId = UUID.randomUUID();

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSeller(seller);
        product.setCategory(new Category());
        product.setIsActive(true);
        product.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(variationId);
        variation.setProduct(product);
        variation.setIsDeleted(false);

        UpdateProductVariationRequest request = new UpdateProductVariationRequest();
        request.setPrice(-1.0);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productVariationRepository.findByIdAndIsDeletedFalse(variationId)).thenReturn(Optional.of(variation));

        assertThrows(BadRequestException.class, () -> productService.updateVariation(variationId, request));
        verify(productVariationRepository, never()).save(any(ProductVariation.class));
    }

    @Test
    void updateVariation_rejectsDuplicateMetadataForAnotherVariation() throws Exception {
        UUID variationId = UUID.randomUUID();

        Category category = new Category();
        category.setId(UUID.randomUUID());

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsActive(true);
        product.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(variationId);
        variation.setProduct(product);
        variation.setMetadata("{\"size\":\"M\"}");
        variation.setIsDeleted(false);

        ProductVariation otherVariation = new ProductVariation();
        otherVariation.setId(UUID.randomUUID());
        otherVariation.setProduct(product);
        otherVariation.setMetadata("{\"size\":\"L\"}");
        otherVariation.setIsDeleted(false);

        CategoryMetadataField sizeField = new CategoryMetadataField();
        sizeField.setId(UUID.randomUUID());
        sizeField.setName("size");

        CategoryMetadataFieldValues sizeValues = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(category.getId(), sizeField.getId()))
                .category(category)
                .field(sizeField)
                .metadataValues("M,L")
                .build();

        UpdateProductVariationRequest request = new UpdateProductVariationRequest();
        LinkedHashMap<String, String> requestMetadata = new LinkedHashMap<>();
        requestMetadata.put("size", "L");
        request.setMetadata(requestMetadata);

        LinkedHashMap<String, String> metadata = new LinkedHashMap<>();
        metadata.put("size", "L");

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productVariationRepository.findByIdAndIsDeletedFalse(variationId)).thenReturn(Optional.of(variation));
        when(categoryMetadataFieldValuesRepository.findByCategoryWithField(category)).thenReturn(List.of(sizeValues));
        when(objectMapper.readValue(eq("{\"size\":\"L\"}"), any(com.fasterxml.jackson.core.type.TypeReference.class)))
                .thenReturn(metadata);
        when(productVariationRepository.findByProductAndIsDeletedFalse(product)).thenReturn(List.of(variation, otherVariation));
        when(productVariationRepository.existsByProductAndMetadataAndIsDeletedFalseAndIdNot(product, "{\"size\":\"L\"}", variationId))
                .thenReturn(true);

        assertThrows(BadRequestException.class, () -> productService.updateVariation(variationId, request));
        verify(productVariationRepository, never()).save(any(ProductVariation.class));
    }

    @Test
    void updateVariation_rejectsVariationOwnedByAnotherSeller() {
        UUID variationId = UUID.randomUUID();

        Seller otherSeller = new Seller();
        otherSeller.setUserId(UUID.randomUUID());
        otherSeller.setUser(new User());

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSeller(otherSeller);
        product.setCategory(new Category());
        product.setIsActive(true);
        product.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(variationId);
        variation.setProduct(product);
        variation.setIsDeleted(false);

        UpdateProductVariationRequest request = new UpdateProductVariationRequest();
        request.setIsActive(false);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productVariationRepository.findByIdAndIsDeletedFalse(variationId)).thenReturn(Optional.of(variation));

        assertThrows(UnauthorizedException.class, () -> productService.updateVariation(variationId, request));
        verify(productVariationRepository, never()).save(any(ProductVariation.class));
    }

    @Test
    void getProductVariations_returnsSellerScopedVariationPage() {
        UUID productId = UUID.randomUUID();
        Category category = new Category();
        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(UUID.randomUUID());
        variation.setProduct(product);
        variation.setIsDeleted(false);
        variation.setIsActive(true);

        ProductVariationResponse response = ProductVariationResponse.builder()
                .id(variation.getId())
                .build();

        PageRequestDto dto = new PageRequestDto();
        dto.setMax(10);
        dto.setOffset(0);
        dto.setSort("createdAt");
        dto.setOrder("desc");
        dto.setQuery("  Blue ");

        Page<ProductVariation> page = new PageImpl<>(List.of(variation), PageRequest.of(0, 10), 1);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productVariationRepository.searchByProductAndQuery(eq(product), eq("Blue"), any()))
                .thenReturn(page);
        when(productVariationMapper.toResponse(variation)).thenReturn(response);

        Object result = productService.getProductVariations(productId, null, dto);

        Page<?> resultPage = (Page<?>) result;
        assertEquals(1, resultPage.getTotalElements());
        assertSame(response, resultPage.getContent().get(0));
    }

    @Test
    void getProductVariations_defaultsToAllowedSortFieldWhenSortNotProvided() {
        UUID productId = UUID.randomUUID();
        Category category = new Category();
        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(UUID.randomUUID());
        variation.setProduct(product);
        variation.setIsDeleted(false);
        variation.setIsActive(true);

        ProductVariationResponse response = ProductVariationResponse.builder()
                .id(variation.getId())
                .build();

        Page<ProductVariation> page = new PageImpl<>(List.of(variation), PageRequest.of(0, 10), 1);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(eq(product), any()))
                .thenReturn(page);
        when(productVariationMapper.toResponse(variation)).thenReturn(response);

        Object result = productService.getProductVariations(productId, null, new PageRequestDto());

        Page<?> resultPage = (Page<?>) result;
        assertEquals(1, resultPage.getTotalElements());
        assertSame(response, resultPage.getContent().get(0));
    }

    @Test
    void getProductVariations_returnsSpecificVariationForOwnedProduct() {
        UUID productId = UUID.randomUUID();
        UUID variationId = UUID.randomUUID();

        Category category = new Category();
        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(category);
        product.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(variationId);
        variation.setProduct(product);
        variation.setIsDeleted(false);
        variation.setIsActive(true);

        ProductVariationResponse response = ProductVariationResponse.builder()
                .id(variationId)
                .build();

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productVariationRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(variationId))
                .thenReturn(Optional.of(variation));
        when(productVariationMapper.toResponse(variation)).thenReturn(response);

        Object result = productService.getProductVariations(productId, variationId, new PageRequestDto());

        List<?> resultList = (List<?>) result;
        assertEquals(1, resultList.size());
        assertSame(response, resultList.get(0));
    }

    @Test
    void getProductVariations_rejectsDeletedProduct() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setSeller(seller);
        product.setCategory(new Category());
        product.setIsDeleted(true);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> productService.getProductVariations(productId, null, new PageRequestDto()));
    }

    @Test
    void getProductVariations_rejectsVariationThatDoesNotBelongToProduct() {
        UUID productId = UUID.randomUUID();
        UUID variationId = UUID.randomUUID();

        Product ownedProduct = new Product();
        ownedProduct.setId(productId);
        ownedProduct.setSeller(seller);
        ownedProduct.setCategory(new Category());
        ownedProduct.setIsDeleted(false);

        Product otherProduct = new Product();
        otherProduct.setId(UUID.randomUUID());
        otherProduct.setSeller(seller);
        otherProduct.setCategory(new Category());
        otherProduct.setIsDeleted(false);

        ProductVariation variation = new ProductVariation();
        variation.setId(variationId);
        variation.setProduct(otherProduct);
        variation.setIsDeleted(false);
        variation.setIsActive(true);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(productRepository.findById(productId)).thenReturn(Optional.of(ownedProduct));
        when(productVariationRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(variationId))
                .thenReturn(Optional.of(variation));

        assertThrows(BadRequestException.class, () -> productService.getProductVariations(productId, variationId, new PageRequestDto()));
    }

    @Test
    void getCustomerProduct_returnsActiveProductWithActiveVariations() {
        UUID productId = UUID.randomUUID();
        Category parentCategory = new Category();
        parentCategory.setId(UUID.randomUUID());
        parentCategory.setName("Fashion");

        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Shoes");
        category.setParentCategory(parentCategory);

        Product product = new Product();
        product.setId(productId);
        product.setName("Runner");
        product.setDescription("Daily shoe");
        product.setBrand("Nike");
        product.setIsCancellable(true);
        product.setIsReturnable(true);
        product.setIsActive(true);
        product.setIsDeleted(false);
        product.setCategory(category);

        ProductVariation variation = new ProductVariation();
        variation.setId(UUID.randomUUID());
        variation.setProduct(product);
        variation.setIsActive(true);
        variation.setIsDeleted(false);

        ProductVariationResponse variationResponse = ProductVariationResponse.builder()
                .id(variation.getId())
                .build();

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product))
                .thenReturn(List.of(variation));
        when(productVariationMapper.toResponse(variation)).thenReturn(variationResponse);

        CustomerProductDetailResponse response = productService.getCustomerProduct(productId);

        assertEquals(productId, response.getId());
        assertEquals("Runner", response.getName());
        assertEquals("Shoes", response.getCategory().getName());
        assertEquals(1, response.getCategory().getParentChain().size());
        assertEquals("Fashion", response.getCategory().getParentChain().get(0).getName());
        assertEquals(1, response.getVariations().size());
        assertSame(variationResponse, response.getVariations().get(0));
    }

    @Test
    void getCustomerProduct_rejectsInactiveProduct() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setIsActive(false);
        product.setIsDeleted(false);
        product.setCategory(new Category());

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> productService.getCustomerProduct(productId));
    }

    @Test
    void getCustomerProduct_rejectsProductWithoutActiveVariations() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setIsActive(true);
        product.setIsDeleted(false);
        product.setCategory(new Category());

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product))
                .thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> productService.getCustomerProduct(productId));
    }

    @Test
    void getCustomerProduct_rejectsInactiveCustomer() {
        UUID productId = UUID.randomUUID();
        customer.getUser().setIsActive(false);

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));

        assertThrows(UnauthorizedException.class, () -> productService.getCustomerProduct(productId));
    }

    @Test
    void getCustomerProducts_returnsActiveProductsForCategoryAndDescendants() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Fashion");

        Category childCategory = new Category();
        childCategory.setId(UUID.randomUUID());
        childCategory.setName("Shoes");
        childCategory.setParentCategory(category);

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Runner");
        product.setDescription("Daily shoe");
        product.setBrand("Nike");
        product.setIsCancellable(true);
        product.setIsReturnable(true);
        product.setCategory(childCategory);

        ProductVariation variation = new ProductVariation();
        variation.setId(UUID.randomUUID());
        variation.setProduct(product);
        variation.setIsActive(true);
        variation.setIsDeleted(false);

        ProductVariation anotherVariation = new ProductVariation();
        anotherVariation.setId(UUID.randomUUID());
        anotherVariation.setProduct(product);
        anotherVariation.setIsActive(true);
        anotherVariation.setIsDeleted(false);

        PageRequestDto dto = new PageRequestDto();
        dto.setMax(10);
        dto.setOffset(0);
        dto.setSort("name");
        dto.setOrder("asc");

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentCategoryAndIsDeletedFalse(category)).thenReturn(List.of(childCategory));
        when(categoryRepository.findByParentCategoryAndIsDeletedFalse(childCategory)).thenReturn(List.of());
        when(productRepository.findActiveCustomerVisibleProductsByCategories(any(), any())).thenReturn(page);
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product))
                .thenReturn(List.of(variation, anotherVariation));
        when(imageStorageService.getVariationPrimaryImageUrl(product.getId(), variation.getId()))
                .thenReturn("/files/products/" + product.getId() + "/variations/" + variation.getId() + "/primary");
        when(imageStorageService.getVariationPrimaryImageUrl(product.getId(), anotherVariation.getId()))
                .thenReturn("/files/products/" + product.getId() + "/variations/" + anotherVariation.getId() + "/primary");

        Object result = productService.getCustomerProducts(categoryId, dto, new CustomerProductFilterDto());

        Page<?> resultPage = (Page<?>) result;
        assertEquals(1, resultPage.getTotalElements());
        CustomerProductListItemResponse response = (CustomerProductListItemResponse) resultPage.getContent().get(0);
        assertEquals("Runner", response.getName());
        assertEquals("Shoes", response.getCategory().getName());
        assertEquals(
                List.of(
                        "/files/products/" + product.getId() + "/variations/" + variation.getId() + "/primary",
                        "/files/products/" + product.getId() + "/variations/" + anotherVariation.getId() + "/primary"
                ),
                response.getPrimaryImages()
        );
    }

    @Test
    void getCustomerProducts_rejectsInvalidCategory() {
        UUID categoryId = UUID.randomUUID();

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getCustomerProducts(categoryId, new PageRequestDto(), new CustomerProductFilterDto()));
    }

    @Test
    void getCustomerProducts_rejectsInactiveCustomer() {
        UUID categoryId = UUID.randomUUID();
        customer.getUser().setIsActive(false);

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));

        assertThrows(UnauthorizedException.class, () -> productService.getCustomerProducts(categoryId, new PageRequestDto(), new CustomerProductFilterDto()));
    }

    @Test
    void getSimilarCustomerProducts_returnsCategoryBasedActiveProductsExcludingCurrentProduct() {
        UUID productId = UUID.randomUUID();
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Shoes");

        Product currentProduct = new Product();
        currentProduct.setId(productId);
        currentProduct.setCategory(category);
        currentProduct.setIsActive(true);
        currentProduct.setIsDeleted(false);

        Product similarProduct = new Product();
        similarProduct.setId(UUID.randomUUID());
        similarProduct.setName("Runner Pro");
        similarProduct.setBrand("Nike");
        similarProduct.setCategory(category);
        similarProduct.setIsCancellable(true);
        similarProduct.setIsReturnable(true);

        ProductVariation variation = new ProductVariation();
        variation.setId(UUID.randomUUID());
        variation.setProduct(similarProduct);
        variation.setIsActive(true);
        variation.setIsDeleted(false);

        PageRequestDto dto = new PageRequestDto();
        dto.setMax(10);
        dto.setOffset(0);
        dto.setSort("name");
        dto.setOrder("asc");

        Page<Product> page = new PageImpl<>(List.of(similarProduct), PageRequest.of(0, 10), 1);

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(currentProduct));
        when(productRepository.findSimilarActiveCustomerVisibleProducts(eq(category), eq(productId), any()))
                .thenReturn(page);
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(similarProduct))
                .thenReturn(List.of(variation));
        when(imageStorageService.getVariationPrimaryImageUrl(similarProduct.getId(), variation.getId()))
                .thenReturn("/files/products/" + similarProduct.getId() + "/variations/" + variation.getId() + "/primary");

        Object result = productService.getSimilarCustomerProducts(productId, dto);

        Page<?> resultPage = (Page<?>) result;
        assertEquals(1, resultPage.getTotalElements());
        CustomerProductListItemResponse response = (CustomerProductListItemResponse) resultPage.getContent().get(0);
        assertEquals(similarProduct.getId(), response.getId());
        assertEquals("Shoes", response.getCategory().getName());
        assertEquals(
                List.of("/files/products/" + similarProduct.getId() + "/variations/" + variation.getId() + "/primary"),
                response.getPrimaryImages()
        );
    }

    @Test
    void getSimilarCustomerProducts_rejectsInactiveCurrentProduct() {
        UUID productId = UUID.randomUUID();

        Product currentProduct = new Product();
        currentProduct.setId(productId);
        currentProduct.setCategory(new Category());
        currentProduct.setIsActive(false);
        currentProduct.setIsDeleted(false);

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.of(currentProduct));

        assertThrows(BadRequestException.class, () -> productService.getSimilarCustomerProducts(productId, new PageRequestDto()));
    }

    @Test
    void getSimilarCustomerProducts_rejectsInvalidProductId() {
        UUID productId = UUID.randomUUID();

        when(customerRepository.findById(sellerId)).thenReturn(Optional.of(customer));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getSimilarCustomerProducts(productId, new PageRequestDto()));
    }
}
