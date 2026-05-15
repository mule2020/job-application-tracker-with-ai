package com.muluken.jobtracker.coverletter.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCoverLetterRequest {

    @NotBlank
    private String content;
}