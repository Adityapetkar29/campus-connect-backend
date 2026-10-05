package com.campusconnect.backend.controller;

import com.campusconnect.backend.entity.User;
import com.campusconnect.backend.repository.UserRepository;
import com.campusconnect.backend.repository.IssuedBookRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AdminWebController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final IssuedBookRepository issuedBookRepository;

    public AdminWebController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            IssuedBookRepository issuedBookRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.issuedBookRepository = issuedBookRepository;
    }

    // =========================
    // ADMIN LOGIN PAGE
    // =========================

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin/login";
    }

    // =========================
    // ADMIN LOGIN PROCESS
    // =========================

    @PostMapping("/admin/login")
    public String adminLoginProcess(
            @RequestParam String username,
            @RequestParam String password,
            Model model) {

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        if (user == null) {
            model.addAttribute(
                    "error",
                    "Invalid username or password"
            );

            return "admin/login";
        }

        if (user.getRole() != User.Role.ADMIN) {
            model.addAttribute(
                    "error",
                    "Access denied. Admin account required."
            );

            return "admin/login";
        }

        if (!passwordEncoder.matches(
                password,
                user.getPasswordHash())) {

            model.addAttribute(
                    "error",
                    "Invalid username or password"
            );

            return "admin/login";
        }

        if (user.getStatus() != User.Status.APPROVED) {
            model.addAttribute(
                    "error",
                    "Admin account is not approved"
            );

            return "admin/login";
        }

        if (!user.getActive()) {
            model.addAttribute(
                    "error",
                    "Admin account is inactive"
            );

            return "admin/login";
        }

        return "redirect:/admin";
    }

    // =========================
    // ADMIN DASHBOARD
    // =========================

    @GetMapping("/admin")
    public String adminDashboard(Model model) {

        var users = userRepository.findAll();

        var recentUsers = users.stream()
                .sorted((u1, u2) -> Long.compare(
                        u2.getUserId(),
                        u1.getUserId()
                ))
                .limit(5)
                .toList();

        model.addAttribute("recentUsers", recentUsers);

        long totalStudents = users.stream()
                .filter(user ->
                        user.getRole() == User.Role.STUDENT
                )
                .count();

        long totalFaculty = users.stream()
                .filter(user ->
                        user.getRole() == User.Role.FACULTY
                )
                .count();

        long pendingFaculty = users.stream()
                .filter(user ->
                        user.getRole() == User.Role.FACULTY
                                && user.getStatus() == User.Status.PENDING
                )
                .count();

        long pendingStudents = users.stream()
                .filter(user ->
                        user.getRole() == User.Role.STUDENT
                                && user.getStatus() == User.Status.PENDING
                )
                .count();

        long totalLibrarians = users.stream()
                .filter(user ->
                        user.getRole() == User.Role.LIBRARIAN
                )
                .count();

        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalFaculty", totalFaculty);
        model.addAttribute("pendingFaculty", pendingFaculty);
        model.addAttribute("pendingStudents", pendingStudents);
        model.addAttribute("totalLibrarians", totalLibrarians);

        return "admin/dashboard";
    }

    // =========================
    // FACULTY APPROVAL PAGE
    // =========================

    @GetMapping("/admin/faculty")
    public String facultyApproval(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model) {

        var facultyList = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() == User.Role.FACULTY
                )
                .toList();

        if (search != null && !search.trim().isEmpty()) {

            String keyword = search.trim().toLowerCase();

            facultyList = facultyList.stream()
                    .filter(user ->
                            user.getUsername().toLowerCase().contains(keyword)
                                    || user.getEmail().toLowerCase().contains(keyword)
                    )
                    .toList();
        }

        if (status != null && !status.isEmpty()) {

            facultyList = facultyList.stream()
                    .filter(user ->
                            user.getStatus().name().equals(status)
                    )
                    .toList();
        }

        model.addAttribute("facultyList", facultyList);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);

        return "admin/faculty";
    }

    // =========================
    // APPROVE FACULTY
    // =========================

    @PostMapping("/admin/faculty/approve/{userId}")
    public String approveFaculty(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole() == User.Role.FACULTY) {

            user.setStatus(User.Status.APPROVED);

            userRepository.save(user);
        }

        return "redirect:/admin/faculty";
    }

    // =========================
    // REJECT FACULTY
    // =========================

    @PostMapping("/admin/faculty/reject/{userId}")
    public String rejectFaculty(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole() == User.Role.FACULTY) {

            user.setStatus(User.Status.REJECTED);

            userRepository.save(user);
        }

        return "redirect:/admin/faculty";
    }

    // =========================
    // ACTIVATE / DEACTIVATE FACULTY
    // =========================

    @PostMapping("/admin/faculty/toggle/{userId}")
    public String toggleFaculty(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole() == User.Role.FACULTY) {

            user.setActive(!user.getActive());

            userRepository.save(user);
        }

        return "redirect:/admin/faculty";
    }

    // =========================
    // DELETE FACULTY
    // =========================

    @PostMapping("/admin/faculty/delete/{userId}")
    public String deleteFaculty(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole() == User.Role.FACULTY) {

            userRepository.delete(user);
        }

        return "redirect:/admin/faculty";
    }

    // =========================
    // USER MANAGEMENT
    // =========================

    @GetMapping("/admin/users")
    public String userManagement(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String active,
            Model model) {

        var users = userRepository.findAll();

        if (search != null && !search.trim().isEmpty()) {

            String keyword = search.trim().toLowerCase();

            users = users.stream()
                    .filter(user ->
                            user.getUsername().toLowerCase().contains(keyword)
                                    || user.getEmail().toLowerCase().contains(keyword)
                    )
                    .toList();
        }

        if (role != null && !role.isEmpty()) {

            users = users.stream()
                    .filter(user ->
                            user.getRole().name().equals(role)
                    )
                    .toList();
        }

        if (status != null && !status.isEmpty()) {

            users = users.stream()
                    .filter(user ->
                            user.getStatus().name().equals(status)
                    )
                    .toList();
        }

        if (active != null && !active.isEmpty()) {

            boolean isActive = Boolean.parseBoolean(active);

            users = users.stream()
                    .filter(user ->
                            user.getActive() == isActive
                    )
                    .toList();
        }

        var allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();

        long totalStudents = allUsers.stream()
                .filter(user ->
                        user.getRole() == User.Role.STUDENT
                )
                .count();

        long totalFaculty = allUsers.stream()
                .filter(user ->
                        user.getRole() == User.Role.FACULTY
                )
                .count();

        long totalLibrarians = allUsers.stream()
                .filter(user ->
                        user.getRole() == User.Role.LIBRARIAN
                )
                .count();

        model.addAttribute("users", users);

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("totalFaculty", totalFaculty);
        model.addAttribute("totalLibrarians", totalLibrarians);

        model.addAttribute("search", search);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedActive", active);

        return "admin/users";
    }

    // =========================
    // APPROVE STUDENT
    // =========================

    @PostMapping("/admin/users/approve-student/{userId}")
    public String approveStudent(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole() == User.Role.STUDENT) {

            user.setStatus(User.Status.APPROVED);

            userRepository.save(user);
        }

        return "redirect:/admin/users?role=STUDENT";
    }

    // =========================
    // REJECT STUDENT
    // =========================

    @PostMapping("/admin/users/reject-student/{userId}")
    public String rejectStudent(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole() == User.Role.STUDENT) {

            user.setStatus(User.Status.REJECTED);

            userRepository.save(user);
        }

        return "redirect:/admin/users?role=STUDENT";
    }

    // =========================
    // STUDENT MANAGEMENT
    // =========================

    @GetMapping("/admin/students")
    public String studentManagement(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String active,
            Model model) {

        var students = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() == User.Role.STUDENT
                )
                .toList();

        if (search != null && !search.trim().isEmpty()) {

            String keyword = search.trim().toLowerCase();

            students = students.stream()
                    .filter(user ->
                            user.getUsername().toLowerCase().contains(keyword)
                                    || user.getEmail().toLowerCase().contains(keyword)
                    )
                    .toList();
        }

        if (active != null && !active.isEmpty()) {

            boolean isActive = Boolean.parseBoolean(active);

            students = students.stream()
                    .filter(user ->
                            user.getActive() == isActive
                    )
                    .toList();
        }

        long totalStudents = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() == User.Role.STUDENT
                )
                .count();

        long activeStudents = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() == User.Role.STUDENT
                                && Boolean.TRUE.equals(user.getActive())
                )
                .count();

        long inactiveStudents =
                totalStudents - activeStudents;

        model.addAttribute("students", students);
        model.addAttribute("totalStudents", totalStudents);
        model.addAttribute("activeStudents", activeStudents);
        model.addAttribute("inactiveStudents", inactiveStudents);

        model.addAttribute("search", search);
        model.addAttribute("selectedActive", active);

        return "admin/students";
    }

    // =========================
    // ACTIVATE / DEACTIVATE USER
    // =========================

    @PostMapping("/admin/users/toggle/{userId}")
    public String toggleUser(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setActive(!user.getActive());

        userRepository.save(user);

        return "redirect:/admin/users";
    }

    // =========================
    // DELETE USER
    // =========================

    @PostMapping("/admin/users/delete/{userId}")
    public String deleteUser(@PathVariable Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getRole() == User.Role.ADMIN) {
            return "redirect:/admin/users?error=admin-delete";
        }

        // Delete student's issued-book history first
        // because issued_books contains records linked
        // with the student's username.
        if (user.getRole() == User.Role.STUDENT) {

            issuedBookRepository
                    .deleteByStudentNameIgnoreCase(
                            user.getUsername()
                    );
        }

        // Delete the user account
        userRepository.delete(user);

        return "redirect:/admin/users";
    }
}