package sheCanCode.maintainanceHub.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sheCanCode.maintainanceHub.modals.User;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {

    private Long userId;
    private String name;
    private String email;
    private String phoneNumber;
    private User.UserRole role;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private String message;
}
