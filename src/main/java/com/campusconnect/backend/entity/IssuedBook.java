package com.campusconnect.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "issued_books")
public class IssuedBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String studentName;

    private LocalDate issueDate;

    private LocalDate returnDate;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    public IssuedBook() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    // Due Date = Issue Date + 14 Days
    public LocalDate getDueDate() {
        if (issueDate == null) {
            return null;
        }

        return issueDate.plusDays(14);
    }

    // Calculate Overdue Days
    public long getOverdueDays() {

        LocalDate dueDate = getDueDate();

        if (dueDate == null || returnDate != null) {
            return 0;
        }

        LocalDate today = LocalDate.now();

        if (dueDate.isBefore(today)) {
            return java.time.temporal.ChronoUnit.DAYS.between(dueDate, today);
        }

        return 0;
    }
}