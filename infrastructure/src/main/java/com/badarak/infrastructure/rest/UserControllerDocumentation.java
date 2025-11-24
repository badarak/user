package com.badarak.infrastructure.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "User", description = "the User API")
public interface UserControllerDocumentation {

    @Operation(
            summary = "Fetch all users",
            description = "Fetch all users and paginate the result"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    ResponseEntity<Page<UserResponse>> list(Pageable pageable);

    @Operation(summary = "Get a user by his identifiant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get user successfully"),
            @ApiResponse(responseCode = "404", description = "Not found user"),
    })
    ResponseEntity<UserResponse> get(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id);

    @Operation(summary = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully")
    })
    ResponseEntity<UserResponse> create(CreateOrUpdateUserRequest user);

    @Operation(summary = "Update the given user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "404", description = "Not found user")

    })
    ResponseEntity<UserResponse> update(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id, CreateOrUpdateUserRequest createUserRequest);

    @Operation(summary = "Delete the given user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully")
    })
    ResponseEntity<Void> delete(@Parameter(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id);
}
