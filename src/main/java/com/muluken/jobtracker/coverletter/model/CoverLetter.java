package com.muluken.jobtracker.coverletter.model;

import com.muluken.jobtracker.application.model.JobApplication;
import com.muluken.jobtracker.common.entity.BaseEntity;
import com.muluken.jobtracker.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cover_letters")
public class CoverLetter extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "application_id", unique = true)
    private JobApplication application;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String name;

    private Boolean isAiGenerated = true;
}