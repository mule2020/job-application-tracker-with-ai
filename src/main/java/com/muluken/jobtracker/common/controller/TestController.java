package com.muluken.jobtracker.common.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@SecurityRequirement(name = "bearerAuth")
public class TestController {

    @GetMapping("/api/test")
    public String test() {

        return "JWT works!";
    }
}