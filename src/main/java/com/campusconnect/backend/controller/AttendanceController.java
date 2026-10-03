package com.campusconnect.backend.controller;

import com.campusconnect.backend.dto.AttendanceBulkRequest;
import com.campusconnect.backend.entity.Attendance;
import com.campusconnect.backend.entity.User;
import com.campusconnect.backend.repository.AttendanceRepository;
import com.campusconnect.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    // ---------------------------------------------------------
    // GET APPROVED STUDENTS
    // ---------------------------------------------------------

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

    // ---------------------------------------------------------
    // GET ATTENDANCE RECORDS
    // ---------------------------------------------------------

    @GetMapping
    public ResponseEntity<?> getAttendance(
            @RequestParam(required = false) String studentUsername,
            @RequestParam(required = false) String facultyUsername) {

        if (studentUsername != null
                && !studentUsername.isEmpty()) {

            return ResponseEntity.ok(
                    attendanceRepository.findByStudentUsername(
                            studentUsername
                    )
            );
        }

        if (facultyUsername != null
                && !facultyUsername.isEmpty()) {

            return ResponseEntity.ok(
                    attendanceRepository
                            .findByFacultyUsernameOrderByAttendanceDateDesc(
                                    facultyUsername
                            )
            );
        }

        return ResponseEntity.ok(
                attendanceRepository.findAll()
        );
    }

    // ---------------------------------------------------------
    // SAVE SINGLE ATTENDANCE
    // ---------------------------------------------------------

    @PostMapping
    public ResponseEntity<?> saveAttendance(
            @RequestBody Attendance attendance) {

        if (attendance.getAttendanceDate() == null) {
            attendance.setAttendanceDate(LocalDate.now());
        }

        Attendance savedAttendance =
                attendanceRepository.save(attendance);

        return ResponseEntity.ok(savedAttendance);
    }

    // ---------------------------------------------------------
    // SAVE BULK ATTENDANCE
    // ---------------------------------------------------------

    @PostMapping("/bulk")
    public ResponseEntity<?> saveBulkAttendance(
            @RequestBody AttendanceBulkRequest request) {

        if (request.getAttendanceList() == null
                || request.getAttendanceList().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Attendance list cannot be empty");
        }

        String facultyUsername =
                request.getFacultyUsername();

        LocalDate attendanceDate;

        if (request.getAttendanceDate() == null
                || request.getAttendanceDate().isEmpty()) {

            attendanceDate = LocalDate.now();

        } else {

            attendanceDate =
                    LocalDate.parse(
                            request.getAttendanceDate()
                    );
        }

        List<Attendance> savedAttendance =
                new ArrayList<>();

        for (AttendanceBulkRequest.AttendanceItem item
                : request.getAttendanceList()) {

            if (item.getStudentUsername() == null
                    || item.getStudentUsername().isEmpty()) {

                continue;
            }

            Attendance attendance =
                    new Attendance();

            attendance.setStudentUsername(
                    item.getStudentUsername()
            );

            attendance.setFacultyUsername(
                    facultyUsername
            );

            attendance.setAttendanceDate(
                    attendanceDate
            );

            attendance.setPresent(
                    item.isPresent()
            );

            savedAttendance.add(
                    attendanceRepository.save(
                            attendance
                    )
            );
        }

        return ResponseEntity.ok(
                savedAttendance
        );
    }

    // ---------------------------------------------------------
    // ATTENDANCE HISTORY
    // ---------------------------------------------------------

    @GetMapping("/history")
    public ResponseEntity<?> getAttendanceHistory(
            @RequestParam String facultyUsername) {

        List<Attendance> attendanceList =
                attendanceRepository
                        .findByFacultyUsernameOrderByAttendanceDateDesc(
                                facultyUsername
                        );

        return ResponseEntity.ok(
                attendanceList
        );
    }

    // ---------------------------------------------------------
    // STUDENT-WISE ATTENDANCE SUMMARY
    // ---------------------------------------------------------

    @GetMapping("/summary")
    public ResponseEntity<?> getAttendanceSummary(
            @RequestParam String facultyUsername) {

        List<Attendance> attendanceList =
                attendanceRepository
                        .findByFacultyUsername(
                                facultyUsername
                        );

        Map<String, List<Attendance>> groupedAttendance =
                new LinkedHashMap<>();

        for (Attendance attendance : attendanceList) {

            groupedAttendance
                    .computeIfAbsent(
                            attendance.getStudentUsername(),
                            key -> new ArrayList<>()
                    )
                    .add(attendance);
        }

        List<Map<String, Object>> summary =
                new ArrayList<>();

        for (Map.Entry<String, List<Attendance>> entry
                : groupedAttendance.entrySet()) {

            String studentUsername =
                    entry.getKey();

            List<Attendance> studentAttendance =
                    entry.getValue();

            int totalClasses =
                    studentAttendance.size();

            int presentCount =
                    (int) studentAttendance
                            .stream()
                            .filter(Attendance::isPresent)
                            .count();

            int absentCount =
                    totalClasses - presentCount;

            double attendancePercentage = 0.0;

            if (totalClasses > 0) {

                attendancePercentage =
                        (presentCount * 100.0)
                                / totalClasses;
            }

            User student =
                    userRepository
                            .findByUsername(
                                    studentUsername
                            )
                            .orElse(null);

            Map<String, Object> studentSummary =
                    new LinkedHashMap<>();

            studentSummary.put(
                    "username",
                    studentUsername
            );

            studentSummary.put(
                    "fullName",
                    student != null
                            ? student.getFullName()
                            : studentUsername
            );

            studentSummary.put(
                    "rollNo",
                    student != null
                            ? student.getRollNo()
                            : ""
            );

            studentSummary.put(
                    "totalClasses",
                    totalClasses
            );

            studentSummary.put(
                    "present",
                    presentCount
            );

            studentSummary.put(
                    "absent",
                    absentCount
            );

            studentSummary.put(
                    "percentage",
                    Math.round(
                            attendancePercentage * 100.0
                    ) / 100.0
            );

            summary.add(
                    studentSummary
            );
        }

        summary.sort(
                Comparator.comparing(
                        item ->
                                item.get("fullName")
                                        .toString()
                )
        );

        return ResponseEntity.ok(
                summary
        );
    }

    // ---------------------------------------------------------
    // INDIVIDUAL STUDENT ATTENDANCE SUMMARY
    // ---------------------------------------------------------

    @GetMapping("/summary/student")
    public ResponseEntity<?> getStudentAttendanceSummary(
            @RequestParam String facultyUsername,
            @RequestParam String studentUsername) {

        List<Attendance> attendanceList =
                attendanceRepository
                        .findByFacultyUsername(
                                facultyUsername
                        )
                        .stream()
                        .filter(attendance ->
                                attendance
                                        .getStudentUsername()
                                        .equalsIgnoreCase(
                                                studentUsername
                                        )
                        )
                        .toList();

        int totalClasses =
                attendanceList.size();

        int presentCount =
                (int) attendanceList
                        .stream()
                        .filter(Attendance::isPresent)
                        .count();

        int absentCount =
                totalClasses - presentCount;

        double percentage = 0.0;

        if (totalClasses > 0) {

            percentage =
                    (presentCount * 100.0)
                            / totalClasses;
        }

        User student =
                userRepository
                        .findByUsername(
                                studentUsername
                        )
                        .orElse(null);

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put(
                "username",
                studentUsername
        );

        result.put(
                "fullName",
                student != null
                        ? student.getFullName()
                        : studentUsername
        );

        result.put(
                "rollNo",
                student != null
                        ? student.getRollNo()
                        : ""
        );

        result.put(
                "totalClasses",
                totalClasses
        );

        result.put(
                "present",
                presentCount
        );

        result.put(
                "absent",
                absentCount
        );

        result.put(
                "percentage",
                Math.round(
                        percentage * 100.0
                ) / 100.0
        );

        return ResponseEntity.ok(
                result
        );
    }
}