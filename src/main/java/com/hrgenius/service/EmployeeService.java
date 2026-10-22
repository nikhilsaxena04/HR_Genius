package com.hrgenius.service;

import com.hrgenius.model.Department;
import com.hrgenius.model.Employee;
import com.hrgenius.model.EmployeeStatus;
import com.hrgenius.model.Role;
import com.hrgenius.model.User;
import com.hrgenius.repository.DepartmentRepository;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

// Business service handling employee onboarding, search filters, and profile updates.
// Automatically provisions a corresponding login account and handles deactivation.
@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           DepartmentRepository departmentRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Optional<Employee> findById(Long id) {
        return employeeRepository.findById(id);
    }

    public Optional<Employee> findByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    public long countActive() {
        return employeeRepository.countByStatus(EmployeeStatus.ACTIVE);
    }

    public long countTotal() {
        return employeeRepository.count();
    }

    public List<Employee> search(String keyword, Long departmentId) {
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasDept = departmentId != null && departmentId > 0;

        if (hasKeyword && hasDept) {
            return employeeRepository.searchByDepartmentAndKeyword(departmentId, keyword.trim());
        } else if (hasKeyword) {
            return employeeRepository.searchByKeyword(keyword.trim());
        } else if (hasDept) {
            return employeeRepository.findByDepartmentId(departmentId);
        } else {
            return employeeRepository.findAll();
        }
    }

    @Transactional
    public Employee save(Employee employee) {
        boolean isNew = (employee.getId() == null);

        if (isNew) {
            // Check for duplicate email
            if (employeeRepository.findByEmail(employee.getEmail()).isPresent()) {
                throw new IllegalArgumentException("An employee with email " + employee.getEmail() + " already exists.");
            }

            // Set initial status and save
            if (employee.getStatus() == null) {
                employee.setStatus(EmployeeStatus.ACTIVE);
            }
            Employee saved = employeeRepository.save(employee);

            // Generate structured employee code (e.g., EMP006)
            saved.setEmpCode(String.format("EMP%03d", saved.getId()));
            saved = employeeRepository.save(saved);

            // Automatically provision a default login account
            User user = new User(
                    saved.getEmail(),
                    passwordEncoder.encode("Welcome@123"),
                    Role.EMPLOYEE,
                    saved
            );
            userRepository.save(user);

            return saved;
        } else {
            // Update existing employee record
            Employee existing = employeeRepository.findById(employee.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + employee.getId()));

            existing.setFullName(employee.getFullName());
            existing.setPhone(employee.getPhone());
            existing.setDesignation(employee.getDesignation());
            existing.setDepartment(employee.getDepartment());
            existing.setJoinDate(employee.getJoinDate());
            existing.setBasicSalary(employee.getBasicSalary());
            if (employee.getStatus() != null) {
                existing.setStatus(employee.getStatus());
            }

            return employeeRepository.save(existing);
        }
    }

    @Transactional
    public void deactivate(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with id: " + id));
        employee.setStatus(EmployeeStatus.INACTIVE);
        employeeRepository.save(employee);
    }
}
