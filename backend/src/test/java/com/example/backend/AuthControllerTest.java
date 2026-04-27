package com.example.backend;

import com.example.backend.model.RefreshToken;
import com.example.backend.model.User;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtil;
import com.example.backend.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired WebApplicationContext webApplicationContext;
    @Autowired UserRepository userRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired RefreshTokenService refreshTokenService;
    @Autowired JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void signup_returnsOkForNewEmail() throws Exception {
        Map<String, String> body = Map.of("name", "Alice", "email", "alice@example.com", "password", "secret123");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        userRepository.findAll().stream()
                .filter(u -> "alice@example.com".equals(u.getEmail()))
                .forEach(userRepository::delete);
    }

    @Test
    void signup_returnsBadRequestForDuplicateEmail() throws Exception {
        User existing = new User(null, "Bob", "bob@example.com", passwordEncoder.encode("pass"));
        userRepository.save(existing);

        Map<String, String> body = Map.of("name", "Bob2", "email", "bob@example.com", "password", "anotherpass");

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());

        userRepository.delete(existing);
    }

    @Test
    void signin_returnsTokensOnValidCredentials() throws Exception {
        User user = new User(null, "Carol", "carol@example.com", passwordEncoder.encode("mypassword"));
        userRepository.save(user);

        Map<String, String> body = Map.of("email", "carol@example.com", "password", "mypassword");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.name").value("Carol"));

        refreshTokenRepository.findAll().stream()
                .filter(t -> t.getUser().getEmail().equals("carol@example.com"))
                .forEach(refreshTokenRepository::delete);
        userRepository.delete(user);
    }

    @Test
    void signin_returnsUnauthorizedForWrongPassword() throws Exception {
        User user = new User(null, "Dave", "dave@example.com", passwordEncoder.encode("correctpass"));
        userRepository.save(user);

        Map<String, String> body = Map.of("email", "dave@example.com", "password", "wrongpass");

        mockMvc.perform(post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());

        userRepository.delete(user);
    }

    @Test
    void logout_deletesRefreshToken() throws Exception {
        User user = new User(null, "Eve", "eve@example.com", passwordEncoder.encode("pass"));
        userRepository.save(user);
        RefreshToken token = refreshTokenService.createRefreshToken(user.getId());

        Map<String, String> body = Map.of("refreshToken", token.getToken());

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        userRepository.delete(user);
    }
}
