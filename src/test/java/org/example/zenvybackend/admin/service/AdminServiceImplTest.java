package org.example.zenvybackend.admin.service;

import org.example.zenvybackend.admin.dto.AdminProductResponse;
import org.example.zenvybackend.admin.service.impl.AdminServiceImpl;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.category.repository.CategoryRepository;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.storage.ImageStorageService;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.product.repository.ProductRepository;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.example.zenvybackend.user.service.UserEmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private UserEmailService emailService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductVariationRepository productVariationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ImageStorageService imageStorageService;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Test
    void getProducts_returnsPagedActiveProducts() {
        UUID sellerId = UUID.randomUUID();
        Category category = new Category();
        category.setId(UUID.randomUUID());
        category.setName("Shoes");

        User user = new User();
        user.setId(sellerId);

        Seller seller = new Seller();
        seller.setUserId(sellerId);
        seller.setUser(user);

        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Runner");
        product.setBrand("Nike");
        product.setCategory(category);
        product.setSeller(seller);
        product.setIsActive(true);

        ProductVariation variation = new ProductVariation();
        variation.setId(UUID.randomUUID());
        variation.setProduct(product);

        PageRequestDto dto = new PageRequestDto();
        dto.setMax(10);
        dto.setOffset(0);
        dto.setSort("name");
        dto.setOrder("asc");

        Page<Product> page = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);

        when(productRepository.findAdminVisibleProducts(eq(null), eq(null), any())).thenReturn(page);
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product)).thenReturn(List.of(variation));
        when(imageStorageService.getVariationPrimaryImageUrl(product.getId(), variation.getId()))
                .thenReturn("/files/products/" + product.getId() + "/variations/" + variation.getId() + "/primary");

        Object result = adminService.getProducts(null, null, null, dto);

        Page<?> resultPage = (Page<?>) result;
        assertEquals(1, resultPage.getTotalElements());
        AdminProductResponse response = (AdminProductResponse) resultPage.getContent().get(0);
        assertEquals("Runner", response.getName());
        assertEquals(
                List.of("/files/products/" + product.getId() + "/variations/" + variation.getId() + "/primary"),
                response.getPrimaryImages()
        );
    }

    @Test
    void getProducts_returnsSpecificProductWhenFiltersMatch() {
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Shoes");

        User user = new User();
        user.setId(sellerId);

        Seller seller = new Seller();
        seller.setUserId(sellerId);
        seller.setUser(user);

        Product product = new Product();
        product.setId(productId);
        product.setName("Runner");
        product.setBrand("Nike");
        product.setCategory(category);
        product.setSeller(seller);
        product.setIsActive(true);

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.of(seller));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(productRepository.findByIdAndIsDeletedFalseWithDetails(productId)).thenReturn(Optional.of(product));
        when(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product)).thenReturn(List.of());

        Object result = adminService.getProducts(productId, sellerId, categoryId, new PageRequestDto());

        List<?> responses = (List<?>) result;
        assertEquals(1, responses.size());
        assertEquals(productId, ((AdminProductResponse) responses.get(0)).getId());
    }

    @Test
    void getProducts_rejectsInvalidSellerFilter() {
        UUID sellerId = UUID.randomUUID();

        when(sellerRepository.findById(sellerId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.getProducts(null, sellerId, null, new PageRequestDto()));
    }

    @Test
    void getProducts_rejectsProductThatDoesNotMatchProvidedCategoryFilter() {
        UUID productId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category actualCategory = new Category();
        actualCategory.setId(UUID.randomUUID());

        Category requestedCategory = new Category();
        requestedCategory.setId(categoryId);

        Seller seller = new Seller();
        seller.setUserId(UUID.randomUUID());
        seller.setUser(new User());

        Product product = new Product();
        product.setId(productId);
        product.setCategory(actualCategory);
        product.setSeller(seller);
        product.setIsActive(true);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(requestedCategory));
        when(productRepository.findByIdAndIsDeletedFalseWithDetails(productId)).thenReturn(Optional.of(product));

        assertThrows(ResourceNotFoundException.class, () -> adminService.getProducts(productId, null, categoryId, new PageRequestDto()));
    }

    @Test
    void activateProduct_activatesInactiveProductAndSendsEmail() {
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        Category category = new Category();
        category.setName("Shoes");

        User user = new User();
        user.setId(sellerId);
        user.setEmail("seller@example.com");

        Seller seller = new Seller();
        seller.setUserId(sellerId);
        seller.setUser(user);

        Product product = new Product();
        product.setId(productId);
        product.setName("Runner");
        product.setBrand("Nike");
        product.setCategory(category);
        product.setSeller(seller);
        product.setIsActive(false);

        when(productRepository.findByIdAndIsDeletedFalseWithDetails(productId)).thenReturn(Optional.of(product));

        adminService.activateProduct(productId);

        assertEquals(Boolean.TRUE, product.getIsActive());
        verify(productRepository).save(product);
        verify(emailService).sendEmail(eq("seller@example.com"), eq("Product Activated"), any());
    }

    @Test
    void activateProduct_rejectsAlreadyActiveProduct() {
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setIsActive(true);
        product.setCategory(new Category());
        product.setSeller(new Seller());

        when(productRepository.findByIdAndIsDeletedFalseWithDetails(productId)).thenReturn(Optional.of(product));

        assertThrows(BadRequestException.class, () -> adminService.activateProduct(productId));
    }

    @Test
    void deactivateProduct_deactivatesActiveProductAndSendsEmail() {
        UUID productId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        Category category = new Category();
        category.setName("Shoes");

        User user = new User();
        user.setId(sellerId);
        user.setEmail("seller@example.com");

        Seller seller = new Seller();
        seller.setUserId(sellerId);
        seller.setUser(user);

        Product product = new Product();
        product.setId(productId);
        product.setName("Runner");
        product.setBrand("Nike");
        product.setCategory(category);
        product.setSeller(seller);
        product.setIsActive(true);

        when(productRepository.findByIdAndIsDeletedFalseWithDetails(productId)).thenReturn(Optional.of(product));

        adminService.deactivateProduct(productId);

        assertEquals(Boolean.FALSE, product.getIsActive());
        verify(productRepository).save(product);
        verify(emailService).sendEmail(eq("seller@example.com"), eq("Product Deactivated"), any());
    }

    @Test
    void deactivateProduct_rejectsInvalidProductId() {
        UUID productId = UUID.randomUUID();

        when(productRepository.findByIdAndIsDeletedFalseWithDetails(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> adminService.deactivateProduct(productId));
    }
}
