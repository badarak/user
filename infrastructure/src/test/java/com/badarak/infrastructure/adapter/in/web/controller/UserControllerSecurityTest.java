package com.badarak.infrastructure.adapter.in.web.controller;

import com.badarak.domain.model.UserPage;
import com.badarak.domain.port.in.*;
import com.badarak.infrastructure.adapter.in.web.mapper.UserMapper;
import com.badarak.infrastructure.config.security.JwtTestFactory;
import com.badarak.infrastructure.config.security.JwtTestProperties;
import com.badarak.infrastructure.config.security.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.WWW_AUTHENTICATE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, JwtTestProperties.class})
@DisplayName("UserController — authentication and authorization")
class UserControllerSecurityTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CreateUserUseCase createUser;
    @MockitoBean
    GetUserUseCase getUser;
    @MockitoBean
    ListUsersUseCase listUsers;
    @MockitoBean
    DeleteUserUseCase deleteUser;
    @MockitoBean
    UpdateUserUseCase updateUser;
    @MockitoBean
    UserMapper mapper;

    @Test
    void should_return_401_with_bearer_challenge_when_no_token() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, startsWith("Bearer")));

        verifyNoInteractions(listUsers);
    }

    @Test
    void should_return_401_on_write_endpoint_when_no_token() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"email":"john@example.com","firstName":"John","lastName":"Doe"}
                                """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(createUser);
    }

    @Test
    void should_return_200_when_token_is_valid() throws Exception {
        when(listUsers.execute(any())).thenReturn(new UserPage(List.of(), 0, 20, 0L, 0));
        when(mapper.toPageResponse(any())).thenCallRealMethod();

        mockMvc.perform(get("/api/v1/users")
                        .header(AUTHORIZATION, "Bearer " + JwtTestFactory.validToken()))
                .andExpect(status().isOk());
    }

    static Stream<Arguments> rejectedTokens() {
        final var now = Instant.now();
        return Stream.of(
                argumentSet("expired",
                        JwtTestFactory.token(claims -> claims.issuedAt(now.minus(2, HOURS)).expiresAt(now.minus(1, HOURS)))),
                argumentSet("not yet valid",
                        JwtTestFactory.token(claims -> claims.notBefore(now.plus(1, HOURS)))),
                argumentSet("wrong issuer",
                        JwtTestFactory.token(claims -> claims.issuer("https://evil.example"))),
                argumentSet("wrong audience",
                        JwtTestFactory.token(claims -> claims.audience(List.of("order-api")))),
                argumentSet("missing audience",
                        JwtTestFactory.token(claims -> claims.claims(all -> all.remove(JwtClaimNames.AUD)))),
                argumentSet("signed with unknown key", JwtTestFactory.tokenSignedWithUnknownKey()),
                argumentSet("unsigned (alg none)", JwtTestFactory.unsignedToken()),
                argumentSet("malformed", "not-a-jwt")
        );
    }

    @ParameterizedTest
    @MethodSource("rejectedTokens")
    void should_return_401_invalid_token_when_token_is_rejected(String token) throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .header(AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(WWW_AUTHENTICATE, containsString("error=\"invalid_token\"")));

        verifyNoInteractions(listUsers);
    }

    static Stream<Arguments> requestsWithInsufficientScope() {
        final var id = UUID.randomUUID();
        return Stream.of(
                argumentSet("read scope cannot create", "users:read",
                        post("/api/v1/users").contentType(APPLICATION_JSON).content("""
                                {"email":"john@example.com","firstName":"John","lastName":"Doe"}
                                """)),
                argumentSet("read scope cannot update", "users:read",
                        put("/api/v1/users/{id}", id).contentType(APPLICATION_JSON).content("""
                                {"firstName":"Bob","lastName":"Blabla"}
                                """)),
                argumentSet("read scope cannot delete", "users:read", delete("/api/v1/users/{id}", id)),
                argumentSet("write scope cannot list", "users:write", get("/api/v1/users")),
                argumentSet("write scope cannot get by id", "users:write", get("/api/v1/users/{id}", id)),
                argumentSet("no scope cannot list", "", get("/api/v1/users")),
                argumentSet("unmapped path is denied", "users:read users:write", get("/api/v1/other"))
        );
    }

    @ParameterizedTest
    @MethodSource("requestsWithInsufficientScope")
    void should_return_403_when_scope_is_insufficient(String scope, MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request.header(AUTHORIZATION, bearerWithScope(scope)))
                .andExpect(status().isForbidden())
                .andExpect(header().string(WWW_AUTHENTICATE, containsString("error=\"insufficient_scope\"")));

        verifyNoInteractions(createUser, getUser, listUsers, deleteUser, updateUser);
    }

    @Test
    void should_return_200_when_listing_with_read_scope() throws Exception {
        when(listUsers.execute(any())).thenReturn(new UserPage(List.of(), 0, 20, 0L, 0));
        when(mapper.toPageResponse(any())).thenCallRealMethod();

        mockMvc.perform(get("/api/v1/users")
                        .header(AUTHORIZATION, bearerWithScope("users:read")))
                .andExpect(status().isOk());
    }

    @Test
    void should_return_204_when_deleting_with_write_scope() throws Exception {
        mockMvc.perform(delete("/api/v1/users/{id}", UUID.randomUUID())
                        .header(AUTHORIZATION, bearerWithScope("users:write")))
                .andExpect(status().isNoContent());
    }

    private static String bearerWithScope(String scope) {
        return "Bearer " + JwtTestFactory.token(claims -> claims.claim("scope", scope));
    }
}
