package com.campusconnect.backend.controller;

import com.campusconnect.backend.entity.User;
import com.campusconnect.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;

    public AdminController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get Pending Faculty
    @GetMapping("/pending-faculty")
    public ResponseEntity<?> getPendingFaculty() {

        return ResponseEntity.ok(
                userRepository.findAll()
                        .stream()
                        .filter(user ->
                                user.getRole() == User.Role.FACULTY
                                        && user.getStatus() == User.Status.PENDING
                        )
                        .toList()
        );
    }

    // Approve Faculty
    @PostMapping("/approve-faculty/{userId}")
    public ResponseEntity<?> approveFaculty(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.getRole() != User.Role.FACULTY) {
            return ResponseEntity
                    .badRequest()
                    .body("User is not a faculty");
        }

        user.setStatus(User.Status.APPROVED);

        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(updatedUser);
    }

    // Reject Faculty
    @PostMapping("/reject-faculty/{userId}")
    public ResponseEntity<?> rejectFaculty(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (user.getRole() != User.Role.FACULTY) {
            return ResponseEntity
                    .badRequest()
                    .body("User is not a faculty");
        }

        user.setStatus(User.Status.REJECTED);

        User updatedUser = userRepository.save(user);

        return ResponseEntity.ok(updatedUser);
    }
}