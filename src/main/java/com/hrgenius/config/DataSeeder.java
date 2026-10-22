package com.hrgenius.config;

import com.hrgenius.model.Attendance;
import com.hrgenius.model.AttendanceStatus;
import com.hrgenius.model.Department;
import com.hrgenius.model.Employee;
import com.hrgenius.model.EmployeeStatus;
import com.hrgenius.model.LeaveRequest;
import com.hrgenius.model.LeaveStatus;
import com.hrgenius.model.LeaveType;
import com.hrgenius.model.Payroll;
import com.hrgenius.model.Role;
import com.hrgenius.model.User;
import com.hrgenius.repository.AttendanceRepository;
import com.hrgenius.repository.DepartmentRepository;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.LeaveRequestRepository;
import com.hrgenius.repository.PayrollRepository;
import com.hrgenius.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;

// Seeds initial administrative and demo data if the user database is empty.
// Populates departments, staff members, attendance logs, leaves, and sample payslips.
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final AttendanceRepository attendanceRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final PayrollRepository payrollRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      DepartmentRepository departmentRepository,
                      EmployeeRepository employeeRepository,
                      AttendanceRepository attendanceRepository,
                      LeaveRequestRepository leaveRequestRepository,
                      PayrollRepository payrollRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
        this.attendanceRepository = attendanceRepository;
        this.leaveRequestRepository = leaveRequestRepository;
        this.payrollRepository = payrollRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // 1. Create Admin User
        User admin = new User(
                "admin@hrgenius.com",
                passwordEncoder.encode("Admin@123"),
                Role.ADMIN,
                null
        );
        userRepository.save(admin);

        // 2. Create Departments
        Department engineering = departmentRepository.save(new Department("Engineering"));
        Department hr = departmentRepository.save(new Department("HR"));
        Department finance = departmentRepository.save(new Department("Finance"));

        // 3. Create Sample Employees and Login Accounts
        String defaultEmpPassword = passwordEncoder.encode("Welcome@123");

        Employee rahul = createEmployee("EMP001", "Rahul Sharma", "rahul@hrgenius.com",
                "9876543210", "Software Engineer", engineering,
                LocalDate.of(2024, 1, 15), new BigDecimal("50000.00"), defaultEmpPassword);

        Employee priya = createEmployee("EMP002", "Priya Patel", "priya@hrgenius.com",
                "9876543211", "Senior Developer", engineering,
                LocalDate.of(2024, 1, 15), new BigDecimal("55000.00"), defaultEmpPassword);

        Employee amit = createEmployee("EMP003", "Amit Kumar", "amit@hrgenius.com",
                "9876543212", "HR Executive", hr,
                LocalDate.of(2024, 2, 1), new BigDecimal("45000.00"), defaultEmpPassword);

        Employee sneha = createEmployee("EMP004", "Sneha Reddy", "sneha@hrgenius.com",
                "9876543213", "Financial Analyst", finance,
                LocalDate.of(2024, 2, 1), new BigDecimal("48000.00"), defaultEmpPassword);

        Employee vikram = createEmployee("EMP005", "Vikram Singh", "vikram@hrgenius.com",
                "9876543214", "Senior Accountant", finance,
                LocalDate.of(2024, 3, 1), new BigDecimal("52000.00"), defaultEmpPassword);

        // 4. Sample Attendance for Today
        LocalDate today = LocalDate.now();
        attendanceRepository.save(new Attendance(rahul, today, LocalTime.of(9, 0), LocalTime.of(18, 0), AttendanceStatus.PRESENT));
        attendanceRepository.save(new Attendance(priya, today, LocalTime.of(9, 15), LocalTime.of(18, 15), AttendanceStatus.PRESENT));
        attendanceRepository.save(new Attendance(amit, today, LocalTime.of(9, 30), null, AttendanceStatus.PRESENT));
        attendanceRepository.save(new Attendance(sneha, today, null, null, AttendanceStatus.ABSENT));
        attendanceRepository.save(new Attendance(vikram, today, LocalTime.of(9, 45), null, AttendanceStatus.PRESENT));

        // 5. Sample Leave Requests
        LeaveRequest req1 = new LeaveRequest(
                rahul,
                LeaveType.CASUAL,
                today.plusDays(1),
                today.plusDays(2),
                "Family function",
                LeaveStatus.PENDING,
                null,
                today
        );
        leaveRequestRepository.save(req1);

        LeaveRequest req2 = new LeaveRequest(
                priya,
                LeaveType.SICK,
                today.minusDays(2),
                today.minusDays(1),
                "Unwell",
                LeaveStatus.APPROVED,
                "Get well soon",
                today.minusDays(3)
        );
        leaveRequestRepository.save(req2);

        // 6. Sample Payroll for Rahul and Priya
        createPayroll(rahul, today.getMonthValue(), today.getYear());
        createPayroll(priya, today.getMonthValue(), today.getYear());
    }

    private Employee createEmployee(String empCode, String fullName, String email,
                                    String phone, String designation, Department dept,
                                    LocalDate joinDate, BigDecimal basicSalary, String encodedPassword) {
        Employee employee = new Employee(fullName, email, phone, designation, dept, joinDate, basicSalary, EmployeeStatus.ACTIVE);
        employee.setEmpCode(empCode);
        employee = employeeRepository.save(employee);

        User user = new User(email, encodedPassword, Role.EMPLOYEE, employee);
        userRepository.save(user);

        return employee;
    }

    private void createPayroll(Employee employee, int month, int year) {
        BigDecimal basic = employee.getBasicSalary();
        BigDecimal allowances = basic.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deductions = basic.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal netSalary = basic.add(allowances).subtract(deductions).setScale(2, RoundingMode.HALF_UP);

        Payroll payroll = new Payroll(employee, month, year, basic, allowances, deductions, netSalary, LocalDate.now());
        payrollRepository.save(payroll);
    }
}
