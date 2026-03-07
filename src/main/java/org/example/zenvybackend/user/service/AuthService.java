package org.example.zenvybackend.user.service;

import lombok.RequiredArgsConstructor;
import org.example.zenvybackend.common.exception.BadRequestException;
import org.example.zenvybackend.common.util.PasswordValidator;
import org.example.zenvybackend.security.util.JwtUtil;
import org.example.zenvybackend.user.dto.request.LoginRequest;
import org.example.zenvybackend.user.dto.request.RegisterCustomerRequest;
import org.example.zenvybackend.user.entity.Role;
import org.example.zenvybackend.user.entity.User;
import org.example.zenvybackend.user.repository.RoleRepository;
import org.example.zenvybackend.user.repository.UserRepository;
import org.example.zenvybackend.user.repository.ActivationTokenRepository;
import org.example.zenvybackend.user.repository.ResetPasswordTokenRepository;
import org.example.zenvybackend.user.token.ActivationToken;
import org.example.zenvybackend.user.token.ResetPasswordToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ActivationTokenRepository activationTokenRepository;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final RoleRepository roleRepository;

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCK_DURATION_MINUTES = 30;

    /* ------------------------------------------------ */
    /* REGISTER CUSTOMER */
    /* ------------------------------------------------ */

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
        /* ---------- ADD ROLE HERE ---------- */

        Role role = roleRepository.findByAuthority("ROLE_CUSTOMER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Set.of(role));

        userRepository.save(user);

        generateActivationToken(user);
    }

    /* ------------------------------------------------ */
    /* GENERATE ACTIVATION TOKEN */
    /* ------------------------------------------------ */

    private void generateActivationToken(User user){

        String token = UUID.randomUUID().toString();

        ActivationToken activationToken = new ActivationToken();

        activationToken.setToken(token);
        activationToken.setUser(user);
        activationToken.setExpiryDate(LocalDateTime.now().plusHours(1));

        activationTokenRepository.save(activationToken);

        emailService.sendEmail(
                user.getEmail(),
                "Activate your account",
                "Activation Link: http://localhost:8080/auth/activate?token=" + token
        );
    }

    /* ------------------------------------------------ */
    /* ACTIVATE ACCOUNT */
    /* ------------------------------------------------ */

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

    /* ------------------------------------------------ */
    /* LOGIN */
    /* ------------------------------------------------ */

    @Transactional
    public String login(LoginRequest request){

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
            }

            userRepository.save(user);

            throw new BadRequestException("Invalid email or password");
        }

        user.setInvalidAttemptCount(0);
        userRepository.save(user);

        return jwtUtil.generateToken(user.getEmail(), new ArrayList<>(user.getRoles()));
    }

    /* ------------------------------------------------ */
    /* FORGOT PASSWORD */
    /* ------------------------------------------------ */

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

    /* ------------------------------------------------ */
    /* RESET PASSWORD */
    /* ------------------------------------------------ */

    @Transactional
    public void resetPassword(String token, String password){

        ResetPasswordToken resetToken = resetPasswordTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if(resetToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new BadRequestException("Reset token expired");
        }

        if(!PasswordValidator.isValid(password)){
            throw new BadRequestException("Password does not meet policy requirements");
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(password));
        user.setPasswordUpdateDate(LocalDateTime.now());
        user.setPasswordExpiryDate(LocalDateTime.now().plusDays(90));

        userRepository.save(user);

        resetPasswordTokenRepository.delete(resetToken);
    }

}