package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.AuthResponse;
import com.adro.tms_backend.dto.LoginRequest;
import com.adro.tms_backend.dto.UserCreateRequest;
import com.adro.tms_backend.dto.UserDto;
import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.InvalidCredentialsException;
import com.adro.tms_backend.mapper.UserMapper;
import com.adro.tms_backend.repository.UserRepository;
import com.adro.tms_backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Transactional
    public AuthResponse register(UserCreateRequest request) {
        UserDto user = userService.create(request);
        sendWelcomeEmail(user);
        String token = jwtService.generateToken(user.id(), user.email(), user.role());
        return new AuthResponse(token, user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email()).orElseThrow(InvalidCredentialsException::new);
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidCredentialsException();
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, userMapper.toDto(user));
    }

    private void sendWelcomeEmail(UserDto user) {
        String subject = "Welcome to Smart Task Management System, " + user.name() + "!";
        String body = "Hi " + user.name() + ",\n\n"
                + "Thanks for signing up with " + user.email() + ". Your account is ready to go — "
                + "start creating tasks, categories, and reminders right away.\n\n"
                + "— Smart Task Management System";
        emailService.send(user.email(), subject, body);
    }
}
