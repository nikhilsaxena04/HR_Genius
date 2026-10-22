package com.hrgenius.controller;

import com.hrgenius.model.Attendance;
import com.hrgenius.model.Employee;
import com.hrgenius.model.Role;
import com.hrgenius.model.User;
import com.hrgenius.repository.UserRepository;
import com.hrgenius.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Controller managing attendance tracking workflows for employees and administrators.
// Enables clock-in/out actions for staff and comprehensive daily log review for HR managers.
@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final UserRepository userRepository;

    public AttendanceController(AttendanceService attendanceService, UserRepository userRepository) {
        this.attendanceService = attendanceService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showAttendance(@RequestParam(name = "date", required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                 Principal principal,
                                 Model model) {
        model.addAttribute("title", "Attendance");
        model.addAttribute("activeMenu", "attendance");

        User user = getUser(principal);
        boolean isAdmin = (user.getRole() == Role.ADMIN);
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            LocalDate targetDate = (date != null) ? date : LocalDate.now();
            List<Attendance> attendanceList = attendanceService.getByDate(targetDate);
            model.addAttribute("attendanceList", attendanceList);
            model.addAttribute("selectedDate", targetDate);
        } else {
            Employee employee = user.getEmployee();
            if (employee == null) {
                throw new AccessDeniedException("No employee profile associated with this account.");
            }
            Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(employee);
            List<Attendance> history = attendanceService.getHistory(employee, 30);

            model.addAttribute("employee", employee);
            model.addAttribute("todayAttendance", todayAttendance.orElse(null));
            model.addAttribute("history", history);
        }

        return "attendance/index";
    }

    @PostMapping("/checkin")
    public String checkIn(Principal principal, RedirectAttributes redirectAttributes) {
        User user = getUser(principal);
        if (user.getRole() != Role.EMPLOYEE || user.getEmployee() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only employees can clock in.");
            return "redirect:/attendance";
        }

        try {
            attendanceService.checkIn(user.getEmployee());
            redirectAttributes.addFlashAttribute("successMessage", "Checked in successfully at " + java.time.LocalTime.now().withNano(0));
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to check in.");
        }

        return "redirect:/attendance";
    }

    @PostMapping("/checkout")
    public String checkOut(Principal principal, RedirectAttributes redirectAttributes) {
        User user = getUser(principal);
        if (user.getRole() != Role.EMPLOYEE || user.getEmployee() == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only employees can clock out.");
            return "redirect:/attendance";
        }

        try {
            attendanceService.checkOut(user.getEmployee());
            redirectAttributes.addFlashAttribute("successMessage", "Checked out successfully at " + java.time.LocalTime.now().withNano(0));
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to check out.");
        }

        return "redirect:/attendance";
    }

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
    }
}
