package com.hrgenius.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

// Stores generated salary statements and payslips for employees.
// Uses pay_month and pay_year column names to prevent H2 SQL keyword conflicts.
@Entity
@Table(
    name = "payroll",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "pay_month", "pay_year"})
)
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "pay_month", nullable = false)
    private int month;

    @Column(name = "pay_year", nullable = false)
    private int year;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basic;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal allowances;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal deductions;

    @NotNull
    @Column(name = "net_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal netSalary;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "generated_on", nullable = false)
    private LocalDate generatedOn;

    public Payroll() {
    }

    public Payroll(Employee employee, int month, int year, BigDecimal basic,
                   BigDecimal allowances, BigDecimal deductions, BigDecimal netSalary, LocalDate generatedOn) {
        this.employee = employee;
        this.month = month;
        this.year = year;
        this.basic = basic;
        this.allowances = allowances;
        this.deductions = deductions;
        this.netSalary = netSalary;
        this.generatedOn = generatedOn != null ? generatedOn : LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BigDecimal getBasic() {
        return basic;
    }

    public void setBasic(BigDecimal basic) {
        this.basic = basic;
    }

    public BigDecimal getAllowances() {
        return allowances;
    }

    public void setAllowances(BigDecimal allowances) {
        this.allowances = allowances;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getNetSalary() {
        return netSalary;
    }

    public void setNetSalary(BigDecimal netSalary) {
        this.netSalary = netSalary;
    }

    public LocalDate getGeneratedOn() {
        return generatedOn;
    }

    public void setGeneratedOn(LocalDate generatedOn) {
        this.generatedOn = generatedOn;
    }
}
