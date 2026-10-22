package com.hrgenius.controller;

import com.hrgenius.model.Attendance;
import com.hrgenius.model.Employee;
import com.hrgenius.model.LeaveRequest;
import com.hrgenius.model.Payroll;
import com.hrgenius.model.Role;
import com.hrgenius.model.User;
import com.hrgenius.repository.DepartmentRepository;
import com.hrgenius.repository.UserRepository;
import com.hrgenius.service.AttendanceService;
import com.hrgenius.service.EmployeeService;
import com.hrgenius.service.LeaveService;
import com.hrgenius.service.PayrollService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Controller rendering personalized dashboards based on user permissions.
// Provides high-level operational KPIs for HR and daily status summaries for employees.
@Controller
public class DashboardController {

    private final EmployeeService employeeService;
    private final DepartmentRepository departmentRepository;
    private final AttendanceService attendanceService;
    private final LeaveService leaveService;
    private final PayrollService payrollService;
    private final UserRepository userRepository;

    public DashboardController(EmployeeService employeeService,
                               DepartmentRepository departmentRepository,
                               AttendanceService attendanceService,
                               LeaveService leaveService,
                               PayrollService payrollService,
                               UserRepository userRepository) {
        this.employeeService = employeeService;
        this.departmentRepository = departmentRepository;
        this.attendanceService = attendanceService;
        this.leaveService = leaveService;
        this.payrollService = payrollService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Principal principal, Model model) {
        model.addAttribute("title", "Dashboard");
        model.addAttribute("activeMenu", "dashboard");

        User user = getUser(principal);
        boolean isAdmin = (user.getRole() == Role.ADMIN);
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            // Admin Metrics & Recent Activity
            model.addAttribute("totalEmployees", employeeService.countActive());
            model.addAttribute("presentToday", attendanceService.getPresentCountToday());
            model.addAttribute("pendingLeaves", leaveService.countPending());
            model.addAttribute("totalDepartments", departmentRepository.count());

            List<LeaveRequest> recentLeaves = leaveService.getRecent5();
            model.addAttribute("recentLeaves", recentLeaves);
        } else {
            // Employee Status & Overview
            Employee employee = user.getEmployee();
            if (employee == null) {
                throw new AccessDeniedException("No employee profile found for account.");
            }

            Optional<Attendance> todayAttendance = attendanceService.getTodayAttendance(employee);
            int currentYear = LocalDate.now().getYear();
            int usedLeaves = leaveService.getUsedLeaveDays(employee, currentYear);
            int remainingLeaves = leaveService.getRemainingLeaveDays(employee, currentYear);
            Optional<Payroll> latestPayslip = payrollService.getLatestForEmployee(employee);

            model.addAttribute("employee", employee);
            model.addAttribute("todayAttendance", todayAttendance.orElse(null));
            model.addAttribute("totalLeaves", LeaveService.ANNUAL_LEAVE_QUOTA);
            model.addAttribute("usedLeaves", usedLeaves);
            model.addAttribute("remainingLeaves", remainingLeaves);
            model.addAttribute("latestPayslip", latestPayslip.orElse(null));
        }

        return "dashboard";
    }

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
    }
}
