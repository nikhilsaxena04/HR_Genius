package com.hrgenius.repository;

import com.hrgenius.model.Employee;
import com.hrgenius.model.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Data access operations for monthly payroll records.
// Supports employee payslip history and period-based salary statement generation.
@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    List<Payroll> findByEmployeeOrderByYearDescMonthDesc(Employee employee);

    Optional<Payroll> findByEmployeeAndMonthAndYear(Employee employee, int month, int year);

    List<Payroll> findByMonthAndYear(int month, int year);

    List<Payroll> findAllByOrderByYearDescMonthDesc();
}
