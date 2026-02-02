package com.maxzdosreis.products_api.controller.docs;

import com.maxzdosreis.products_api.data.dto.UserResponseDTO;
import com.maxzdosreis.products_api.data.dto.security.SignInRequestDTO;
import com.maxzdosreis.products_api.data.dto.security.SignUpRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public interface AuthControllerDocs {

    @Operation(
            summary = "Authenticate user",
            description = "Validates user credentials and generates access and refresh tokens",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Forbidden", responseCode = "403", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<?> signin(SignInRequestDTO credentials);

    @Operation(
            summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(description = "Success", responseCode = "200", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Unauthorized", responseCode = "401", content = @Content),
                    @ApiResponse(description = "Forbidden", responseCode = "403", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<?> refresh(String username, String refreshToken);

    @Operation(
            summary = "Register new User",
            description = "Creates a new user account with default USER role.",
            tags = {"Authentication"},
            responses = {
                    @ApiResponse(description = "Created", responseCode = "201", content = @Content),
                    @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                    @ApiResponse(description = "Conflict", responseCode = "409", content = @Content),
                    @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            }
    )
    ResponseEntity<UserResponseDTO> signup(SignUpRequestDTO credentials);
}
