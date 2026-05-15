package com.muluken.jobtracker.coverletter.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SaveCoverLetterRequest {

    @NotNull
    private UUID applicationId;

    @NotBlank
    private String content;
}