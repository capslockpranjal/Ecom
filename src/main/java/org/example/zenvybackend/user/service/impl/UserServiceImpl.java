package org.example.zenvybackend.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.common.storage.ImageStorageService;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateCustomerProfileRequest;
import org.example.zenvybackend.user.dto.request.UpdateSellerProfileRequest;
import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.dto.response.SellerProfileResponse;
import org.example.zenvybackend.user.entity.Address;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.mapper.UserMapper;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.SellerRepository;
import org.example.zenvybackend.user.repository.TokenRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.example.zenvybackend.user.token.TokenType;
import org.example.zenvybackend.user.service.UserEmailService;
import org.example.zenvybackend.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;
    private final UserEmailService emailService;
    private final ImageStorageService imageStorageService;

    @Override
    public CustomerProfileResponse getCustomerProfile() {

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = customerRepository
                .findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return UserMapper.toProfileResponse(customer, imageStorageService.getUserProfileImageUrl(user.getId()));
    }

    @Transactional
    @Override
    public CustomerProfileResponse updateCustomerProfile(UpdateCustomerProfileRequest request) {
        return updateCustomerProfile(request, null);
    }


    @Transactional
    @Override
    public CustomerProfileResponse updateCustomerProfile(UpdateCustomerProfileRequest request, MultipartFile profileImage) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
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

        if (profileImage != null && !profileImage.isEmpty()) {
            imageStorageService.storeUserProfileImage(user.getId(), profileImage);
        }

        return UserMapper.toProfileResponse(customer, imageStorageService.getUserProfileImageUrl(user.getId()));
    }

    @Override
    public SellerProfileResponse getSellerProfile() {

        UUID currentUserId = SecurityUtil.getCurrentUserId();

        Seller seller = sellerRepository.findByIdWithUserAndAddresses(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        User sellerUser = seller.getUser();

        Address address = sellerUser.getAddresses()
                .stream()
                .findFirst()
                .orElse(null);

        return UserMapper.toSellerProfileResponse(seller, address, imageStorageService.getUserProfileImageUrl(currentUserId));
    }

    @Transactional
    @Override
    public SellerProfileResponse updateSellerProfile(UpdateSellerProfileRequest request) {
        return updateSellerProfile(request, null);
    }

    @Transactional
    @Override
    public SellerProfileResponse updateSellerProfile(UpdateSellerProfileRequest request, MultipartFile profileImage) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
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

        if (profileImage != null && !profileImage.isEmpty()) {
            imageStorageService.storeUserProfileImage(user.getId(), profileImage);
        }

        Seller refreshed = sellerRepository.findByIdWithUserAndAddresses(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        User sellerUser = refreshed.getUser();
        Address address = sellerUser.getAddresses()
                .stream()
                .findFirst()
                .orElse(null);

        return UserMapper.toSellerProfileResponse(refreshed, address, imageStorageService.getUserProfileImageUrl(user.getId()));
    }

    @Transactional
    @Override
    public void changePassword(ChangePasswordRequest request) {

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
