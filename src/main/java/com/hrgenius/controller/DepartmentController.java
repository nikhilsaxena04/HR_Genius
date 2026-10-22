package com.hrgenius.controller;

import com.hrgenius.model.Department;
import com.hrgenius.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controller managing administrative CRUD operations for organizational departments.
// Includes deletion guard handling with user-friendly error messages on constraint violations.
@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public String listDepartments(Model model) {
        model.addAttribute("title", "Departments");
        model.addAttribute("activeMenu", "departments");
        model.addAttribute("departments", departmentService.findAll());
        return "departments/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("title", "Add Department");
        model.addAttribute("activeMenu", "departments");
        model.addAttribute("department", new Department());
        return "departments/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return departmentService.findById(id).map(dept -> {
            model.addAttribute("title", "Edit Department");
            model.addAttribute("activeMenu", "departments");
            model.addAttribute("department", dept);
            return "departments/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("errorMessage", "Department not found with ID: " + id);
            return "redirect:/departments";
        });
    }

    @PostMapping("/save")
    public String saveDepartment(@Valid @ModelAttribute("department") Department department,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("title", department.getId() == null ? "Add Department" : "Edit Department");
            model.addAttribute("activeMenu", "departments");
            return "departments/form";
        }

        try {
            departmentService.save(department);
            redirectAttributes.addFlashAttribute("successMessage", "Department saved successfully.");
        } catch (Exception e) {
            model.addAttribute("title", department.getId() == null ? "Add Department" : "Edit Department");
            model.addAttribute("activeMenu", "departments");
            model.addAttribute("errorMessage", "Could not save department. It may already exist.");
            return "departments/form";
        }

        return "redirect:/departments";
    }

    @GetMapping("/delete/{id}")
    public String deleteDepartment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            departmentService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Department deleted successfully.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to delete department.");
        }
        return "redirect:/departments";
    }
}
