package org.example.zenvybackend.category.service;

import org.example.zenvybackend.category.dto.response.CustomerCategoryResponse;
import org.example.zenvybackend.category.dto.response.FilteringResponse;
import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.category.entity.CategoryMetadataField;
import org.example.zenvybackend.category.entity.CategoryMetadataFieldValues;
import org.example.zenvybackend.category.entity.CategoryMetadataFieldValuesId;
import org.example.zenvybackend.category.repository.CategoryMetadataFieldRepository;
import org.example.zenvybackend.category.repository.CategoryMetadataFieldValuesRepository;
import org.example.zenvybackend.category.repository.CategoryRepository;
import org.example.zenvybackend.category.service.impl.CategoryServiceImpl;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.exception.UnauthorizedException;
import org.example.zenvybackend.product.repository.ProductRepository;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.example.zenvybackend.security.service.CustomUserDetails;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryMetadataFieldRepository fieldRepository;

    @Mock
    private CategoryMetadataFieldValuesRepository valuesRepository;

    @Mock
    private ProductVariationRepository productVariationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private UUID customerId;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        User user = new User();
        user.setId(customerId);
        user.setEmail("customer@example.com");
        user.setIsActive(true);

        customer = new Customer();
        customer.setUserId(customerId);
        customer.setUser(user);

        CustomUserDetails userDetails = new CustomUserDetails(
                customerId,
                user.getEmail(),
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
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
    void getCustomerCategories_returnsRootCategoriesWhenNoIdProvided() {
        Category root = new Category();
        root.setId(UUID.randomUUID());
        root.setName("Fashion");

        CategoryMetadataField field = new CategoryMetadataField();
        field.setId(UUID.randomUUID());
        field.setName("size");

        CategoryMetadataFieldValues values = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(root.getId(), field.getId()))
                .category(root)
                .field(field)
                .metadataValues("S,M,L")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(categoryRepository.findByParentCategoryIsNullAndIsDeletedFalse()).thenReturn(List.of(root));
        when(categoryRepository.findByParentCategoryAndIsDeletedFalse(root)).thenReturn(List.of());
        when(valuesRepository.findByCategoryWithField(root)).thenReturn(List.of(values));
        when(productRepository.findDistinctBrandsByCategory(root)).thenReturn(List.of("Nike"));
        when(productVariationRepository.findMinPriceByCategory(root)).thenReturn(10.0);
        when(productVariationRepository.findMaxPriceByCategory(root)).thenReturn(100.0);

        List<CustomerCategoryResponse> response = categoryService.getCustomerCategories(null);

        assertEquals(1, response.size());
        assertEquals("Fashion", response.get(0).getName());
        assertEquals(List.of("Nike"), response.get(0).getBrands());
        assertEquals(10.0, response.get(0).getMinPrice());
        assertEquals(100.0, response.get(0).getMaxPrice());
    }

    @Test
    void getCustomerCategories_rejectsInvalidCategoryId() {
        UUID categoryId = UUID.randomUUID();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.getCustomerCategories(categoryId));
    }

    @Test
    void getFilteringData_returnsCompiledMetadataBrandsAndPriceRange() {
        UUID categoryId = UUID.randomUUID();

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Shoes");

        CategoryMetadataField field = new CategoryMetadataField();
        field.setId(UUID.randomUUID());
        field.setName("size");

        CategoryMetadataFieldValues values = CategoryMetadataFieldValues.builder()
                .id(new CategoryMetadataFieldValuesId(categoryId, field.getId()))
                .category(category)
                .field(field)
                .metadataValues("S,M,L")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByParentCategoryAndIsDeletedFalse(category)).thenReturn(List.of());
        when(valuesRepository.findByCategoryWithField(category)).thenReturn(List.of(values));
        when(productVariationRepository.findMetadataByCategory(categoryId)).thenReturn(List.of("{\"size\":\"M\"}"));
        when(productRepository.findDistinctBrandsByCategory(category)).thenReturn(List.of("Nike"));
        when(productVariationRepository.findMinPriceByCategory(category)).thenReturn(50.0);
        when(productVariationRepository.findMaxPriceByCategory(category)).thenReturn(120.0);

        FilteringResponse response = categoryService.getFilteringData(categoryId);

        assertEquals(List.of("Nike"), response.getBrands());
        assertEquals(50.0, response.getMinPrice());
        assertEquals(120.0, response.getMaxPrice());
        assertEquals(1, response.getMetadata().size());
        assertEquals("size", response.getMetadata().get(0).getName());
        assertEquals(List.of("M"), response.getMetadata().get(0).getValues());
    }

    @Test
    void customerCategoryApisRejectInactiveCustomer() {
        customer.getUser().setIsActive(false);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        assertThrows(UnauthorizedException.class, () -> categoryService.getCustomerCategories(null));
        assertThrows(UnauthorizedException.class, () -> categoryService.getFilteringData(UUID.randomUUID()));
    }
}
