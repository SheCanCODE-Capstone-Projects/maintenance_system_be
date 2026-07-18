package sheCanCode.maintainanceHub.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sheCanCode.maintainanceHub.dto.RegisterRequest;
import sheCanCode.maintainanceHub.dto.RegisterResponse;
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
}
