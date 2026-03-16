package org.example.zenvybackend.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.constants.RoleConstants;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.util.PasswordValidator;
import org.example.zenvybackend.security.service.BlacklistCacheService;
import org.example.zenvybackend.security.util.JwtUtil;
import org.example.zenvybackend.user.dto.request.LoginRequest;
import org.example.zenvybackend.user.dto.request.RegisterCustomerRequest;
import org.example.zenvybackend.user.dto.request.RegisterSellerRequest;
import org.example.zenvybackend.user.dto.response.AuthResponse;
import org.example.zenvybackend.user.entity.Address;
import org.example.zenvybackend.user.entity.Customer;
import org.example.zenvybackend.user.entity.Role;
import org.example.zenvybackend.user.entity.Seller;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.*;
import org.example.zenvybackend.user.token.Token;
import org.example.zenvybackend.user.token.TokenType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final SellerRepository sellerRepository;
    private final AddressRepository addressRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final RoleRepository roleRepository;
    private final BlacklistCacheService blacklistCacheService;

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 30;
    private static final int MAX_RESET_ATTEMPTS = 5;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpiration;


    @Transactional
    public void registerCustomer(RegisterCustomerRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new BadRequestException(
                    "Password must contain uppercase, lowercase, number and minimum 8 characters"
            );
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);


// NEW: prevent reusing a seller account as customer
        if (user != null) {
            boolean isSellerRole = user.getRoles() != null &&
                    user.getRoles().stream()
                            .anyMatch(r -> RoleConstants.SELLER.equals(r.getAuthority()));

            boolean hasSellerProfile = user.getSeller() != null;

            if (isSellerRole || hasSellerProfile) {
                throw new BadRequestException(
                        "Email already registered as seller. Use a different email for customer account."
                );
            }

            if (Boolean.TRUE.equals(user.getIsActive())) {
                throw new BadRequestException("Email already registered. Please login.");
            }

            throw new BadRequestException(
                    "Email already registered but not activated. Please check your email or use resend activation."
            );
        }

        if (user == null) {
            user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPasswordUpdateDate(LocalDateTime.now());
            user.setPasswordExpiryDate(LocalDateTime.now().plusDays(90));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setIsActive(false);
        }

        addRoleIfMissing(user, RoleConstants.CUSTOMER);

        // ... existing code up to userRepository.save(user);
        userRepository.save(user);

// Ensure customer profile exists and set contact
        Customer customer = customerRepository.findByUser(user).orElse(null);
        if (customer == null) {
            customer = new Customer();
            customer.setUser(user);
        }
        customer.setContact(request.getContact());
        customerRepository.save(customer);

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            generateActivationToken(user);
        }
    }


    @Transactional
    public void registerSeller(RegisterSellerRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new BadRequestException(
                    "Password must contain uppercase, lowercase, number and minimum 8 characters"
            );
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);


// NEW: prevent reusing a customer account as seller
        if (user != null) {
            boolean isCustomerRole = user.getRoles() != null &&
                    user.getRoles().stream()
                            .anyMatch(r -> RoleConstants.CUSTOMER.equals(r.getAuthority()));

            boolean hasCustomerProfile = user.getCustomer() != null;

            if (isCustomerRole || hasCustomerProfile) {
                throw new BadRequestException(
                        "Email already registered as customer. Use a different email for seller account."
                );
            }

            if (Boolean.TRUE.equals(user.getIsActive())) {
                throw new BadRequestException("Email already registered. Please login.");
            }

            throw new BadRequestException(
                    "Seller registration already submitted or account not activated. Please check your email or contact support."
            );
        }

        if (user == null) {
            user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setPasswordUpdateDate(LocalDateTime.now());
            user.setPasswordExpiryDate(LocalDateTime.now().plusDays(90));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            /* SELLER ACCOUNTS REQUIRE ADMIN APPROVAL */
            user.setIsActive(false);
        }

        addRoleIfMissing(user, RoleConstants.SELLER);

        userRepository.save(user);

        // Ensure seller profile exists (and store seller-specific fields)
        Seller seller = user.getSeller();
        if (seller == null) {
            seller = new Seller();
            seller.setUser(user);
        }

        if (seller.getGst() == null || !seller.getGst().equals(request.getGst())) {
            if (sellerRepository.existsByGst(request.getGst())) {
                throw new BadRequestException("GST already registered");
            }
        }

        if (seller.getCompanyName() == null || !seller.getCompanyName().equals(request.getCompanyName())) {
            if (sellerRepository.existsByCompanyName(request.getCompanyName())) {
                throw new BadRequestException("Company name already registered");
            }
        }

        seller.setGst(request.getGst());
        seller.setCompanyName(request.getCompanyName());
        seller.setCompanyContact(request.getCompanyContact());
        sellerRepository.save(seller);

        // also persist company address into address table for this seller user
        Address address = new Address();
        address.setUser(user);
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setAddressLine(request.getAddressLine());
        address.setZipCode(request.getZipCode());
        address.setLabel("Company");
        addressRepository.save(address);

        emailService.sendEmail(
                user.getEmail(),
                "Seller Registration Received",
                "Your seller account is under review. You will be notified once approved."
        );
    }

    private void addRoleIfMissing(User user, String authority) {
        Role role = roleRepository.findByAuthority(authority)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(new java.util.HashSet<>(java.util.Set.of(role)));
            return;
        }

        boolean alreadyHasRole = user.getRoles().stream()
                .anyMatch(r -> authority.equals(r.getAuthority()));

        if (!alreadyHasRole) {
            user.setRoles(new java.util.HashSet<>(user.getRoles()));
            user.getRoles().add(role);
        }
    }


    private void generateActivationToken(User user) {

        tokenRepository.deleteByUserAndType(user, TokenType.ACTIVATION);

        String tokenValue = UUID.randomUUID().toString();

        Token activationToken = new Token();
        activationToken.setToken(tokenValue);
        activationToken.setType(TokenType.ACTIVATION);
        activationToken.setUser(user);
        activationToken.setUserEmail(user.getEmail());
        activationToken.setExpiryDate(LocalDateTime.now().plusHours(3));

        tokenRepository.save(activationToken);

        emailService.sendEmail(
                user.getEmail(),
                "Activate your account",
                "Activation Link: http://localhost:8080/auth/activate?token=" + tokenValue
        );
    }


  @Transactional(noRollbackFor = BadRequestException.class)
public void activateAccount(String token) {

    Token activationToken = tokenRepository
            .findByTokenAndType(token, TokenType.ACTIVATION)
            .orElseThrow(() -> new BadRequestException("Invalid activation token"));

    User user = activationToken.getUser();

    // EXPIRED token flow
    if (activationToken.getExpiryDate().isBefore(LocalDateTime.now())) {

        // delete old token
        tokenRepository.delete(activationToken);

        // generate new token and send another email
        generateActivationToken(user);

        // do NOT activate user
        throw new BadRequestException("Activation token expired. A new activation link has been sent.");
    }

    // VALID token flow
    user.setIsActive(true);
    userRepository.save(user);

    tokenRepository.delete(activationToken);

    // async mail after successful activation (you already have EmailService)
    emailService.sendEmail(
            user.getEmail(),
            "Account Activated",
            "Your account has been successfully activated."
    );
}


    @Transactional(noRollbackFor = BadRequestException.class)
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.getIsActive()) {
            throw new BadRequestException("Account not activated");
        }
        if (user.getPasswordExpiryDate() != null &&
                user.getPasswordExpiryDate().isBefore(LocalDateTime.now())) {

            throw new BadRequestException("Password expired. Please reset your password.");
        }

        if (user.getIsLocked()) {

            if (user.getLockTime() != null &&
                    user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {

                // UNLOCK ACCOUNT
                user.setIsLocked(false);
                user.setInvalidAttemptCount(0);
                user.setLockTime(null);

                userRepository.save(user);

            } 
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {

            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);

            if (user.getInvalidAttemptCount() >= MAX_LOGIN_ATTEMPTS) {
                user.setIsLocked(true);
                user.setLockTime(LocalDateTime.now());

                emailService.sendEmail(
                        user.getEmail(),
                        "Account Locked",
                        "Your account has been locked due to multiple failed login attempts."
                );
            }

            userRepository.save(user);

            throw new BadRequestException("Invalid email or password");
        }

        user.setInvalidAttemptCount(0);
        user.setIsLocked(false);
        user.setLockTime(null);
        userRepository.save(user);

        String accessToken = jwtUtil.generateToken(user.getEmail(), new ArrayList<>(user.getRoles()));

        tokenRepository.deleteByUserAndType(user, TokenType.REFRESH);
        String refreshToken = UUID.randomUUID().toString();

        Token token = new Token();
        token.setToken(refreshToken);
        token.setType(TokenType.REFRESH);
        token.setUser(user);
        token.setUserEmail(user.getEmail());
        token.setExpiryDate(
                LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration))
        );


        tokenRepository.save(token);

        return new AuthResponse(accessToken, refreshToken);
    }


    @Transactional
    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email not found"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Account is not activated. Please activate your account first.");
        }

        tokenRepository.deleteByUserAndType(user, TokenType.RESET_PASSWORD);
        String tokenValue = UUID.randomUUID().toString();

        Token resetToken = new Token();

        resetToken.setToken(tokenValue);
        resetToken.setType(TokenType.RESET_PASSWORD);
        resetToken.setUser(user);
        resetToken.setUserEmail(user.getEmail());
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        tokenRepository.save(resetToken);

        emailService.sendEmail(
                user.getEmail(),
                "Reset Password",
                "Reset Link: http://localhost:8080/auth/reset-password?token=" + tokenValue
        );
    }


    @Transactional
    public void resetPassword(String token, String password, String confirmPassword) {

        if (!password.equals(confirmPassword)) {
            throw new BadRequestException("Passwords do not match");
        }
        Token resetToken = tokenRepository
                .findByTokenAndType(token, TokenType.RESET_PASSWORD)
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));


        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("Reset token expired");
        }

        if (resetToken.getAttemptCount() >= MAX_RESET_ATTEMPTS) {
            tokenRepository.delete(resetToken);
            throw new BadRequestException("Too many attempts. Request a new reset link.");
        }

        if (!PasswordValidator.isValid(password)) {

            resetToken.setAttemptCount(
                    (resetToken.getAttemptCount() == null ? 0 : resetToken.getAttemptCount()) + 1
            );
            tokenRepository.save(resetToken);

            throw new BadRequestException("Password does not meet policy requirements");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordUpdateDate(LocalDateTime.now());
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(90));

        user.setIsLocked(false);
        user.setInvalidAttemptCount(0);
        user.setLockTime(null);
        userRepository.save(user);
        tokenRepository.deleteByUserAndType(user, TokenType.REFRESH);

        tokenRepository.delete(resetToken);

        emailService.sendEmail(
                user.getEmail(),
                "Password Updated",
                "Your password has been successfully updated. If this was not you, please contact support immediately."
        );
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {

        Token refreshToken = tokenRepository
                .findByTokenAndType(refreshTokenValue, TokenType.REFRESH)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        /* DELETE OLD REFRESH TOKEN */
        tokenRepository.delete(refreshToken);

        /* CREATE NEW REFRESH TOKEN */
        String newRefreshToken = UUID.randomUUID().toString();

        Token token = new Token();
        token.setToken(newRefreshToken);
        token.setType(TokenType.REFRESH);
        token.setUser(user);
        token.setUserEmail(user.getEmail());
        token.setExpiryDate(
                LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration))
        );
        tokenRepository.save(token);

        /* CREATE NEW ACCESS TOKEN */
        String accessToken = jwtUtil.generateToken(
                user.getEmail(),
                new ArrayList<>(user.getRoles())
        );

        return new AuthResponse(accessToken, newRefreshToken);
    }

    @Transactional
    public void resendActivation(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getIsActive()) {
            throw new BadRequestException("Account already activated");
        }

        tokenRepository.deleteByUserAndType(user, TokenType.ACTIVATION);

        generateActivationToken(user);
    }

    @Transactional
    public void logout(String token) {

        // prevent duplicate insert
        if (tokenRepository.existsByTokenAndType(token, TokenType.BLACKLISTED)) {
            return;
        }

        Date expiry;

        try {
            expiry = jwtUtil.extractExpiration(token);
        } catch (ExpiredJwtException ex) {
            expiry = ex.getClaims().getExpiration();
        }

        Token blacklistedToken = new Token();
        blacklistedToken.setToken(token);
        blacklistedToken.setType(TokenType.BLACKLISTED);
        blacklistedToken.setExpiryDate(
                expiry.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
        );

        tokenRepository.save(blacklistedToken);

        blacklistCacheService.blacklistToken(token);

        String email = jwtUtil.extractEmail(token);
        tokenRepository.deleteByUserEmailAndType(email, TokenType.REFRESH);
    }
}
