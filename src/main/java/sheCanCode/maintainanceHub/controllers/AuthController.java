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
import sheCanCode.maintainanceHub.dto.RegisterRequest;
import sheCanCode.maintainanceHub.dto.RegisterResponse;
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
}
