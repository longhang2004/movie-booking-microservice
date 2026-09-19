package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.exception.DuplicateResourceException;
import com.example.auth_service.exception.InvalidCredentialsException;
import com.example.auth_service.model.RefreshToken;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.UserAccount;
import com.example.auth_service.repository.RefreshTokenRepository;
import com.example.auth_service.repository.UserAccountRepository;
import com.example.auth_service.security.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
        authService = new AuthService(userAccountRepository, refreshTokenRepository, passwordEncoder, tokenService, 86400);
    }

    private void stubTokens() {
        when(tokenService.getExpirationSeconds()).thenReturn(3600L);
        when(tokenService.issueAccessToken(any())).thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void register_newEmail_createsUserAndReturnsTokens() {
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
        assertThat(response.user().email()).isEqualTo("ada@cinema.local");
        assertThat(response.user().role()).isEqualTo(Role.USER);
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("hash");
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userAccountRepository.existsByEmailIgnoreCase("ada@cinema.local")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("ada@cinema.local", "Password1", "Ada")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void login_wrongPassword_throwsUnauthorized() {
        UserAccount user = new UserAccount();
        user.setEmail("ada@cinema.local");
        user.setPasswordHash("hash");
        user.setEnabled(true);
        when(userAccountRepository.findByEmailIgnoreCase("ada@cinema.local")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("ada@cinema.local", "bad")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void refresh_unknownToken_throwsUnauthorized() {
        UUID id = UUID.randomUUID();
        when(refreshTokenRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(id.toString()))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
