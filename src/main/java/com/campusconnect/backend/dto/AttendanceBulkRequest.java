package com.campusconnect.backend.dto;

import java.util.List;

public class AttendanceBulkRequest {

    private String facultyUsername;
    private String attendanceDate;
    private List<AttendanceItem> attendanceList;

    public AttendanceBulkRequest() {
    }

    public String getFacultyUsername() {
        return facultyUsername;
    }

    public void setFacultyUsername(String facultyUsername) {
        this.facultyUsername = facultyUsername;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(String attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public List<AttendanceItem> getAttendanceList() {
        return attendanceList;
    }

    public void setAttendanceList(List<AttendanceItem> attendanceList) {
        this.attendanceList = attendanceList;
    }

    public static class AttendanceItem {

        private String studentUsername;
        private boolean present;

        public AttendanceItem() {
        }

        public String getStudentUsername() {
            return studentUsername;
        }

        public void setStudentUsername(String studentUsername) {
            this.studentUsername = studentUsername;
        }

        public boolean isPresent() {
            return present;
        }

        public void setPresent(boolean present) {
            this.present = present;
        }
    }
}