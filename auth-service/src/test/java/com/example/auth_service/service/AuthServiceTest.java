package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.exception.AccountLockedException;
import com.example.auth_service.exception.DuplicateResourceException;
import com.example.auth_service.exception.InvalidCredentialsException;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.UserAccount;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserAccountRepository;
import com.example.auth_service.security.TokenHash;
import com.example.auth_service.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private TokenService tokenService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userAccountRepository, refreshTokenRepository, passwordEncoder, tokenService, 86400, 5, 900);
    }

    private void stubTokens() {
        when(tokenService.getExpirationSeconds()).thenReturn(3600L);
        when(tokenService.issueAccessToken(any())).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void register_newEmail_createsUserAndHashedRefreshToken() {
        stubTokens();
        when(userAccountRepository.existsByEmailIgnoreCase("ada@cinema.local")).thenReturn(false);
        when(passwordEncoder.encode("Password1")).thenReturn("hash");
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(inv -> {
            UserAccount user = inv.getArgument(0);
            user.setId(7L);
            return user;
        });

        AuthResponse response = authService.register(new RegisterRequest("ada@cinema.local", "Password1", "Ada Lovelace"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getTokenHash()).isEqualTo(TokenHash.sha256(response.refreshToken()));
        assertThat(tokenCaptor.getValue().getTokenHash()).isNotEqualTo(response.refreshToken());
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userAccountRepository.existsByEmailIgnoreCase("ada@cinema.local")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("ada@cinema.local", "Password1", "Ada")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void login_wrongPassword_incrementsFailures() {
        UserAccount user = enabledUser();
        when(userAccountRepository.findByEmailIgnoreCase("ada@cinema.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ada@cinema.local", "bad")))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(user.getFailedLoginAttempts()).isEqualTo(1);
        verify(userAccountRepository).save(user);
    }

    @Test
    void login_fifthFailure_locksAccount() {
        UserAccount user = enabledUser();
        user.setFailedLoginAttempts(4);
        when(userAccountRepository.findByEmailIgnoreCase("ada@cinema.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ada@cinema.local", "bad")))
                .isInstanceOf(InvalidCredentialsException.class);
        assertThat(user.isLocked()).isTrue();
        assertThat(user.getLockedUntil()).isAfter(Instant.now());
    }

    @Test
    void login_whileLocked_throwsLocked() {
        UserAccount user = enabledUser();
        user.setLockedUntil(Instant.now().plusSeconds(600));
        when(userAccountRepository.findByEmailIgnoreCase("ada@cinema.local")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ada@cinema.local", "Password1")))
                .isInstanceOf(AccountLockedException.class);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void refresh_unknownToken_throwsUnauthorized() {
        assertThatThrownBy(() -> authService.refresh("missing-token"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_revokedToken_revokesFamily() {
        UUID family = UUID.randomUUID();
        RefreshToken stored = new RefreshToken();
        stored.setFamilyId(family);
        stored.setRevoked(true);
        stored.setExpiresAt(Instant.now().plusSeconds(60));
        String raw = "rotated-token";
        when(refreshTokenRepository.findByTokenHash(TokenHash.sha256(raw))).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refresh(raw))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("reuse");
        verify(refreshTokenRepository).revokeAllForFamily(family);
    }

    private UserAccount enabledUser() {
        UserAccount user = new UserAccount();
        user.setId(7L);
        user.setEmail("ada@cinema.local");
        user.setPasswordHash("hash");
        user.setFullName("Ada");
        user.setRole(Role.USER);
        user.setEnabled(true);
        return user;
    }
}
