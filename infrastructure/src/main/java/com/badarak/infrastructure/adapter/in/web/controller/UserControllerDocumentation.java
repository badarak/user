package com.badarak.infrastructure.adapter.in.web.controller;

import com.badarak.infrastructure.adapter.in.web.dto.CreateUserRequest;
import com.badarak.infrastructure.adapter.in.web.dto.CreateUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface UserControllerDocumentation {

//    @Operation(
//            summary = "Fetch all users",
//            description = "Fetch all users and paginate the result"
//    )
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successful operation"),
//            @ApiResponse(responseCode = "500", description = "Internal Server Error")
//    })
//    ResponseEntity<Page<UserResponse>> list(Pageable pageable);
//
//    @Operation(summary = "Get a user by his identifiant")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Get user successfully"),
//            @ApiResponse(responseCode = "404", description = "Not found user"),
//    })
//    ResponseEntity<UserResponse> get(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id);

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
    ResponseEntity<CreateUserResponse> create(CreateUserRequest request);

//    @Operation(summary = "Update the given user")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "User updated successfully"),
//            @ApiResponse(responseCode = "404", description = "Not found user")
//
//    })
//    ResponseEntity<UserResponse> update(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id, CreateOrUpdateUserRequest createUserRequest);
//
//    @Operation(summary = "Delete the given user")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "204", description = "User deleted successfully")
//    })
//    ResponseEntity<Void> delete(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id);
}
