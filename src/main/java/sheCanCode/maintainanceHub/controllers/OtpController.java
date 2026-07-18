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
import sheCanCode.maintainanceHub.dto.OtpResponse;
import sheCanCode.maintainanceHub.dto.OtpSendRequest;
import sheCanCode.maintainanceHub.dto.OtpVerifyRequest;
import sheCanCode.maintainanceHub.services.OtpService;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "OTP Management", description = "OTP generation, verification and resend endpoints")
@CrossOrigin(origins = "*")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    @Operation(
            summary = "Send OTP",
            description = "Generate and send an OTP code to the user's phone/email. Creates an OTP_VERIFICATION record. " +
                    "The identifier can be either email or phone number."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP sent successfully",
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
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<sheCanCode.maintainanceHub.dto.ApiResponse<OtpResponse>> sendOtp(
            @Valid @RequestBody OtpSendRequest request) {
        
        log.info("OTP send request received for identifier: {}", request.getIdentifier());
        
        try {
            OtpResponse response = otpService.sendOtp(
                    request.getIdentifier(),
                    request.getOtpType()
            );
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.success(
                            "OTP sent successfully",
                            response
                    ));
            
        } catch (RuntimeException e) {
            log.error("Failed to send OTP: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/verify")
    @Operation(
            summary = "Verify OTP",
            description = "Verify the OTP code submitted by the user and activate the account/session. " +
                    "The identifier can be either email or phone number."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "OTP verified successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = sheCanCode.maintainanceHub.dto.ApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired OTP",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<sheCanCode.maintainanceHub.dto.ApiResponse<String>> verifyOtp(
            @Valid @RequestBody OtpVerifyRequest request) {
        
        log.info("OTP verification request received for identifier: {}", request.getIdentifier());
        
        try {
            boolean verified = otpService.verifyOtp(
                    request.getIdentifier(),
                    request.getOtpCode()
            );
            
            if (verified) {
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(sheCanCode.maintainanceHub.dto.ApiResponse.success(
                                "OTP verified successfully. Account is now active.",
                                "verified"
                        ));
            } else {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(sheCanCode.maintainanceHub.dto.ApiResponse.error(
                                "OTP verification failed"
                        ));
            }
            
        } catch (RuntimeException e) {
            log.error("OTP verification failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/resend")
    @Operation(
            summary = "Resend OTP",
            description = "Resend a fresh OTP code if the previous one expired. This will invalidate any previous " +
                    "unused OTP and generate a new one. The identifier can be either email or phone number."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Fresh OTP sent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = sheCanCode.maintainanceHub.dto.ApiResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<sheCanCode.maintainanceHub.dto.ApiResponse<OtpResponse>> resendOtp(
            @Valid @RequestBody OtpSendRequest request) {
        
        log.info("OTP resend request received for identifier: {}", request.getIdentifier());
        
        try {
            OtpResponse response = otpService.resendOtp(request.getIdentifier());
            
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.success(
                            "Fresh OTP sent successfully. Previous OTP has been invalidated.",
                            response
                    ));
            
        } catch (RuntimeException e) {
            log.error("Failed to resend OTP: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(sheCanCode.maintainanceHub.dto.ApiResponse.error(e.getMessage()));
        }
    }
}
