package com.hrgenius.controller;

import com.hrgenius.model.Employee;
import com.hrgenius.model.Payroll;
import com.hrgenius.model.Role;
import com.hrgenius.model.User;
import com.hrgenius.repository.UserRepository;
import com.hrgenius.service.PayrollService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

// Controller managing monthly payroll generation, statements listing, and individual payslips.
// Enforces strict ownership checks to prevent employees from viewing peers' financial records.
@Controller
@RequestMapping("/payroll")
public class PayrollController {

    private final PayrollService payrollService;
    private final UserRepository userRepository;

    public PayrollController(PayrollService payrollService, UserRepository userRepository) {
        this.payrollService = payrollService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listPayroll(@RequestParam(name = "month", required = false) Integer month,
                              @RequestParam(name = "year", required = false) Integer year,
                              Principal principal,
                              Model model) {
        model.addAttribute("title", "Payroll");
        model.addAttribute("activeMenu", "payroll");

        User user = getUser(principal);
        boolean isAdmin = (user.getRole() == Role.ADMIN);
        model.addAttribute("isAdmin", isAdmin);

        LocalDate now = LocalDate.now();
        int currentMonth = (month != null) ? month : now.getMonthValue();
        int currentYear = (year != null) ? year : now.getYear();

        if (isAdmin) {
            List<Payroll> payrolls = (month != null && year != null)
                    ? payrollService.getByMonthAndYear(month, year)
                    : payrollService.getAll();
            model.addAttribute("payrolls", payrolls);
            model.addAttribute("selectedMonth", currentMonth);
            model.addAttribute("selectedYear", currentYear);
            model.addAttribute("months", Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));
            model.addAttribute("years", Arrays.asList(currentYear - 1, currentYear, currentYear + 1));
        } else {
            Employee employee = user.getEmployee();
            if (employee == null) {
                throw new AccessDeniedException("No employee profile found for account.");
            }
            model.addAttribute("payrolls", payrollService.getByEmployee(employee));
        }

        return "payroll/index";
    }

    @PostMapping("/generate")
    public String generatePayroll(@RequestParam int month,
                                  @RequestParam int year,
                                  RedirectAttributes redirectAttributes) {
        try {
            List<Payroll> created = payrollService.generateMonthlyPayroll(month, year);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payroll generated successfully for " + created.size() + " employee(s) for " + month + "/" + year + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to generate payroll: " + e.getMessage());
        }
        return "redirect:/payroll?month=" + month + "&year=" + year;
    }

    @GetMapping("/payslip/{id}")
    public String viewPayslip(@PathVariable Long id, Principal principal, Model model) {
        Payroll payroll = payrollService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payslip not found with ID: " + id));

        User user = getUser(principal);
        // Ownership guard: employees cannot view another employee's payslip
        if (user.getRole() == Role.EMPLOYEE) {
            Employee loggedInEmp = user.getEmployee();
            if (loggedInEmp == null || !payroll.getEmployee().getId().equals(loggedInEmp.getId())) {
                throw new AccessDeniedException("You do not have permission to view another employee's payslip.");
            }
        }

        model.addAttribute("title", "Payslip — " + payroll.getEmployee().getFullName());
        model.addAttribute("activeMenu", "payroll");
        model.addAttribute("payroll", payroll);

        return "payroll/payslip";
    }

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
    }
}
