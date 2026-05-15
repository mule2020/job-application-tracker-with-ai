//
//        package com.muluken.jobtracker.resume.model;
//
//import com.muluken.jobtracker.common.entity.BaseEntity;
//import com.muluken.jobtracker.user.model.User;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//@Getter
//@Setter
//@Entity
//@Table(name = "resumes")
//public class Resume extends BaseEntity {
//
//    @ManyToOne
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @Column(columnDefinition = "TEXT")
//    private String content;
//
//    private Boolean isAiGenerated = false;
//}
