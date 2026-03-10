package org.example.zenvybackend.user.service;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;

import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.util.PasswordValidator;
import org.example.zenvybackend.security.service.BlacklistCacheService;
import org.example.zenvybackend.security.util.JwtUtil;
import org.example.zenvybackend.user.dto.request.LoginRequest;
import org.example.zenvybackend.user.dto.request.RegisterCustomerRequest;
import org.example.zenvybackend.user.dto.response.AuthResponse;
import org.example.zenvybackend.user.entity.Role;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.*;
import org.example.zenvybackend.user.token.ActivationToken;
import org.example.zenvybackend.user.token.BlacklistedToken;
import org.example.zenvybackend.user.token.RefreshToken;
import org.example.zenvybackend.user.token.ResetPasswordToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
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
    @Value("${jwt.access.expiration}")
    private long accessExpiration;



    @Transactional
    public void registerCustomer(RegisterCustomerRequest request){

        if(userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already registered");
        }

        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new BadRequestException("Passwords do not match");
        }

        if(!PasswordValidator.isValid(request.getPassword())){
            throw new BadRequestException(
                    "Password must contain uppercase, lowercase, number and minimum 8 characters"
            );
        }

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPasswordUpdateDate(LocalDateTime.now());
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(90));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setIsActive(false);


        Role role = roleRepository.findByAuthority("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Set.of(role));

        userRepository.save(user);

        generateActivationToken(user);
    }



    @Transactional
    public void registerSeller(RegisterCustomerRequest request){

        if(userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already registered");
        }

        if(!request.getPassword().equals(request.getConfirmPassword())){
            throw new BadRequestException("Passwords do not match");
        }

        if(!PasswordValidator.isValid(request.getPassword())){
            throw new BadRequestException(
                    "Password must contain uppercase, lowercase, number and minimum 8 characters"
            );
        }

        User user = new User();

        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user.setPasswordUpdateDate(LocalDateTime.now());
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(90));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        /* SELLER ACCOUNTS REQUIRE ADMIN APPROVAL */
        user.setIsActive(false);

        Role role = roleRepository.findByAuthority("ROLE_SELLER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Set.of(role));

        userRepository.save(user);

        emailService.sendEmail(
                user.getEmail(),
                "Seller Registration Received",
                "Your seller account is under review. You will be notified once approved."
        );
    }



    private void generateActivationToken(User user){

        activationTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        ActivationToken activationToken = new ActivationToken();

        activationToken.setToken(token);
        activationToken.setUser(user);
        activationToken.setExpiryDate(LocalDateTime.now().plusHours(3));

        activationTokenRepository.save(activationToken);

        emailService.sendEmail(
                user.getEmail(),
                "Activate your account",
                "Activation Link: http://localhost:8080/auth/activate?token=" + token
        );
    }



    @Transactional
    public void activateAccount(String token){

        ActivationToken activationToken = activationTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid activation token"));

        if(activationToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new BadRequestException("Activation token expired");
        }

        User user = activationToken.getUser();

        user.setIsActive(true);

        userRepository.save(user);

        activationTokenRepository.delete(activationToken);
    }



    @Transactional(noRollbackFor = BadRequestException.class)
    public AuthResponse login(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if(!user.getIsActive()){
            throw new BadRequestException("Account not activated");
        }
        if(user.getPasswordExpiryDate() != null &&
                user.getPasswordExpiryDate().isBefore(LocalDateTime.now())){

            throw new BadRequestException("Password expired. Please reset your password.");
        }

        if(user.getIsLocked()){

            if(user.getLockTime() != null &&
                    user.getLockTime().plusMinutes(LOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())){

                // UNLOCK ACCOUNT
                user.setIsLocked(false);
                user.setInvalidAttemptCount(0);
                user.setLockTime(null);

                userRepository.save(user);

            } else {
                throw new BadRequestException("Account locked. Try again after 30 minutes");
            }
        }

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){

            user.setInvalidAttemptCount(user.getInvalidAttemptCount() + 1);

            if(user.getInvalidAttemptCount() >= MAX_LOGIN_ATTEMPTS){
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

        refreshTokenRepository.deleteByUser(user);
        String refreshToken = UUID.randomUUID().toString();

        RefreshToken token = new RefreshToken();
        token.setToken(refreshToken);
        token.setUser(user);
        token.setExpiryDate(
                LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration))
        );


        refreshTokenRepository.save(token);

        return new AuthResponse(accessToken, refreshToken);
    }



    @Transactional
    public void forgotPassword(String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email not found"));

        resetPasswordTokenRepository.deleteByUser(user);
        String token = UUID.randomUUID().toString();

        ResetPasswordToken resetToken = new ResetPasswordToken();

        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(15));

        resetPasswordTokenRepository.save(resetToken);

        emailService.sendEmail(
                user.getEmail(),
                "Reset Password",
                "Reset Link: http://localhost:8080/auth/reset-password?token=" + token
        );
    }



    @Transactional
    public void resetPassword(String token, String password, String confirmPassword){

        if(!password.equals(confirmPassword)){
            throw new BadRequestException("Passwords do not match");
        }
        ResetPasswordToken resetToken = resetPasswordTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));


        if(resetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            resetPasswordTokenRepository.delete(resetToken);
            throw new BadRequestException("Reset token expired");
        }

        if(resetToken.getAttemptCount() >= MAX_RESET_ATTEMPTS){
            resetPasswordTokenRepository.delete(resetToken);
            throw new BadRequestException("Too many attempts. Request a new reset link.");
        }

        if(!PasswordValidator.isValid(password)){

            resetToken.setAttemptCount(resetToken.getAttemptCount() + 1);
            resetPasswordTokenRepository.save(resetToken);

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

        resetPasswordTokenRepository.delete(resetToken);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue){

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if(refreshToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new BadRequestException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        /* DELETE OLD REFRESH TOKEN */
        refreshTokenRepository.delete(refreshToken);

        /* CREATE NEW REFRESH TOKEN */
        String newRefreshToken = UUID.randomUUID().toString();

        RefreshToken token = new RefreshToken();
        token.setToken(newRefreshToken);
        token.setUser(user);
        token.setExpiryDate(
                LocalDateTime.now().plus(Duration.ofMillis(refreshExpiration))
        );

        refreshTokenRepository.save(token);

        /* CREATE NEW ACCESS TOKEN */
        String accessToken = jwtUtil.generateToken(
                user.getEmail(),
                new ArrayList<>(user.getRoles())
        );

        return new AuthResponse(accessToken, newRefreshToken);
    }

    @Transactional
    public void resendActivation(String email){

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if(user.getIsActive()){
            throw new BadRequestException("Account already activated");
        }

        activationTokenRepository.deleteByUser(user);

        generateActivationToken(user);
    }
    @Transactional
    public void logout(String token) {

        // prevent duplicate insert
        if (blacklistedTokenRepository.existsByToken(token)) {
            return;
        }

        Date expiry;

        try {
            expiry = jwtUtil.extractExpiration(token);
        } catch (ExpiredJwtException ex) {
            expiry = ex.getClaims().getExpiration();
        }

        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
                .token(token)
                .expiryDate(
                        expiry.toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                )
                .build();

        blacklistedTokenRepository.save(blacklistedToken);

        blacklistCacheService.blacklistToken(token);

        String email = jwtUtil.extractEmail(token);
        refreshTokenRepository.deleteByUser_Email(email);
    }
}
