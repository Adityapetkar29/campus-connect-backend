package com.campusconnect.backend.dto;

public class PendingStudentResponse {

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String rollNo;
    private String role;
    private String status;

    public PendingStudentResponse() {
    }

    public PendingStudentResponse(
            Long userId,
            String username,
            String email,
            String fullName,
            String rollNo,
            String role,
            String status) {

        this.userId = userId;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.rollNo = rollNo;
        this.role = role;
        this.status = status;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRollNo() {
        return rollNo;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}