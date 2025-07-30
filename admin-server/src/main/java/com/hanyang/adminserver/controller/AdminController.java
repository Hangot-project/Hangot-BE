package com.hanyang.adminserver.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AdminController {

    @GetMapping("/admin")
    public String getAdminPage() {
        return "admin";
    }
    
    @GetMapping("/dlq")
    public String getDlqPage() {
        return "admin";
    }
    
    // Spring Boot Admin과 DLQ를 함께 보여주는 통합 페이지
    @GetMapping("/dashboard")
    public String getDashboard() {
        return "dashboard";
    }
}

