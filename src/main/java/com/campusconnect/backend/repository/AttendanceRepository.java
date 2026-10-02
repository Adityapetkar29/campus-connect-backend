package com.campusconnect.backend.repository;

import com.campusconnect.backend.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AttendanceRepository
        extends JpaRepository<Attendance, Long> {

    List<Attendance> findByFacultyUsername(
            String facultyUsername
    );

    List<Attendance> findByStudentUsername(
            String studentUsername
    );
}