package org.example.zenvybackend.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.admin.dto.AdminCustomerResponse;
import org.example.zenvybackend.admin.dto.AdminSellerResponse;
import org.example.zenvybackend.admin.service.AdminService;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.response.PagedResponse;
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
}