package com.hrgenius.controller;

import com.hrgenius.model.Employee;
import com.hrgenius.model.LeaveRequest;
import com.hrgenius.model.LeaveStatus;
import com.hrgenius.model.LeaveType;
import com.hrgenius.model.Role;
import com.hrgenius.model.User;
import com.hrgenius.repository.UserRepository;
import com.hrgenius.service.LeaveService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

// Controller managing leave submissions, approval lifecycles, and balance queries.
// Routes administrative decisions for HR and self-service requests for staff members.
@Controller
@RequestMapping("/leaves")
public class LeaveController {

    private final LeaveService leaveService;
    private final UserRepository userRepository;

    public LeaveController(LeaveService leaveService, UserRepository userRepository) {
        this.leaveService = leaveService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listLeaves(@RequestParam(name = "status", required = false) LeaveStatus filterStatus,
                             Principal principal,
                             Model model) {
        model.addAttribute("title", "Leave Management");
        model.addAttribute("activeMenu", "leaves");

        User user = getUser(principal);
        boolean isAdmin = (user.getRole() == Role.ADMIN);
        model.addAttribute("isAdmin", isAdmin);

        if (isAdmin) {
            List<LeaveRequest> leaves = (filterStatus != null)
                    ? leaveService.getByStatus(filterStatus)
                    : leaveService.getAll();
            model.addAttribute("leaves", leaves);
            model.addAttribute("filterStatus", filterStatus);
        } else {
            Employee employee = user.getEmployee();
            if (employee == null) {
                throw new AccessDeniedException("No employee record linked to account.");
            }
            int currentYear = LocalDate.now().getYear();
            int used = leaveService.getUsedLeaveDays(employee, currentYear);
            int remaining = leaveService.getRemainingLeaveDays(employee, currentYear);

            model.addAttribute("leaves", leaveService.getByEmployee(employee));
            model.addAttribute("totalLeaves", LeaveService.ANNUAL_LEAVE_QUOTA);
            model.addAttribute("usedLeaves", used);
            model.addAttribute("remainingLeaves", remaining);
        }

        return "leaves/index";
    }

    @GetMapping("/apply")
    public String showApplyForm(Principal principal, Model model) {
        User user = getUser(principal);
        if (user.getRole() != Role.EMPLOYEE || user.getEmployee() == null) {
            throw new AccessDeniedException("Only employees can submit leave applications.");
        }

        model.addAttribute("title", "Apply for Leave");
        model.addAttribute("activeMenu", "leaves");
        model.addAttribute("leaveRequest", new LeaveRequest());
        model.addAttribute("leaveTypes", LeaveType.values());

        int currentYear = LocalDate.now().getYear();
        model.addAttribute("remainingLeaves", leaveService.getRemainingLeaveDays(user.getEmployee(), currentYear));

        return "leaves/apply";
    }

    @PostMapping("/apply")
    public String applyLeave(@Valid @ModelAttribute("leaveRequest") LeaveRequest leaveRequest,
                             BindingResult bindingResult,
                             Principal principal,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        User user = getUser(principal);
        if (user.getRole() != Role.EMPLOYEE || user.getEmployee() == null) {
            throw new AccessDeniedException("Only employees can submit leave applications.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Apply for Leave");
            model.addAttribute("activeMenu", "leaves");
            model.addAttribute("leaveTypes", LeaveType.values());
            int currentYear = LocalDate.now().getYear();
            model.addAttribute("remainingLeaves", leaveService.getRemainingLeaveDays(user.getEmployee(), currentYear));
            return "leaves/apply";
        }

        try {
            leaveService.apply(leaveRequest, user.getEmployee());
            redirectAttributes.addFlashAttribute("successMessage", "Leave application submitted successfully.");
            return "redirect:/leaves";
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("title", "Apply for Leave");
            model.addAttribute("activeMenu", "leaves");
            model.addAttribute("leaveTypes", LeaveType.values());
            int currentYear = LocalDate.now().getYear();
            model.addAttribute("remainingLeaves", leaveService.getRemainingLeaveDays(user.getEmployee(), currentYear));
            model.addAttribute("errorMessage", e.getMessage());
            return "leaves/apply";
        }
    }

    @PostMapping("/approve/{id}")
    public String approveLeave(@PathVariable Long id,
                               @RequestParam(name = "adminRemark", required = false) String adminRemark,
                               RedirectAttributes redirectAttributes) {
        try {
            leaveService.approve(id, adminRemark);
            redirectAttributes.addFlashAttribute("successMessage", "Leave request approved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to approve leave request.");
        }
        return "redirect:/leaves";
    }

    @PostMapping("/reject/{id}")
    public String rejectLeave(@PathVariable Long id,
                              @RequestParam(name = "adminRemark", required = false) String adminRemark,
                              RedirectAttributes redirectAttributes) {
        try {
            leaveService.reject(id, adminRemark);
            redirectAttributes.addFlashAttribute("successMessage", "Leave request rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reject leave request.");
        }
        return "redirect:/leaves";
    }

    private User getUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
    }
}
