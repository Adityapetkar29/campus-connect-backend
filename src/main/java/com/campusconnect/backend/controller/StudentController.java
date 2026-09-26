package com.campusconnect.backend.controller;

import com.campusconnect.backend.entity.IssuedBook;
import com.campusconnect.backend.entity.User;
import com.campusconnect.backend.repository.BookRepository;
import com.campusconnect.backend.repository.IssuedBookRepository;
import com.campusconnect.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final IssuedBookRepository issuedBookRepository;

    public StudentController(
            UserRepository userRepository,
            BookRepository bookRepository,
            IssuedBookRepository issuedBookRepository) {

        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.issuedBookRepository = issuedBookRepository;
    }

    // STUDENT DASHBOARD
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        List<IssuedBook> issuedBooks = issuedBookRepository
                .findAll()
                .stream()
                .filter(book ->
                        book.getStudentName()
                                .equalsIgnoreCase(user.getUsername()))
                .toList();

        long overdueBooks = issuedBooks.stream()
                .filter(book -> book.getReturnDate() == null)
                .filter(book -> book.getDueDate() != null)
                .filter(book -> book.getDueDate().isBefore(LocalDate.now()))
                .count();

        int availableBooks = bookRepository.findAll()
                .stream()
                .mapToInt(book -> book.getAvailableCopies())
                .sum();

        return ResponseEntity.ok(
                Map.of(
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "issuedBooks", issuedBooks.size(),
                        "overdueBooks", overdueBooks,
                        "availableBooks", availableBooks
                )
        );
    }

    // STUDENT PROFILE
    @GetMapping("/profile")
    public ResponseEntity<?> profile(Authentication authentication) {

        String username = authentication.getName();

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                Map.of(
                        "userId", user.getUserId(),
                        "username", user.getUsername(),
                        "email", user.getEmail(),
                        "role", user.getRole().name(),
                        "status", user.getStatus().name(),
                        "active", user.getActive()
                )
        );
    }

    // STUDENT ISSUED BOOKS
    @GetMapping("/issued-books")
    public ResponseEntity<?> issuedBooks(Authentication authentication) {

        String username = authentication.getName();

        List<IssuedBook> books = issuedBookRepository.findAll()
                .stream()
                .filter(book ->
                        book.getStudentName()
                                .equalsIgnoreCase(username))
                .toList();

        return ResponseEntity.ok(books);
    }

    // ALL AVAILABLE LIBRARY BOOKS
    @GetMapping("/books")
    public ResponseEntity<?> books() {

        return ResponseEntity.ok(
                bookRepository.findAll()
        );
    }
}