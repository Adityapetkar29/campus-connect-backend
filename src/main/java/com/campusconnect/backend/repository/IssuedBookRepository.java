package com.campusconnect.backend.repository;

import com.campusconnect.backend.entity.IssuedBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuedBookRepository extends JpaRepository<IssuedBook, Long> {

    void deleteByStudentNameIgnoreCase(String studentName);
}