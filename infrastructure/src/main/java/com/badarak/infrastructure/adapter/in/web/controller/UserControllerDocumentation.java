package com.badarak.infrastructure.adapter.in.web.controller;

import com.badarak.domain.model.UserStatus;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserResponse;
import com.badarak.infrastructure.adapter.in.web.dto.UserPageResponse;
import com.badarak.infrastructure.adapter.in.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface UserControllerDocumentation {

    @Operation(
            summary = "List users",
            description = "Paginated list, filterable by status."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Results page",
            content = @Content(schema = @Schema(implementation = UserPageResponse.class))
    )
    ResponseEntity<UserPageResponse> list(
            @Parameter(description = "Page number (base 0)")
            @Min(0)
            int page,
            @Parameter(description = "Page size (max 100)")
            @Min(1) @Max(100)
            int size,
            @Parameter(description = "Status filter: ACTIVE | INACTIVE | (absent = all)")
            UserStatus status
    );

    @Operation(summary = "Get a user by his id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    ResponseEntity<UserResponse> getById(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id);

    @Operation(
            summary = "Create a new user",
            description = "Create a new active user. The email address must be unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    headers = @Header(
                            name = "Location",
                            description = "URI of the new user",
                            schema = @Schema(type = "string", format = "uri")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid query data (email format, missing fields, etc.)"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "A user with this email address already exists."
            )
    })
    ResponseEntity<CreateUserResponse> create(@Valid CreateUserRequest request);

//    @Operation(summary = "Update the given user")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "User updated successfully"),
//            @ApiResponse(responseCode = "404", description = "Not found user")
//
//    })
//    ResponseEntity<UserResponse> update(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id, CreateOrUpdateUserRequest createUserRequest);
//
    @Operation(summary = "Delete the given user", description = "Soft-delete : changes the status to INACTIVE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User disabled"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User already inactive")
    })
    ResponseEntity<Void> delete(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id);
}
