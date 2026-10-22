package com.hrgenius.service;

import com.hrgenius.model.Employee;
import com.hrgenius.model.EmployeeStatus;
import com.hrgenius.model.Payroll;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.PayrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Business service calculating monthly salaries, allowances, and statutory deductions.
// Generates reproducible payroll statements for active employees with idempotent batching.
@Service
public class PayrollService {

    public static final BigDecimal ALLOWANCE_RATE = new BigDecimal("0.20"); // 20%
    public static final BigDecimal DEDUCTION_RATE = new BigDecimal("0.12"); // 12%

    private final PayrollRepository payrollRepository;
    private final EmployeeRepository employeeRepository;

    public PayrollService(PayrollRepository payrollRepository, EmployeeRepository employeeRepository) {
        this.payrollRepository = payrollRepository;
        this.employeeRepository = employeeRepository;
    }

    public List<Payroll> getAll() {
        return payrollRepository.findAllByOrderByYearDescMonthDesc();
    }

    public List<Payroll> getByEmployee(Employee employee) {
        return payrollRepository.findByEmployeeOrderByYearDescMonthDesc(employee);
    }

    public Optional<Payroll> findById(Long id) {
        return payrollRepository.findById(id);
    }

    public Optional<Payroll> getLatestForEmployee(Employee employee) {
        List<Payroll> list = payrollRepository.findByEmployeeOrderByYearDescMonthDesc(employee);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<Payroll> getByMonthAndYear(int month, int year) {
        return payrollRepository.findByMonthAndYear(month, year);
    }

    @Transactional
    public List<Payroll> generateMonthlyPayroll(int month, int year) {
        List<Employee> activeEmployees = employeeRepository.findByStatus(EmployeeStatus.ACTIVE);
        List<Payroll> generatedList = new ArrayList<>();

        for (Employee employee : activeEmployees) {
            // Idempotent: skip employees who already have a statement for this month/year
            if (payrollRepository.findByEmployeeAndMonthAndYear(employee, month, year).isPresent()) {
                continue;
            }

            BigDecimal basic = employee.getBasicSalary();
            BigDecimal allowances = basic.multiply(ALLOWANCE_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal deductions = basic.multiply(DEDUCTION_RATE).setScale(2, RoundingMode.HALF_UP);
            BigDecimal netSalary = basic.add(allowances).subtract(deductions).setScale(2, RoundingMode.HALF_UP);

            Payroll payroll = new Payroll(
                    employee,
                    month,
                    year,
                    basic,
                    allowances,
                    deductions,
                    netSalary,
                    LocalDate.now()
            );

            generatedList.add(payrollRepository.save(payroll));
        }

        return generatedList;
    }
}
