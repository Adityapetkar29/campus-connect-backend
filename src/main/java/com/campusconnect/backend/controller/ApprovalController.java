package com.campusconnect.backend.controller;

import com.campusconnect.backend.entity.User;
import com.campusconnect.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.campusconnect.backend.dto.PendingStudentResponse;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
public class ApprovalController {

    private final UserRepository userRepository;

    public ApprovalController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==============================
    // PENDING STUDENTS
    // ==============================

    @GetMapping("/pending-students")
    public ResponseEntity<?> getPendingStudents() {

        List<PendingStudentResponse> students =
                userRepository.findAll()
                        .stream()
                        .filter(user ->
                                user.getRole() == User.Role.STUDENT
                                        && user.getStatus() == User.Status.PENDING
                        )
                        .map(user -> new PendingStudentResponse(
                                user.getUserId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRole().name(),
                                user.getStatus().name()
                        ))
                        .toList();

        return ResponseEntity.ok(students);
    }

    // ==============================
    // STUDENT LIST
    // ==============================

    @GetMapping("/student-list")
    public ResponseEntity<?> getStudentList() {

        List<PendingStudentResponse> students =
                userRepository.findAll()
                        .stream()
                        .filter(user ->
                                user.getRole() == User.Role.STUDENT
                        )
                        .map(user -> new PendingStudentResponse(
                                user.getUserId(),
                                user.getUsername(),
                                user.getEmail(),
                                user.getRole().name(),
                                user.getStatus().name()
                        ))
                        .toList();

        return ResponseEntity.ok(students);
    }

    // ==============================
    // APPROVE STUDENT
    // ==============================

    @PutMapping("/approve-student/{userId}")
    public ResponseEntity<?> approveStudent(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        if (user.getRole() != User.Role.STUDENT) {
            return ResponseEntity
                    .badRequest()
                    .body("User is not a student");
        }

        user.setStatus(User.Status.APPROVED);

        User updatedUser =
                userRepository.save(user);

        return ResponseEntity.ok(updatedUser);
    }

    // ==============================
    // REJECT STUDENT
    // ==============================

    @PutMapping("/reject-student/{userId}")
    public ResponseEntity<?> rejectStudent(
            @PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElse(null);

        if (user == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        if (user.getRole() != User.Role.STUDENT) {
            return ResponseEntity
                    .badRequest()
                    .body("User is not a student");
        }

        user.setStatus(User.Status.REJECTED);

        User updatedUser =
                userRepository.save(user);

        return ResponseEntity.ok(updatedUser);
    }
}