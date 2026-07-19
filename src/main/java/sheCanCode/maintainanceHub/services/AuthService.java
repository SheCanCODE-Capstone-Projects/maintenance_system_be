package sheCanCode.maintainanceHub.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sheCanCode.maintainanceHub.auth.JwtUtil;
import sheCanCode.maintainanceHub.dto.*;
import sheCanCode.maintainanceHub.modals.Customer;
import sheCanCode.maintainanceHub.modals.OtpVerification;
import sheCanCode.maintainanceHub.modals.User;
import sheCanCode.maintainanceHub.repositories.CustomerRepository;
import sheCanCode.maintainanceHub.repositories.OtpVerificationRepository;
import sheCanCode.maintainanceHub.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        log.info("Processing registration for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Check if phone number already exists
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setIsVerified(false);
        user.setIsBlocked(false);
        user.setFailedLoginAttempts(0);

        // Save user
        User savedUser = userRepository.save(user);
        log.info("User created with ID: {}", savedUser.getId());

        // If role is CUSTOMER, create customer profile
        if (request.getRole() == User.UserRole.CUSTOMER) {
            Customer customer = new Customer();
            customer.setUser(savedUser);
            customerRepository.save(customer);
            log.info("Customer profile created for user ID: {}", savedUser.getId());
        }

        // Generate and save OTP
        String otpCode = generateOTP();
        OtpVerification otp = new OtpVerification();
        otp.setUser(savedUser);
        otp.setOtpCode(otpCode);
        otp.setOtpType(OtpVerification.OtpType.REGISTRATION);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setIsUsed(false);
        otpVerificationRepository.save(otp);

        log.info("OTP generated for user ID {}: {}", savedUser.getId(), otpCode);

        // In production, send OTP via SMS/Email
        // For now, we'll just log it

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .phoneNumber(savedUser.getPhoneNumber())
                .role(savedUser.getRole())
                .isVerified(savedUser.getIsVerified())
                .createdAt(savedUser.getCreatedAt())
                .message("Registration successful. Please verify your account with the OTP sent to your email/phone. OTP: " + otpCode)
                .build();
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // ==================== LOGIN ====================
    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Processing login for: {}", request.getEmailOrPhone());

        // Find user by email or phone
        User user = userRepository.findByEmail(request.getEmailOrPhone())
                .or(() -> userRepository.findByPhoneNumber(request.getEmailOrPhone()))
                .orElseThrow(() -> new RuntimeException("Invalid email/phone or password"));

        // Check if account is blocked
        if (user.getIsBlocked()) {
            throw new RuntimeException("Account is blocked due to multiple failed login attempts. Please contact support.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Increment failed login attempts
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            
            if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
                user.setIsBlocked(true);
                userRepository.save(user);
                log.warn("Account blocked for user ID {} after {} failed attempts", user.getId(), MAX_FAILED_ATTEMPTS);
                throw new RuntimeException("Account blocked due to multiple failed login attempts");
            }
            
            userRepository.save(user);
            log.warn("Failed login attempt {} for user ID {}", user.getFailedLoginAttempts(), user.getId());
            throw new RuntimeException("Invalid email/phone or password");
        }

        // Check if user is verified
        if (!user.getIsVerified()) {
            // Generate new OTP for verification
            String otpCode = generateAndSaveOtp(user, OtpVerification.OtpType.LOGIN);
            log.info("User not verified. New OTP sent: {}", otpCode);
            throw new RuntimeException("Account not verified. OTP sent to your email/phone. OTP: " + otpCode);
        }

        // Reset failed login attempts on successful login
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user);

        log.info("User {} logged in successfully", user.getEmail());

        return LoginResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isVerified(user.getIsVerified())
                .token(token)
                .message("Login successful")
                .build();
    }

    // ==================== FORGOT PASSWORD ====================
    @Transactional
    public OtpResponse forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password for: {}", request.getEmailOrPhone());

        // Find user — throws if not found
        User user = userRepository.findByEmail(request.getEmailOrPhone())
                .or(() -> userRepository.findByPhoneNumber(request.getEmailOrPhone()))
                .orElseThrow(() -> new RuntimeException("No account found with this email or phone number"));

        // Invalidate any existing PASSWORD_RESET OTPs
        otpVerificationRepository
                .findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(user, OtpVerification.OtpType.PASSWORD_RESET)
                .ifPresent(existing -> {
                    existing.setIsUsed(true);
                    otpVerificationRepository.save(existing);
                });

        // Generate and save a new PASSWORD_RESET OTP
        String otpCode = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);

        OtpVerification otp = new OtpVerification();
        otp.setUser(user);
        otp.setOtpCode(otpCode);
        otp.setOtpType(OtpVerification.OtpType.PASSWORD_RESET);
        otp.setExpiresAt(expiresAt);
        otp.setIsUsed(false);
        otpVerificationRepository.save(otp);

        log.info("Password-reset OTP generated for user {}: {}", user.getId(), otpCode);

        // In production, send OTP via SMS/Email here

        return OtpResponse.builder()
                .message("Password reset OTP sent to your email/phone")
                .destination(maskDestination(request.getEmailOrPhone()))
                .expiresAt(expiresAt)
                .otpCode(otpCode) // Only expose in development/testing
                .build();
    }

    // ==================== RESET PASSWORD ====================
    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {
        log.info("Processing password reset for: {}", request.getEmailOrPhone());

        // Find user
        User user = userRepository.findByEmail(request.getEmailOrPhone())
                .or(() -> userRepository.findByPhoneNumber(request.getEmailOrPhone()))
                .orElseThrow(() -> new RuntimeException("No account found with this email or phone number"));

        // Find the matching unused PASSWORD_RESET OTP
        OtpVerification otp = otpVerificationRepository
                .findByUserAndOtpCodeAndIsUsedFalse(user, request.getOtpCode())
                .orElseThrow(() -> new RuntimeException("Invalid OTP code"));

        // Ensure the OTP is of PASSWORD_RESET type
        if (otp.getOtpType() != OtpVerification.OtpType.PASSWORD_RESET) {
            throw new RuntimeException("Invalid OTP code for password reset");
        }

        // Check expiry
        if (otp.isExpired()) {
            throw new RuntimeException("OTP has expired. Please request a new password reset.");
        }

        // Mark OTP as used
        otp.setIsUsed(true);
        otpVerificationRepository.save(otp);

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // Reset failed attempts and unblock account if it was blocked
        user.setFailedLoginAttempts(0);
        user.setIsBlocked(false);

        userRepository.save(user);

        log.info("Password reset successfully for user ID: {}", user.getId());

        return ApiResponse.success("Password has been reset successfully. You can now log in with your new password.", null);
    }

    // ==================== HELPER METHODS ====================
    private String generateAndSaveOtp(User user, OtpVerification.OtpType otpType) {
        String otpCode = generateOTP();
        
        OtpVerification otp = new OtpVerification();
        otp.setUser(user);
        otp.setOtpCode(otpCode);
        otp.setOtpType(otpType);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setIsUsed(false);
        otpVerificationRepository.save(otp);
        
        // In production, send OTP via SMS/Email service
        // For now, we just return it
        
        return otpCode;
    }

    private String maskDestination(String destination) {
        if (destination.contains("@")) {
            String[] parts = destination.split("@");
            String username = parts[0];
            String domain = parts[1];
            if (username.length() <= 2) {
                return "**@" + domain;
            }
            return username.substring(0, 2) + "***@" + domain;
        } else {
            if (destination.length() <= 4) {
                return "****" + destination.substring(destination.length() - 2);
            }
            return destination.substring(0, 4) + "****" + destination.substring(destination.length() - 2);
        }
    }
}
