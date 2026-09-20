package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.dto.UserResponse;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final long refreshExpirationSeconds;
    private final int maxFailedAttempts;
    private final long lockoutSeconds;

    public AuthService(
            UserAccountRepository userAccountRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            @Value("${app.jwt.refresh-expiration-seconds}") long refreshExpirationSeconds,
            @Value("${app.auth.max-failed-attempts:5}") int maxFailedAttempts,
            @Value("${app.auth.lockout-seconds:900}") long lockoutSeconds) {
        this.userAccountRepository = userAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockoutSeconds = lockoutSeconds;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userAccountRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email is already registered");
        }
        UserAccount user = new UserAccount();
        user.setEmail(request.email().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(Role.USER);
        user.setEnabled(true);
        UserAccount saved = userAccountRepository.save(user);
        return issueTokens(saved, UUID.randomUUID());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
        if (user.isLocked()) {
            throw new AccountLockedException("Account is locked. Try again later");
        }
        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            registerFailure(user);
            throw new InvalidCredentialsException("Invalid email or password");
        }
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userAccountRepository.save(user);
        return issueTokens(user, UUID.randomUUID());
    }

    @Transactional
    public AuthResponse refresh(String refreshTokenValue) {
        String hash = TokenHash.sha256(refreshTokenValue);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));
        if (stored.isRevoked()) {
            refreshTokenRepository.revokeAllForFamily(stored.getFamilyId());
            throw new InvalidCredentialsException("Refresh token reuse detected");
        }
        if (stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new InvalidCredentialsException("Refresh token is expired or revoked");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(stored.getUser(), stored.getFamilyId());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.revokeAllForUser(userId);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));
        return UserResponse.from(user);
    }

    private void registerFailure(UserAccount user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(Instant.now().plusSeconds(lockoutSeconds));
        }
        userAccountRepository.save(user);
    }

    private AuthResponse issueTokens(UserAccount user, UUID familyId) {
        String rawRefresh = TokenHash.randomToken();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setId(UUID.randomUUID());
        refreshToken.setTokenHash(TokenHash.sha256(rawRefresh));
        refreshToken.setFamilyId(familyId);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(refreshExpirationSeconds));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                tokenService.issueAccessToken(user),
                rawRefresh,
                "Bearer",
                tokenService.getExpirationSeconds(),
                UserResponse.from(user)
        );
    }
}
