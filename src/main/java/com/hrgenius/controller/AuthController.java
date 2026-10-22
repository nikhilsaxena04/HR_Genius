package com.hrgenius.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Controller managing authentication routing and application entry redirection.
// Directs root path traffic to the central authenticated dashboard.
@Controller
public class AuthController {

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
