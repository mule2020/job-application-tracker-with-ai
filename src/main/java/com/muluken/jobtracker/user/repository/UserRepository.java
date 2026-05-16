package com.muluken.jobtracker.user.repository;

import com.muluken.jobtracker.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByVerificationToken(String verificationToken);

    boolean existsByEmail(String email);

    // ── NEW ──────────────────────────
    Optional<User> findByPasswordResetToken(String passwordResetToken);
}