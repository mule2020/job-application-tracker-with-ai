
        package com.muluken.jobtracker.user.model;

import com.muluken.jobtracker.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

        @Getter
@Setter
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private Boolean isVerified = false;

    private String verificationToken;

    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiresAt;
}

