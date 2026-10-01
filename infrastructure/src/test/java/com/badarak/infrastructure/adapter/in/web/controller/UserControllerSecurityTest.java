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

import java.time.Instant;
import java.util.List;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import({SecurityConfig.class, JwtTestProperties.class})
@DisplayName("UserController — authentication")
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
}
