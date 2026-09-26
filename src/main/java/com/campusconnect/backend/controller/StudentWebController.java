package com.campusconnect.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentWebController {

    @GetMapping("/student/login")
    public String studentLogin() {
        return "student/login";
    }

    @GetMapping("/student")
    public String studentDashboard() {
        return "student/dashboard";
    }
}