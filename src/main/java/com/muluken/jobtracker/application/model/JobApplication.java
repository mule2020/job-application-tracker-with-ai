package com.muluken.jobtracker.application.model;

import com.muluken.jobtracker.common.entity.BaseEntity;
import com.muluken.jobtracker.coverletter.model.CoverLetter;
import com.muluken.jobtracker.resume.model.GeneratedResume;
import com.muluken.jobtracker.user.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "job_applications")
public class JobApplication extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String jobTitle;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private String jobUrl;

    private String salaryRange;

    private String location;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private GeneratedResume generatedResume;

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private CoverLetter coverLetter;

    private LocalDateTime appliedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
    }
}