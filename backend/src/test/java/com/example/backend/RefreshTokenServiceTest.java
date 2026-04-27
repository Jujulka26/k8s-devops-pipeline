package com.example.backend;

import com.example.backend.model.RefreshToken;
import com.example.backend.model.User;
import com.example.backend.repository.RefreshTokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.RefreshTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void createRefreshToken_savesTokenForUser() {
        User user = new User(1L, "John", "john@example.com", "encoded");
        RefreshToken saved = new RefreshToken();
        saved.setUser(user);
        saved.setToken("uuid-token");
        saved.setExpiryDate(Instant.now().plusSeconds(86400));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenReturn(saved);

        // Manually set the field since @Value won't inject in unit tests
        org.springframework.test.util.ReflectionTestUtils.setField(
                refreshTokenService, "refreshTokenDurationMs", 86400000L);

        RefreshToken result = refreshTokenService.createRefreshToken(1L);

        assertThat(result.getToken()).isEqualTo("uuid-token");
        assertThat(result.getUser().getEmail()).isEqualTo("john@example.com");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void isTokenExpired_returnsTrueWhenExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().minusSeconds(60)); // 1 minute in the past

        assertThat(refreshTokenService.isTokenExpired(token)).isTrue();
    }

    @Test
    void isTokenExpired_returnsFalseWhenNotExpired() {
        RefreshToken token = new RefreshToken();
        token.setExpiryDate(Instant.now().plusSeconds(3600)); // 1 hour in the future

        assertThat(refreshTokenService.isTokenExpired(token)).isFalse();
    }
}
