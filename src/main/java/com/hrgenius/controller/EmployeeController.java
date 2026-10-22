package com.hrgenius.controller;

import com.hrgenius.model.Employee;
import com.hrgenius.service.DepartmentService;
import com.hrgenius.service.EmployeeService;
import jakarta.validation.Valid;
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

// Controller managing administrative operations for employee records.
// Supports keyword search, department filtering, profile creation, and deactivation.
@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DepartmentService departmentService;

    public EmployeeController(EmployeeService employeeService, DepartmentService departmentService) {
        this.employeeService = employeeService;
        this.departmentService = departmentService;
    }

    @GetMapping
    public String listEmployees(@RequestParam(name = "keyword", required = false) String keyword,
                                @RequestParam(name = "departmentId", required = false) Long departmentId,
                                Model model) {
        model.addAttribute("title", "Employees");
        model.addAttribute("activeMenu", "employees");
        model.addAttribute("employees", employeeService.search(keyword, departmentId));
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("departmentId", departmentId);
        return "employees/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("title", "Add Employee");
        model.addAttribute("activeMenu", "employees");
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", departmentService.findAll());
        return "employees/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return employeeService.findById(id).map(emp -> {
            model.addAttribute("title", "Edit Employee");
            model.addAttribute("activeMenu", "employees");
            model.addAttribute("employee", emp);
            model.addAttribute("departments", departmentService.findAll());
            return "employees/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found with ID: " + id);
            return "redirect:/employees";
        });
    }

    @GetMapping("/view/{id}")
    public String viewEmployeeDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return employeeService.findById(id).map(emp -> {
            model.addAttribute("title", emp.getFullName() + " — Profile");
            model.addAttribute("activeMenu", "employees");
            model.addAttribute("employee", emp);
            return "employees/view";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Employee not found with ID: " + id);
            return "redirect:/employees";
        });
    }

    @PostMapping("/save")
    public String saveEmployee(@Valid @ModelAttribute("employee") Employee employee,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", employee.getId() == null ? "Add Employee" : "Edit Employee");
            model.addAttribute("activeMenu", "employees");
            model.addAttribute("departments", departmentService.findAll());
            return "employees/form";
        }

        try {
            employeeService.save(employee);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Employee " + employee.getFullName() + " saved successfully.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("title", employee.getId() == null ? "Add Employee" : "Edit Employee");
            model.addAttribute("activeMenu", "employees");
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("errorMessage", e.getMessage());
            return "employees/form";
        } catch (Exception e) {
            model.addAttribute("title", employee.getId() == null ? "Add Employee" : "Edit Employee");
            model.addAttribute("activeMenu", "employees");
            model.addAttribute("departments", departmentService.findAll());
            model.addAttribute("errorMessage", "An error occurred while saving the employee.");
            return "employees/form";
        }

        return "redirect:/employees";
    }

    @GetMapping("/deactivate/{id}")
    public String deactivateEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            employeeService.deactivate(id);
            redirectAttributes.addFlashAttribute("successMessage", "Employee deactivated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to deactivate employee.");
        }
        return "redirect:/employees";
    }
}
