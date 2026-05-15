package com.muluken.jobtracker.auth.model;

import com.muluken.jobtracker.common.entity.BaseEntity;
import com.muluken.jobtracker.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String tokenHash;

    private LocalDateTime expiresAt;

    private Boolean revoked = false;
}
