# HRGenius — Viva Presentation & Demonstration Guide

This guide is designed to help you ace your college project demo and viva examination. It contains a 10-step structured live demo walk-through followed by 15 high-frequency technical viva questions and clear, confident answers.

---

## Part 1: 10-Step Live Demonstration Script

Follow this step-by-step sequence during your project demonstration to showcase every core requirement smoothly.

### Step 1: Show Clean Single-Command Launch
- Open terminal and highlight the project directory.
- Run `mvn clean test` to demonstrate all unit and integration tests passing.
- Run `mvn spring-boot:run` and note how Spring Boot initializes, connects to the H2 file database, and executes `DataSeeder` automatically.

### Step 2: Login as Administrator
- Navigate to `http://localhost:8080/login`.
- Enter `admin@hrgenius.com` / `Admin@123`.
- Point out the clean redirection to `/dashboard` and show the 4 stat cards (Active Employees: 5, Present Today: 4, Pending Leaves: 1, Departments: 3) and the Recent Leave Applications table.

### Step 3: Manage Departments (CRUD & Integrity Guard)
- Click on **Departments** in the sidebar.
- Click **+ Add Department**, enter `Marketing`, and save.
- Attempt to delete the `Engineering` department:
  - Explain to the examiner that the system intercepts the deletion and shows a friendly error message (`Cannot delete department 'Engineering' because it currently has 2 employee(s) assigned to it`).
  - Demonstrate referential integrity protection.

### Step 4: Employee Onboarding & Automated Login Provisioning
- Click on **Employees** in the sidebar.
- Click **+ Add Employee**.
- Fill in:
  - Name: `Rohan Verma`
  - Email: `rohan@hrgenius.com`
  - Department: `Marketing`
  - Designation: `Digital Marketing Associate`
  - Joining Date: *select today or earlier*
  - Basic Salary: `42000.00`
- Click **Save Employee**.
- Point out that:
  - Employee code was automatically generated as `EMP006`.
  - A corresponding login user account (`username = rohan@hrgenius.com`, default password `Welcome@123`, role `EMPLOYEE`) was created inside the same `@Transactional` boundary.

### Step 5: Search & Filter Employees
- In the Employee list, type `rohan` in the search box and click **Filter** &rarr; only Rohan's record is shown.
- Filter by department `Finance` &rarr; shows Sneha and Vikram.
- Click **Reset** to return to the full list.

### Step 6: Review Daily Attendance as Admin
- Click on **Attendance** in the sidebar.
- Show today's attendance log displaying check-in times for Rahul, Priya, Amit, Vikram, and Sneha marked as `ABSENT`.
- Select an older date using the date picker to demonstrate historical inspection.

### Step 7: Review & Approve Leave Requests
- Click on **Leaves** in the sidebar.
- Show Rahul Sharma's pending leave request.
- Click **Take Action**, enter an optional remark (`"Approved, enjoy the event"`), and click **Approve**.
- Notice the status updates immediately to `APPROVED` with the green badge.

### Step 8: Generate Monthly Payroll & View Payslips
- Click on **Payroll** in the sidebar.
- Select the current month and year, then click **Generate Payroll**.
- Show how salaries are automatically calculated:
  - `Basic Salary`
  - `Allowances (20% of basic)`
  - `Deductions (12% of basic)`
  - `Net Salary = Basic + Allowances - Deductions (108% of basic)`
- Click **View Payslip** on Rahul's row.
- Click **Print Payslip** or press `Ctrl + P` to show the browser print preview: sidebar, navigation, and buttons cleanly disappear due to CSS `@media print` rules.

### Step 9: Employee Experience & Self-Service
- Log out via the topbar Logout button.
- Log in as employee `rahul@hrgenius.com` with password `Welcome@123`.
- Notice the customized Employee Dashboard:
  - Today's clock status
  - Annual Leave balance (Total: 12, Used: 2, Remaining: 10)
  - Direct link to his latest payslip.
- Navigate to **Attendance** and explain the Check-In/Check-Out prevention rules.
- Navigate to **Leaves** &rarr; **Apply for Leave**:
  - Show how past dates are prevented.
  - Show how exceeding 12 annual days is blocked.

### Step 10: Security Enforcement & Account Deactivation
- While logged in as `rahul@hrgenius.com`, attempt URL manipulation:
  - Try visiting `http://localhost:8080/employees` &rarr; blocked with a friendly **403 Access Denied** page.
  - Try accessing Priya's payslip at `http://localhost:8080/payroll/payslip/2` &rarr; blocked with **403 Access Denied** because the controller validates employee ownership against `Principal`.
- Log back in as `admin@hrgenius.com`, go to **Employees**, and click **Deactivate** on `sneha@hrgenius.com`.
- Log out and attempt to log in as `sneha@hrgenius.com` &rarr; blocked at the login screen with an account deactivated alert.

---

## Part 2: Top 15 Viva Questions & Model Answers

### Q1: What architecture pattern does this project follow?
**Answer:** HRGenius follows the standard Model-View-Controller (MVC) architectural pattern:
- **Model:** JPA entities (`User`, `Employee`, `Department`, `Attendance`, `LeaveRequest`, `Payroll`) representing data stored in H2.
- **View:** Server-rendered HTML templates using Thymeleaf with native layout fragments.
- **Controller:** Spring `@Controller` classes orchestrating incoming HTTP requests, model population, and view navigation.
Business logic is encapsulated in `@Service` classes, and database access is managed via Spring Data JPA interfaces.

### Q2: Why did you choose H2 file mode instead of in-memory or a full database server?
**Answer:** H2 in file mode (`jdbc:h2:file:./data/hrgenius`) provides two key advantages:
1. **Zero External Dependencies:** No need to install, configure, or run an external database server like MySQL or PostgreSQL, enabling the app to run with a single command.
2. **Data Persistence:** Unlike pure in-memory H2 (`jdbc:h2:mem:`), file mode persists all records across server restarts, ensuring that test data and employee logs are retained.

### Q3: How is authentication and role-based authorization implemented?
**Answer:** We use Spring Security 6 with session-based form login:
- Passwords are encrypted using `BCryptPasswordEncoder` (a salted, slow hashing algorithm).
- Roles (`ADMIN`, `EMPLOYEE`) are evaluated in `SecurityConfig` using `requestMatchers("/employees/**").hasRole("ADMIN")`.
- Method and URL-level checks verify `Principal` to ensure an employee can never read another employee's records or payslip.

### Q4: How does the system prevent deactivated employees from logging in?
**Answer:** Inside `CustomUserDetailsService` (which implements Spring Security's `UserDetailsService`), when loading a user by username:
```java
boolean enabled = true;
if (user.getRole() == Role.EMPLOYEE && user.getEmployee() != null) {
    if (user.getEmployee().getStatus() == EmployeeStatus.INACTIVE) {
        enabled = false;
    }
}
return new org.springframework.security.core.userdetails.User(
    user.getUsername(), user.getPassword(), enabled, ...
);
```
Setting `enabled = false` instructs Spring Security's authentication provider to reject the credentials with a `DisabledException`.

### Q5: Why did you use Unidirectional JPA relationships instead of Bidirectional (`@OneToMany`)?
**Answer:** Bidirectional mappings with `@OneToMany` collections inside parent entities (like a `List<Employee>` inside `Department`) frequently cause two major issues in Spring Boot:
1. `LazyInitializationException` when accessing uninitialized collections outside an active transaction.
2. Circular serialization loops and performance degradation (`N+1` select queries).
By using unidirectional `@ManyToOne` on the child entity only, we keep memory usage low and execute explicit, performant repository queries when child records are needed.

### Q6: How does the leave quota validation work?
**Answer:** Each employee is entitled to 12 days per calendar year. When applying:
1. We reject past dates (`fromDate.isBefore(LocalDate.now())`).
2. We reject invalid ranges (`toDate.isBefore(fromDate)`).
3. We query all approved leaves for the employee in that calendar year, sum the elapsed days using `ChronoUnit.DAYS.between(start, end) + 1`, and ensure `usedDays + requestedDays <= 12`.

### Q7: What is the salary calculation formula used in Payroll?
**Answer:** We use `BigDecimal` with 2 decimal places and `RoundingMode.HALF_UP` to prevent floating-point rounding errors:
- $\text{Basic Salary} = \text{employee.basicSalary}$
- $\text{Allowances} = \text{Basic} \times 0.20$ (20%)
- $\text{Deductions} = \text{Basic} \times 0.12$ (12%)
- $\text{Net Salary} = \text{Basic} + \text{Allowances} - \text{Deductions} = 1.08 \times \text{Basic}$ (108%)

### Q8: What prevents duplicate payroll generation for the same employee in the same month?
**Answer:** Two layers of defense:
1. **Database Constraint:** Unique constraint `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "pay_month", "pay_year"}))`.
2. **Service Idempotency:** Before generating a salary entry, `PayrollService` checks `payrollRepository.findByEmployeeAndMonthAndYear(employee, month, year)`. If present, it skips that employee.

### Q9: Why did you map columns as `pay_month` and `pay_year` instead of `month` and `year`?
**Answer:** `MONTH` and `YEAR` are reserved SQL keywords in ANSI SQL and H2 database. Attempting to create columns named `month` or `year` causes SQL syntax parse exceptions during table schema creation. Mapping them explicitly as `@Column(name = "pay_month")` avoids reserved word collisions.

### Q10: How does the employee attendance clock-in rule work?
**Answer:**
- A database-level unique constraint on `(employee_id, date)` guarantees only one record per employee per calendar date.
- `AttendanceService.checkIn()` checks if a record exists for today; if so, it throws an `IllegalStateException`.
- `checkOut()` verifies that a record exists and that `checkOut == null` before setting the current time.

### Q11: How did you implement the print-friendly payslip without JavaScript libraries?
**Answer:** Using standard CSS `@media print` rules in `style.css`:
```css
@media print {
    .sidebar, .topbar, .btn, .no-print {
        display: none !important;
    }
    .main-wrapper {
        margin-left: 0 !important;
    }
    .payslip-container {
        border: none !important;
    }
}
```
When `window.print()` is triggered, the browser prints only the payslip document itself.

### Q12: How are exceptions and error codes (403, 404, 500) handled?
**Answer:**
1. `GlobalExceptionHandler` (`@ControllerAdvice`): intercepts `AccessDeniedException` (returns HTTP 403), `IllegalArgumentException` (HTTP 400), and unhandled exceptions (HTTP 500).
2. `CustomErrorController` (implements `ErrorController`): captures container-level error dispatches to `/error` (e.g. 404 Not Found) and extracts `RequestDispatcher.ERROR_STATUS_CODE`.
Both render `templates/error.html`, maintaining the consistent minimalist design.

### Q13: Why did you avoid Lombok in this project?
**Answer:** While Lombok reduces boilerplate in enterprise apps, in an academic or college project:
- Standard Java getters, setters, and constructors make the code self-contained and transparent to review.
- It eliminates the need for IDE annotation processor plugins and avoids potential bytecode manipulation quirks during maven builds.

### Q14: How does Spring Boot ensure the database is seeded only once?
**Answer:** `DataSeeder` implements `CommandLineRunner` and executes on startup. The very first line checks:
```java
if (userRepository.count() > 0) {
    return;
}
```
Because H2 is running in persistent file mode, on second and subsequent boots `userRepository.count()` is greater than 0, so seeding is immediately bypassed.

### Q15: How did you test the application?
**Answer:** We wrote an end-to-end integration test suite using `@SpringBootTest` and `@AutoConfigureMockMvc`:
- `@WithMockUser` simulates authenticated requests with `ADMIN` and `EMPLOYEE` roles without needing to start a live browser.
- Tests verify HTTP 200 for all views, verify HTTP 403 when an employee tries to access admin sections or peers' payslips, verify the exact payroll formula, and verify account deactivation logic.
