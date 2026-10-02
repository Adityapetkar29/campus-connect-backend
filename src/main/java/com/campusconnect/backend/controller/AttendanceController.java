package com.campusconnect.backend.controller;

import com.campusconnect.backend.entity.Attendance;
import com.campusconnect.backend.entity.User;
import com.campusconnect.backend.repository.AttendanceRepository;
import com.campusconnect.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/faculty/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;

    public AttendanceController(
            AttendanceRepository attendanceRepository,
            UserRepository userRepository) {

        this.attendanceRepository = attendanceRepository;
        this.userRepository = userRepository;
    }

    // Approved students ki list
    @GetMapping("/students")
    public ResponseEntity<?> getStudents() {

        List<User> students = userRepository.findAll()
                .stream()
                .filter(user ->
                        user.getRole() == User.Role.STUDENT
                                && user.getStatus() == User.Status.APPROVED
                                && user.getActive()
                )
                .toList();

        return ResponseEntity.ok(students);
    }

    // Attendance records
    @GetMapping
    public ResponseEntity<?> getAttendance(
            @RequestParam(required = false) String studentUsername,
            @RequestParam(required = false) String facultyUsername) {

        if (studentUsername != null
                && !studentUsername.isEmpty()) {

            return ResponseEntity.ok(
                    attendanceRepository
                            .findByStudentUsername(studentUsername)
            );
        }

        if (facultyUsername != null
                && !facultyUsername.isEmpty()) {

            return ResponseEntity.ok(
                    attendanceRepository
                            .findByFacultyUsername(facultyUsername)
            );
        }

        return ResponseEntity.ok(
                attendanceRepository.findAll()
        );
    }

    // Attendance save
    @PostMapping
    public ResponseEntity<?> saveAttendance(
            @RequestBody Attendance attendance) {

        if (attendance.getAttendanceDate() == null) {
            attendance.setAttendanceDate(
                    LocalDate.now()
            );
        }

        Attendance savedAttendance =
                attendanceRepository.save(attendance);

        return ResponseEntity.ok(savedAttendance);
    }
}