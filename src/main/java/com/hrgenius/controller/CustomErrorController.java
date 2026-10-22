package com.hrgenius.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

// Handles standard HTTP error status dispatches (e.g., 404 Not Found, 403 Forbidden).
// Presents clear and helpful guidance styled in the minimal design system.
@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        int status = 500;
        String message = "An unexpected error occurred.";

        if (statusCode != null) {
            try {
                status = Integer.parseInt(statusCode.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        if (status == HttpStatus.NOT_FOUND.value()) {
            message = "The page or resource you requested could not be found.";
        } else if (status == HttpStatus.FORBIDDEN.value()) {
            message = "You do not have permission to access this resource.";
        } else if (status == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            message = "An internal server error occurred while fulfilling your request.";
        }

        model.addAttribute("status", status);
        model.addAttribute("title", status + " — Error");
        model.addAttribute("message", message);

        return "error";
    }
}
