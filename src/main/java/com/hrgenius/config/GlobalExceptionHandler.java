package com.hrgenius.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

// Intercepts and centralizes uncaught runtime exceptions across controllers.
// Translates security violations and application errors into user-friendly error views.
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(AccessDeniedException ex, Model model) {
        model.addAttribute("status", 403);
        model.addAttribute("title", "403 — Access Denied");
        model.addAttribute("message", ex.getMessage() != null && !ex.getMessage().isEmpty()
                ? ex.getMessage()
                : "You do not have sufficient permissions to access this page or resource.");
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("title", "400 — Invalid Request");
        model.addAttribute("message", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("title", "500 — Server Error");
        model.addAttribute("message", "An unexpected error occurred while processing your request. Please try again.");
        return "error";
    }
}
