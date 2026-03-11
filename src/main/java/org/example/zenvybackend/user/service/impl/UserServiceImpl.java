package org.example.zenvybackend.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.exception.ResourceNotFoundException;
import org.example.zenvybackend.security.util.SecurityUtil;
import org.example.zenvybackend.user.dto.request.ChangePasswordRequest;
import org.example.zenvybackend.user.dto.request.UpdateCustomerProfileRequest;
import org.example.zenvybackend.user.dto.response.CustomerProfileResponse;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.mapper.UserMapper;
import org.example.zenvybackend.user.repository.CustomerRepository;
import org.example.zenvybackend.user.repository.RefreshTokenRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.example.zenvybackend.user.service.EmailService;
import org.example.zenvybackend.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailService emailService;

    @Override
    public CustomerProfileResponse getCustomerProfile() {

        User user = SecurityUtil.getCurrentUser();

        Customer customer = customerRepository
                .findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        return UserMapper.toProfileResponse(customer);
    }

    @Override
    public CustomerProfileResponse updateCustomerProfile(UpdateCustomerProfileRequest request) {

        User user = SecurityUtil.getCurrentUser();

        Customer customer = customerRepository
                .findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
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

        refreshTokenRepository.deleteByUser(user);
    }
}
