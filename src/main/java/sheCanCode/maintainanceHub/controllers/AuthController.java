package sheCanCode.maintainanceHub.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sheCanCode.maintainanceHub.dto.*;
import sheCanCode.maintainanceHub.services.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and user registration endpoints")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Create a new user account with role (CUSTOMER, TECHNICIAN, or ADMIN). " +
                    "Users with CUSTOMER role will automatically have a customer profile created. " +
                    "An OTP will be generated and returned for verification."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = sheCanCode.maintainanceHub.dto.ApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or validation error",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email or phone number already registered",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<sheCanCode.maintainanceHub.dto.ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        
        log.info("Registration request received for email: {}", request.getEmail());
        
        try {
            RegisterResponse response = authService.register(request);
            
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.success(
                            "Registration successful. Please verify your account with OTP.",
                            response
                    ));
            
        } catch (RuntimeException e) {
            log.error("Registration failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login user",
            description = "Authenticate with email/phone and password. Returns a JWT session token on success. " +
                    "Account will be blocked after 5 failed login attempts. " +
                    "If account is not verified, an OTP will be sent."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = sheCanCode.maintainanceHub.dto.ApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials or account not verified",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Account is blocked",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<sheCanCode.maintainanceHub.dto.ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        log.info("Login request received for: {}", request.getEmailOrPhone());
        
        try {
            LoginResponse response = authService.login(request);
            
            return ResponseEntity
                    .ok(sheCanCode.maintainanceHub.dto.ApiResponse.success(
                            "Login successful",
                            response
                    ));
            
        } catch (RuntimeException e) {
            log.error("Login failed: {}", e.getMessage());
            
            HttpStatus status = e.getMessage().contains("blocked") 
                    ? HttpStatus.FORBIDDEN 
                    : HttpStatus.UNAUTHORIZED;
            
            return ResponseEntity
                    .status(status)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Logout user",
            description = "Invalidate the current session/token. " +
                    "Note: In a stateless JWT setup, the client should discard the token."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<sheCanCode.maintainanceHub.dto.ApiResponse<String>> logout() {
        log.info("Logout request received");
        
        // In a stateless JWT system, logout is handled client-side by removing the token
        // If you need server-side token invalidation, implement a token blacklist
        
        return ResponseEntity
                .ok(sheCanCode.maintainanceHub.dto.ApiResponse.success(
                        "Logout successful",
                        "Session terminated. Please discard your token."
                ));
    }

    // TODO: Implement forgot-password and reset-password endpoints
    // These will be implemented in OtpService when needed
    
    /*
    @PostMapping("/forgot-password")
    @Operation(
            summary = "Request password reset",
            description = "Trigger a password-reset OTP to be sent to user's email/phone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset OTP sent",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = sheCanCode.maintainanceHub.dto.ApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No account found with this email/phone",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<sheCanCode.maintainanceHub.dto.ApiResponse<OtpResponse>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        
        log.info("Forgot password request for: {}", request.getEmailOrPhone());
        
        try {
            // Use OtpService.sendOtp with PASSWORD_RESET type
            OtpResponse response = authService.forgotPassword(request);
            
            return ResponseEntity
                    .ok(sheCanCode.maintainanceHub.dto.ApiResponse.success(
                            "Password reset OTP sent successfully",
                            response
                    ));
            
        } catch (RuntimeException e) {
            log.error("Forgot password failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    @Operation(
            summary = "Reset password",
            description = "Set a new password after OTP verification. " +
                    "This will also reset failed login attempts and unblock the account if blocked."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Password reset successful",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired OTP",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<sheCanCode.maintainanceHub.dto.ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        
        log.info("Reset password request for user ID: {}", request.getUserId());
        
        try {
            sheCanCode.maintainanceHub.dto.ApiResponse<String> response = authService.resetPassword(request);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Reset password failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.error(e.getMessage()));
        }
    }
    */
}
