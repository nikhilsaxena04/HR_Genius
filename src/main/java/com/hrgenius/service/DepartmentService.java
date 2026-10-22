package com.hrgenius.service;

import com.hrgenius.model.Department;
import com.hrgenius.repository.DepartmentRepository;
import com.hrgenius.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Business service managing organizational departments and hierarchy rules.
// Enforces referential integrity by blocking deletion of departments with assigned employees.
@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public DepartmentService(DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    @Transactional
    public Department save(Department department) {
        return departmentRepository.save(department);
    }

    @Transactional
    public void deleteById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));

        // Referential integrity check: prevent orphaned employees
        long employeeCount = employeeRepository.countByDepartment(department);
        if (employeeCount > 0) {
            throw new IllegalStateException("Cannot delete department '" + department.getName() +
                    "' because it currently has " + employeeCount + " employee(s) assigned to it.");
        }

        departmentRepository.delete(department);
    }
}
