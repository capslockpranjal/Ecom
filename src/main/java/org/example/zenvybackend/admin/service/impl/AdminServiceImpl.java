package org.example.zenvybackend.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.admin.dto.AdminCustomerResponse;
import org.example.zenvybackend.admin.dto.AdminProductCategoryResponse;
import org.example.zenvybackend.admin.dto.AdminProductResponse;
import org.example.zenvybackend.admin.dto.AdminSellerResponse;
import org.example.zenvybackend.admin.service.AdminService;
import org.example.zenvybackend.category.dto.request.PageRequestDto;
import org.example.zenvybackend.category.dto.response.ParentCategoryResponse;
import org.example.zenvybackend.category.entity.Category;
import org.example.zenvybackend.category.repository.CategoryRepository;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.response.PagedResponse;
import org.example.zenvybackend.common.storage.ImageStorageService;
import org.example.zenvybackend.common.util.PageUtils;
import org.example.zenvybackend.product.entity.Product;
import org.example.zenvybackend.product.entity.ProductVariation;
import org.example.zenvybackend.product.repository.ProductRepository;
import org.example.zenvybackend.product.repository.ProductVariationRepository;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.example.zenvybackend.user.service.EmailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final EmailService emailService;
    private final ProductRepository productRepository;
    private final ProductVariationRepository productVariationRepository;
    private final CategoryRepository categoryRepository;
    private final ImageStorageService imageStorageService;

    @Override
    public PagedResponse<AdminCustomerResponse> listCustomers(
            int pageOffset,
            int pageSize,
            String sort,
            String email
    ) {

        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));

        Page<Customer> page;

        if (email != null && !email.isBlank()) {
            page = customerRepository.findByUserEmailContainingIgnoreCase(email, pageable);
        } else {
            page = customerRepository.findAll(pageable);
        }

        if (page.isEmpty()) {
            throw new ResourceNotFoundException("No customers registered");
        }

        List<AdminCustomerResponse> customers = page.stream()
                .map(c -> {
                    User u = c.getUser();
                    return AdminCustomerResponse.builder()
                            .id(u.getId())
                            .fullName(u.getFirstName() + " " + u.getLastName())
                            .email(u.getEmail())
                            .isActive(u.getIsActive())
                            .build();
                })
                .toList();

        return PagedResponse.<AdminCustomerResponse>builder()
                .content(customers)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public PagedResponse<AdminSellerResponse> listSellers(
            int pageOffset,
            int pageSize,
            String sort,
            String email
    ) {

        Pageable pageable = PageRequest.of(pageOffset, pageSize, Sort.by(sort));

        Page<Seller> page;

        if (email != null && !email.isBlank()) {
            page = sellerRepository.findByUserEmailContainingIgnoreCase(email, pageable);
        } else {
            page = sellerRepository.findAll(pageable);
        }

        if (page.isEmpty()) {
            throw new ResourceNotFoundException("No sellers registered");
        }

        List<AdminSellerResponse> sellers = page.stream()
                .map(s -> {

                    User u = s.getUser();

                    String companyAddress = u.getAddresses()
                            .stream()
                            .findFirst()
                            .map(a -> a.getAddressLine() + ", "
                                    + a.getCity() + ", "
                                    + a.getState() + ", "
                                    + a.getCountry() + " - "
                                    + a.getZipCode())
                            .orElse(null);

                    return AdminSellerResponse.builder()
                            .id(u.getId())
                            .fullName(u.getFirstName() + " " + u.getLastName())
                            .email(u.getEmail())
                            .isActive(u.getIsActive())
                            .companyName(s.getCompanyName())
                            .companyAddress(companyAddress)
                            .companyContact(s.getCompanyContact())
                            .build();
                })
                .toList();

        return PagedResponse.<AdminSellerResponse>builder()
                .content(sellers)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    public void activateCustomer(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Customer account already active");
        }

        user.setIsActive(true);
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Account Activated",
                "Your customer account has been activated by the admin."
        );
    }

    @Override
    public void deactivateCustomer(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BadRequestException("Customer account already deactivated");
        }

        user.setIsActive(false);
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Account Deactivated",
                "Your customer account has been deactivated by the admin."
        );
    }

    @Override
    public void activateSeller(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        boolean isSeller = user.getRoles()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_SELLER"));

        if (!isSeller) {
            throw new BadRequestException("User is not a seller");
        }

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Seller account already active");
        }

        user.setIsActive(true);
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Seller Account Activated",
                "Your seller account has been activated by the admin."
        );
    }

    @Override
    public void deactivateSeller(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        boolean isSeller = user.getRoles()
                .stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_SELLER"));

        if (!isSeller) {
            throw new BadRequestException("User is not a seller");
        }

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new BadRequestException("Seller account already deactivated");
        }

        user.setIsActive(false);
        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Seller Account Deactivated",
                "Your seller account has been deactivated by the admin."
        );
    }

    @Override
    public Object getProducts(UUID productId, UUID sellerId, UUID categoryId, PageRequestDto dto) {
        Seller seller = null;
        if (sellerId != null) {
            seller = sellerRepository.findById(sellerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));
        }

        Category category = null;
        if (categoryId != null) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        if (productId != null) {
            Product product = productRepository.findByIdAndIsDeletedFalseAndIsActiveTrue(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            if (seller != null && !product.getSeller().getUserId().equals(seller.getUserId())) {
                throw new ResourceNotFoundException("Product not found");
            }

            if (category != null && !product.getCategory().getId().equals(category.getId())) {
                throw new ResourceNotFoundException("Product not found");
            }

            return List.of(mapProduct(product));
        }

        Pageable pageable = PageUtils.getPageable(dto, List.of("createdAt", "name", "brand"));
        return productRepository.findAdminVisibleProducts(seller, category, pageable)
                .map(this::mapProduct);
    }

    @Override
    public void activateProduct(UUID productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product is already active");
        }

        product.setIsActive(true);
        productRepository.save(product);

        emailService.sendEmail(
                product.getSeller().getUser().getEmail(),
                "Product Activated",
                buildProductStatusEmailBody(product, true)
        );
    }

    @Override
    public void deactivateProduct(UUID productId) {
        Product product = productRepository.findByIdAndIsDeletedFalse(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new BadRequestException("Product is already inactive");
        }

        product.setIsActive(false);
        productRepository.save(product);

        emailService.sendEmail(
                product.getSeller().getUser().getEmail(),
                "Product Deactivated",
                buildProductStatusEmailBody(product, false)
        );
    }

    private AdminProductResponse mapProduct(Product product) {
        return AdminProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .brand(product.getBrand())
                .isCancellable(product.getIsCancellable())
                .isReturnable(product.getIsReturnable())
                .isActive(product.getIsActive())
                .sellerId(product.getSeller().getUserId())
                .category(AdminProductCategoryResponse.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .parentChain(buildParentChain(product.getCategory()))
                        .build())
                .primaryImages(productVariationRepository.findByProductAndIsDeletedFalseAndIsActiveTrue(product)
                        .stream()
                        .map(variation -> imageStorageService.getVariationPrimaryImageUrl(product.getId(), variation.getId()))
                        .filter(java.util.Objects::nonNull)
                        .distinct()
                        .toList())
                .build();
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

    private String buildProductStatusEmailBody(Product product, boolean activated) {
        return (activated ? "Your product has been activated.\n\n" : "Your product has been deactivated.\n\n")
                + "Product ID: " + product.getId() + "\n"
                + "Name: " + product.getName() + "\n"
                + "Brand: " + product.getBrand() + "\n"
                + "Category: " + product.getCategory().getName();
    }
}
