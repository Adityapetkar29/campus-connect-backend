package com.campusconnect.backend.repository;

import com.campusconnect.backend.entity.IssuedBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface IssuedBookRepository extends JpaRepository<IssuedBook, Long> {

    @Transactional
    void deleteByStudentNameIgnoreCase(String studentName);
}