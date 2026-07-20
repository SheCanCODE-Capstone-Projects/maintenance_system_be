package sheCanCode.maintainanceHub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpResponse {

    private String message;
    private String destination; // masked email/phone
    private LocalDateTime expiresAt;
    private String otpCode; // Only for development/testing - remove in production
}
