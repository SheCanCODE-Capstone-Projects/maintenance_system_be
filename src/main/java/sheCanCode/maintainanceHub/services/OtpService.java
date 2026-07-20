package sheCanCode.maintainanceHub.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sheCanCode.maintainanceHub.dto.OtpResponse;
import sheCanCode.maintainanceHub.modals.OtpVerification;
import sheCanCode.maintainanceHub.modals.User;
import sheCanCode.maintainanceHub.repositories.OtpVerificationRepository;
import sheCanCode.maintainanceHub.repositories.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpVerificationRepository otpVerificationRepository;
    private final UserRepository userRepository;
    private static final int OTP_EXPIRATION_MINUTES = 10;

    @Transactional
    public OtpResponse sendOtp(String emailOrPhone, OtpVerification.OtpType otpType) {
        log.info("Sending OTP to: {}", emailOrPhone);

        // Find user by email or phone
        User user = findUserByEmailOrPhone(emailOrPhone);

        // Mark any existing unused OTPs as used
        markExistingOtpsAsUsed(user, otpType);

        // Generate new OTP
        String otpCode = generateOTP();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);

        // Save OTP
        OtpVerification otp = new OtpVerification();
        otp.setUser(user);
        otp.setOtpCode(otpCode);
        otp.setOtpType(otpType);
        otp.setExpiresAt(expiresAt);
        otp.setIsUsed(false);
        otpVerificationRepository.save(otp);

        log.info("OTP generated for user {}: {}", user.getId(), otpCode);

        // In production, send OTP via SMS/Email here
        // sendOtpViaSms(user.getPhoneNumber(), otpCode);
        // sendOtpViaEmail(user.getEmail(), otpCode);

        return OtpResponse.builder()
                .message("OTP sent successfully to your email/phone")
                .destination(maskDestination(emailOrPhone))
                .expiresAt(expiresAt)
                .otpCode(otpCode) // Only for development/testing
                .build();
    }

    @Transactional
    public OtpResponse resendOtp(String emailOrPhone) {
        log.info("Resending OTP to: {}", emailOrPhone);

        // Find user by email or phone
        User user = findUserByEmailOrPhone(emailOrPhone);

        // Find the most recent OTP to determine the type
        Optional<OtpVerification> lastOtp = otpVerificationRepository
                .findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(user, OtpVerification.OtpType.REGISTRATION);

        if (lastOtp.isEmpty()) {
            lastOtp = otpVerificationRepository
                    .findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(user, OtpVerification.OtpType.PASSWORD_RESET);
        }

        OtpVerification.OtpType otpType = lastOtp.isPresent() 
                ? lastOtp.get().getOtpType() 
                : OtpVerification.OtpType.REGISTRATION;

        // Mark existing OTPs as used
        markExistingOtpsAsUsed(user, otpType);

        // Generate and send new OTP
        return sendOtp(emailOrPhone, otpType);
    }

    @Transactional
    public boolean verifyOtp(String emailOrPhone, String otpCode) {
        log.info("Verifying OTP for: {}", emailOrPhone);

        // Find user
        User user = findUserByEmailOrPhone(emailOrPhone);

        // Find OTP
        Optional<OtpVerification> otpOpt = otpVerificationRepository
                .findByUserAndOtpCodeAndIsUsedFalse(user, otpCode);

        if (otpOpt.isEmpty()) {
            log.warn("Invalid OTP code for user: {}", emailOrPhone);
            throw new RuntimeException("Invalid OTP code");
        }

        OtpVerification otp = otpOpt.get();

        // Check if OTP is expired
        if (otp.isExpired()) {
            log.warn("Expired OTP for user: {}", emailOrPhone);
            throw new RuntimeException("OTP has expired. Please request a new one");
        }

        // Mark OTP as used
        otp.setIsUsed(true);
        otpVerificationRepository.save(otp);

        // If it's a registration OTP, verify the user account
        if (otp.getOtpType() == OtpVerification.OtpType.REGISTRATION) {
            user.setIsVerified(true);
            userRepository.save(user);
            log.info("User account verified: {}", user.getId());
        }

        log.info("OTP verified successfully for user: {}", emailOrPhone);
        return true;
    }

    private User findUserByEmailOrPhone(String emailOrPhone) {
        Optional<User> userOpt = userRepository.findByEmail(emailOrPhone);
        
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPhoneNumber(emailOrPhone);
        }

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with email/phone: " + emailOrPhone);
        }

        return userOpt.get();
    }

    private void markExistingOtpsAsUsed(User user, OtpVerification.OtpType otpType) {
        // This would be more efficient with a bulk update query
        otpVerificationRepository.findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(user, otpType)
                .ifPresent(existingOtp -> {
                    existingOtp.setIsUsed(true);
                    otpVerificationRepository.save(existingOtp);
                });
    }

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private String maskDestination(String destination) {
        if (destination.contains("@")) {
            // Email masking
            String[] parts = destination.split("@");
            String username = parts[0];
            String domain = parts[1];
            
            if (username.length() <= 2) {
                return "**@" + domain;
            }
            
            return username.substring(0, 2) + "***@" + domain;
        } else {
            // Phone masking
            if (destination.length() <= 4) {
                return "****" + destination.substring(destination.length() - 2);
            }
            return destination.substring(0, 4) + "****" + destination.substring(destination.length() - 2);
        }
    }
}
