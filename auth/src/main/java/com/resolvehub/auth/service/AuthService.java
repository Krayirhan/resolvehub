package com.resolvehub.auth.service;

import com.resolvehub.auth.domain.RoleEntity;
import com.resolvehub.auth.domain.UserEntity;
import com.resolvehub.auth.domain.UserStatus;
import com.resolvehub.auth.dto.*;
import com.resolvehub.auth.repository.RoleRepository;
import com.resolvehub.auth.repository.UserRepository;
import com.resolvehub.common.exception.BadRequestException;
import com.resolvehub.common.exception.NotFoundException;
import com.resolvehub.common.exception.UnauthorizedException;
import com.resolvehub.common.security.RoleNames;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BadRequestException("Email already exists");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new BadRequestException("Username already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.email().trim().toLowerCase());
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        RoleEntity role = roleRepository.findByName(RoleNames.USER)
                .orElseThrow(() -> new NotFoundException("Default USER role not found"));
        user.getRoles().add(role);
        userRepository.save(user);

        return issueTokens(user);
    }

    public TokenResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User is not active");
        }
        return issueTokens(user);
    }

    public TokenResponse refresh(RefreshRequest request) {
        Long userId = refreshTokenService.validate(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        return issueTokens(user);
    }

    public void logout(LogoutRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    private TokenResponse issueTokens(UserEntity user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.issueToken(user.getId());
        return new TokenResponse(
                accessToken,
                refreshToken,
                jwtService.accessTokenTtlSeconds(),
                toUserResponse(user)
        );
    }

    private UserResponse toUserResponse(UserEntity user) {
        Set<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getEmail(), user.getUsername(), roles);
    }
}
