package com.muluken.jobtracker.resume.model;

import com.muluken.jobtracker.application.model.JobApplication;
import com.muluken.jobtracker.common.entity.BaseEntity;
import com.muluken.jobtracker.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "generated_resumes")
public class GeneratedResume extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne
    @JoinColumn(name = "application_id", unique = true)
    private JobApplication application;

    @Column(columnDefinition = "TEXT")
    private String generatedContent;

    private String name;
}