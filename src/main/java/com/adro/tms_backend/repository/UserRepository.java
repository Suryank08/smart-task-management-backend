package com.adro.tms_backend.repository;

import com.adro.tms_backend.entity.User;
import com.adro.tms_backend.entity.enums.UserStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndStatus(UUID id, UserStatus status);

    boolean existsByIdAndStatus(UUID id, UserStatus status);

    Optional<User> findByEmail(String email);
}
