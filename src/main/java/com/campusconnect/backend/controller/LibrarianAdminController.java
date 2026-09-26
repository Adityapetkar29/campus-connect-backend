package com.campusconnect.backend.controller;

import com.campusconnect.backend.entity.User;
import com.campusconnect.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/librarians")
public class LibrarianAdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LibrarianAdminController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<?> createLibrarian(
            @RequestBody User request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email already exists");
        }

        User librarian = new User();

        librarian.setUsername(request.getUsername());
        librarian.setEmail(request.getEmail());

        librarian.setPasswordHash(
                passwordEncoder.encode(request.getPasswordHash())
        );

        librarian.setRole(User.Role.LIBRARIAN);
        librarian.setStatus(User.Status.APPROVED);
        librarian.setActive(true);

        User saved = userRepository.save(librarian);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }
}