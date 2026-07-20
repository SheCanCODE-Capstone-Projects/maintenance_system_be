package sheCanCode.maintainanceHub.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sheCanCode.maintainanceHub.modals.OtpVerification;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpSendRequest {

    @NotBlank(message = "Email or phone number is required")
    private String identifier; // Can be email or phone number

    private OtpVerification.OtpType otpType; // Optional: REGISTRATION, PASSWORD_RESET, LOGIN (defaults to REGISTRATION)
}
