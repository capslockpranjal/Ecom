package org.example.zenvybackend.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateCustomerProfileRequest;
import org.example.zenvybackend.user.dto.request.UpdateSellerProfileRequest;
import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.dto.response.SellerProfileResponse;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.mapper.AddressMapper;
import org.example.zenvybackend.user.mapper.UserMapper;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.example.zenvybackend.user.repository.TokenRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.example.zenvybackend.user.token.TokenType;
import org.example.zenvybackend.user.service.EmailService;
import org.example.zenvybackend.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    @Override
    public CustomerProfileResponse getCustomerProfile() {

        User user = SecurityUtil.getCurrentUser();

        Customer customer = customerRepository
                .findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return UserMapper.toProfileResponse(customer);
    }

    @Transactional
    @Override
    public CustomerProfileResponse updateCustomerProfile(UpdateCustomerProfileRequest request) {



        User current = SecurityUtil.getCurrentUser();
        User user = userRepository.findById(current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));


        


        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }

        if(request.getMiddleName() != null){
            user.setMiddleName(request.getMiddleName());
        }

        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }

        if(request.getContact() != null){
            customer.setContact(request.getContact());
        }

        userRepository.save(user);
        customerRepository.save(customer);


        

        return UserMapper.toProfileResponse(customer);
    }

    @Override
    public SellerProfileResponse getSellerProfile() {

        User user = SecurityUtil.getCurrentUser();
        Seller seller = sellerRepository.findByIdWithUserAndAddresses(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        User sellerUser = seller.getUser();
        // map addresses of this seller (user) from address table (already loaded via fetch join)
        var addressResponses = sellerUser.getAddresses()
                .stream()
                .map(AddressMapper::toResponse)
                .toList();

        return SellerProfileResponse.builder()
                .id(sellerUser.getId())
                .firstName(sellerUser.getFirstName())
                .lastName(sellerUser.getLastName())
                .email(sellerUser.getEmail())
                .companyName(seller.getCompanyName())
                .companyContact(seller.getCompanyContact())
                .gst(seller.getGst())
                .addresses(addressResponses)
                .build();
    }

    @Transactional
    @Override
    public SellerProfileResponse updateSellerProfile(UpdateSellerProfileRequest request) {

        User current = SecurityUtil.getCurrentUser();
        User user = userRepository.findById(current.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Seller seller = sellerRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getCompanyName() != null) {
            seller.setCompanyName(request.getCompanyName());
        }
        if (request.getCompanyContact() != null) {
            seller.setCompanyContact(request.getCompanyContact());
        }

        userRepository.save(user);
        sellerRepository.save(seller);

        // Return the same shape as GET /seller/profile (including addresses).
        Seller refreshed = sellerRepository.findByIdWithUserAndAddresses(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        User sellerUser = refreshed.getUser();
        var addressResponses = sellerUser.getAddresses()
                .stream()
                .map(AddressMapper::toResponse)
                .toList();

        return SellerProfileResponse.builder()
                .id(sellerUser.getId())
                .firstName(sellerUser.getFirstName())
                .lastName(sellerUser.getLastName())
                .email(sellerUser.getEmail())
                .companyName(refreshed.getCompanyName())
                .companyContact(refreshed.getCompanyContact())
                .gst(refreshed.getGst())
                .addresses(addressResponses)
                .build();
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request) {

        User user = SecurityUtil.getCurrentUser();

        if(user.getIsLocked()){
            throw new BadRequestException("Account is locked");
        }

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())){
            throw new BadRequestException("Old password is incorrect");
        }

        if(!request.getNewPassword().equals(request.getConfirmPassword())){
            throw new BadRequestException("Passwords do not match");
        }

        if(passwordEncoder.matches(request.getNewPassword(), user.getPassword())){
            throw new BadRequestException("New password must be different");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordUpdateDate(LocalDateTime.now());

        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Password Changed Successfully",
                "Your password was updated. If this was not you, please contact support immediately."
        );

        tokenRepository.deleteByUserAndType(user, TokenType.REFRESH);
    }
}
