package com.adro.tms_backend.service;

import com.adro.tms_backend.dto.UserCreateRequest;
import com.adro.tms_backend.dto.UserDto;
import com.adro.tms_backend.dto.UserUpdateRequest;
import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.entity.enums.UserStatus;
import com.adro.tms_backend.exception.DuplicateResourceException;
import com.adro.tms_backend.exception.RecoverableAccountException;
import com.adro.tms_backend.exception.ResourceNotFoundException;
import com.adro.tms_backend.mapper.UserMapper;
import com.adro.tms_backend.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public UserDto create(UserCreateRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(existing -> {
            if (existing.getStatus() == UserStatus.ACTIVE) {
                throw new DuplicateResourceException("Email already in use: " + request.email());
            }
            throw new RecoverableAccountException(existing.getId(), request.email());
        });

        User user = User.builder()
                .email(request.email())
                // NOTE: hashing is not wired up yet — stored as plain text until auth is built.
                .passwordHash(request.password())
                .name(request.name())
                .avatarUrl(request.avatarUrl())
                .timezone(request.timezone() != null ? request.timezone() : "UTC")
                .build();

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserDto findById(UUID id) {
        return userMapper.toDto(getActiveUser(id));
    }

    @Transactional
    public UserDto update(UUID id, UserUpdateRequest request) {
        User user = getActiveUser(id);
        userMapper.updateEntityFromRequest(request, user);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto deactivate(UUID id) {
        User user = getUser(id);
        user.setStatus(UserStatus.INACTIVE);
        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserDto activate(UUID id) {
        User user = getUser(id);
        user.setStatus(UserStatus.ACTIVE);
        return userMapper.toDto(userRepository.save(user));
    }

    private User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    private User getActiveUser(UUID id) {
        return userRepository
                .findByIdAndStatus(id, UserStatus.ACTIVE)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }
}
