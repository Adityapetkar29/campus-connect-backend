package com.campusconnect.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    @GetMapping("/test")
    public String test() {
        return "FacultyController is working";
    }
    @GetMapping("/dashboard")
    public ResponseEntity<?> facultyDashboard(
            org.springframework.security.core.Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity
                    .status(401)
                    .body("Authentication not found");
        }

        return ResponseEntity.ok(
                java.util.Map.of(
                        "message", "Faculty dashboard access successful",
                        "username", authentication.getName(),
                        "authorities", authentication.getAuthorities().toString()
                )
        );
    }}