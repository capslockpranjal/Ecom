package org.example.zenvybackend.admin.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.admin.dto.AdminCustomerResponse;
import org.example.zenvybackend.admin.dto.AdminSellerResponse;
import org.example.zenvybackend.admin.service.AdminService;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.mapper.AddressMapper;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.example.zenvybackend.user.service.EmailService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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
    public List<AdminCustomerResponse> listCustomers(int pageNo, int pageSize, String filter, String sortDirection) {

        List<Customer> all = customerRepository.findAll();

        // map to DTOs with joined user
        List<AdminCustomerResponse> mapped = all.stream()
                .map(c -> {
                    User u = c.getUser();
                    return AdminCustomerResponse.builder()
                            .id(u.getId())
                            .firstName(u.getFirstName())
                            .lastName(u.getLastName())
                            .email(u.getEmail())
                            .isActive(u.getIsActive())
                            .contact(c.getContact())
                            .build();
                })
                .toList();

        // filter by name/email/contact if provided
        if (filter != null && !filter.isBlank()) {
            String f = filter.toLowerCase();
            mapped = mapped.stream()
                    .filter(c -> (c.getFirstName() != null && c.getFirstName().toLowerCase().contains(f))
                            || (c.getLastName() != null && c.getLastName().toLowerCase().contains(f))
                            || (c.getEmail() != null && c.getEmail().toLowerCase().contains(f))
                            || (c.getContact() != null && c.getContact().toLowerCase().contains(f)))
                    .toList();
        }

        // simple sort by firstName asc/desc
        Comparator<AdminCustomerResponse> comparator =
                Comparator.comparing(c -> c.getFirstName() == null ? "" : c.getFirstName());
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        mapped = mapped.stream().sorted(comparator).toList();

        // manual pagination
        int fromIndex = Math.max(pageNo, 0) * Math.max(pageSize, 1);
        if (fromIndex >= mapped.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + pageSize, mapped.size());
        return mapped.subList(fromIndex, toIndex);
    }

    @Override
    public List<AdminSellerResponse> listSellers(int pageNo, int pageSize, String filter, String sortDirection) {

        List<Seller> all = sellerRepository.findAllWithUserAndAddresses();

        List<AdminSellerResponse> mapped = all.stream()
                .map(s -> {
                    User u = s.getUser();
                    var addresses = u.getAddresses()
                            .stream()
                            .map(AddressMapper::toResponse)
                            .toList();
                    return AdminSellerResponse.builder()
                            .id(u.getId())
                            .firstName(u.getFirstName())
                            .lastName(u.getLastName())
                            .email(u.getEmail())
                            .isActive(u.getIsActive())
                            .companyName(s.getCompanyName())
                            .companyContact(s.getCompanyContact())
                            .gst(s.getGst())
                            .addresses(addresses)
                            .build();
                })
                .toList();

        if (filter != null && !filter.isBlank()) {
            String f = filter.toLowerCase();
            mapped = mapped.stream()
                    .filter(s -> (s.getFirstName() != null && s.getFirstName().toLowerCase().contains(f))
                            || (s.getLastName() != null && s.getLastName().toLowerCase().contains(f))
                            || (s.getEmail() != null && s.getEmail().toLowerCase().contains(f))
                            || (s.getCompanyName() != null && s.getCompanyName().toLowerCase().contains(f)))
                    .toList();
        }

        Comparator<AdminSellerResponse> comparator =
                Comparator.comparing(s -> s.getCompanyName() == null ? "" : s.getCompanyName());
        if ("desc".equalsIgnoreCase(sortDirection)) {
            comparator = comparator.reversed();
        }
        mapped = mapped.stream().sorted(comparator).toList();

        int fromIndex = Math.max(pageNo, 0) * Math.max(pageSize, 1);
        if (fromIndex >= mapped.size()) {
            return List.of();
        }
        int toIndex = Math.min(fromIndex + pageSize, mapped.size());
        return mapped.subList(fromIndex, toIndex);
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