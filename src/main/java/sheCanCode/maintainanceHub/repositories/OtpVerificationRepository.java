package sheCanCode.maintainanceHub.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sheCanCode.maintainanceHub.modals.OtpVerification;
import sheCanCode.maintainanceHub.modals.User;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {

    Optional<OtpVerification> findByUserAndOtpCodeAndIsUsedFalse(User user, String otpCode);

    Optional<OtpVerification> findTopByUserAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(
            User user, OtpVerification.OtpType otpType);
}
