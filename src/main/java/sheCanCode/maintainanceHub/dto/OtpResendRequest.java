package sheCanCode.maintainanceHub.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpResendRequest {

    @NotNull(message = "User ID is required")
    private Long userId;
}
