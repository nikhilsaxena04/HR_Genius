# HRGenius — Implementation Plan

> **Goal:** A clean, working HR Management System for a college project.
> **Stack:** Java 17 · Spring Boot 3.2.x · Maven · Thymeleaf · H2 (file) · Spring Security 6

---

## 1. Project Structure (Exact File Paths)

```
hrgenius/
├── pom.xml
├── PLAN.md
├── README.md
├── VIVA_AND_DEMO.md
└── src/
    ├── main/
    │   ├── java/com/hrgenius/
    │   │   ├── HrGeniusApplication.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── DataSeeder.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── model/
    │   │   │   ├── User.java
    │   │   │   ├── Department.java
    │   │   │   ├── Employee.java
    │   │   │   ├── Attendance.java
    │   │   │   ├── LeaveRequest.java
    │   │   │   ├── Payroll.java
    │   │   │   ├── Role.java              (enum)
    │   │   │   ├── EmployeeStatus.java    (enum)
    │   │   │   ├── AttendanceStatus.java  (enum)
    │   │   │   ├── LeaveType.java         (enum)
    │   │   │   ├── LeaveStatus.java       (enum)
    │   │   │   └── (no DTO classes — bind directly to entities)
    │   │   ├── repository/
    │   │   │   ├── UserRepository.java
    │   │   │   ├── DepartmentRepository.java
    │   │   │   ├── EmployeeRepository.java
    │   │   │   ├── AttendanceRepository.java
    │   │   │   ├── LeaveRequestRepository.java
    │   │   │   └── PayrollRepository.java
    │   │   ├── service/
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── DepartmentService.java
    │   │   │   ├── EmployeeService.java
    │   │   │   ├── AttendanceService.java
    │   │   │   ├── LeaveService.java
    │   │   │   └── PayrollService.java
    │   │   └── controller/
    │   │       ├── AuthController.java
    │   │       ├── DashboardController.java
    │   │       ├── DepartmentController.java
    │   │       ├── EmployeeController.java
    │   │       ├── AttendanceController.java
    │   │       ├── LeaveController.java
    │   │       ├── PayrollController.java
    │   │       └── CustomErrorController.java
    │   └── resources/
    │       ├── application.properties
    │       ├── application-mysql.properties
    │       ├── static/css/style.css
    │       └── templates/
    │           ├── layout.html
    │           ├── login.html
    │           ├── dashboard.html
    │           ├── departments/list.html
    │           ├── departments/form.html
    │           ├── employees/list.html
    │           ├── employees/form.html
    │           ├── employees/view.html
    │           ├── attendance/index.html
    │           ├── leaves/index.html
    │           ├── leaves/apply.html
    │           ├── payroll/index.html
    │           ├── payroll/payslip.html
    │           └── error.html
    └── test/java/com/hrgenius/
        └── HrGeniusApplicationTests.java
```

---

## 2. Data Model — Entity Fields & Table/Column Mappings

### 2.1 Enums

| Enum               | Values                          |
|---------------------|---------------------------------|
| `Role`             | `ADMIN`, `EMPLOYEE`             |
| `EmployeeStatus`   | `ACTIVE`, `INACTIVE`            |
| `AttendanceStatus` | `PRESENT`, `ABSENT`, `LEAVE`    |
| `LeaveType`        | `CASUAL`, `SICK`, `PAID`        |
| `LeaveStatus`      | `PENDING`, `APPROVED`, `REJECTED` |

### 2.2 `User` → `@Table(name = "users")`

| Java Field   | Column         | Type         | Constraints                       |
|-------------|----------------|--------------|-----------------------------------|
| `id`        | `id`           | `Long`       | `@Id @GeneratedValue(IDENTITY)`   |
| `username`  | `username`     | `String`     | `@Column(unique = true)`, `@NotBlank`, `@Email` |
| `password`  | `password`     | `String`     | `@NotBlank`                       |
| `role`      | `role`         | `Role`       | `@Enumerated(STRING)`             |
| `employee`  | `employee_id`  | `Employee`   | `@OneToOne`, nullable, `@JoinColumn(name = "employee_id")` |

### 2.3 `Department` → `department`

| Java Field | Column | Type     | Constraints                     |
|-----------|--------|----------|---------------------------------|
| `id`      | `id`   | `Long`   | `@Id @GeneratedValue(IDENTITY)` |
| `name`    | `name` | `String` | `@Column(unique = true)`, `@NotBlank` |

No `@OneToMany` list inside Department (use query instead).

### 2.4 `Employee` → `employee`

| Java Field    | Column          | Type            | Constraints                          |
|--------------|-----------------|-----------------|--------------------------------------|
| `id`         | `id`            | `Long`          | `@Id @GeneratedValue(IDENTITY)`      |
| `empCode`    | `emp_code`      | `String`        | `@Column(unique = true)`             |
| `fullName`   | `full_name`     | `String`        | `@NotBlank`                          |
| `email`      | `email`         | `String`        | `@Column(unique = true)`, `@Email`   |
| `phone`      | `phone`         | `String`        |                                      |
| `designation`| `designation`   | `String`        |                                      |
| `department` | `department_id` | `Department`    | `@ManyToOne`, `@JoinColumn`          |
| `joinDate`   | `join_date`     | `LocalDate`     | `@DateTimeFormat(iso = DATE)`        |
| `basicSalary`| `basic_salary`  | `BigDecimal`    | `@Positive`                          |
| `status`     | `status`        | `EmployeeStatus`| `@Enumerated(STRING)`, default `ACTIVE` |

Auto-generated `empCode`: formatted as `EMP` + zero-padded id (e.g., `EMP001`). Set in `@PrePersist` or in service after save.

### 2.5 `Attendance` → `attendance`

| Java Field | Column        | Type              | Constraints                     |
|-----------|---------------|-------------------|---------------------------------|
| `id`      | `id`          | `Long`            | `@Id @GeneratedValue(IDENTITY)` |
| `employee`| `employee_id` | `Employee`        | `@ManyToOne`, `@JoinColumn`     |
| `date`    | `date`        | `LocalDate`       | `@DateTimeFormat(iso = DATE)`   |
| `checkIn` | `check_in`    | `LocalTime`       |                                 |
| `checkOut`| `check_out`   | `LocalTime`       | nullable                        |
| `status`  | `status`      | `AttendanceStatus`| `@Enumerated(STRING)`           |

Unique constraint: `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "date"}))`

### 2.6 `LeaveRequest` → `leave_request`

| Java Field   | Column         | Type          | Constraints                     |
|-------------|----------------|---------------|---------------------------------|
| `id`        | `id`           | `Long`        | `@Id @GeneratedValue(IDENTITY)` |
| `employee`  | `employee_id`  | `Employee`    | `@ManyToOne`, `@JoinColumn`     |
| `type`      | `type`         | `LeaveType`   | `@Enumerated(STRING)`           |
| `fromDate`  | `from_date`    | `LocalDate`   | `@DateTimeFormat(iso = DATE)`   |
| `toDate`    | `to_date`      | `LocalDate`   | `@DateTimeFormat(iso = DATE)`   |
| `reason`    | `reason`       | `String`      | `@NotBlank`                     |
| `status`    | `status`       | `LeaveStatus` | `@Enumerated(STRING)`, default `PENDING` |
| `adminRemark`| `admin_remark`| `String`      | nullable                        |
| `appliedOn` | `applied_on`   | `LocalDate`   |                                 |

### 2.7 `Payroll` → `payroll`

| Java Field   | Column         | Type          | Constraints                     |
|-------------|----------------|---------------|---------------------------------|
| `id`        | `id`           | `Long`        | `@Id @GeneratedValue(IDENTITY)` |
| `employee`  | `employee_id`  | `Employee`    | `@ManyToOne`, `@JoinColumn`     |
| `month`     | `pay_month`    | `int`         | `@Column(name = "pay_month")`   |
| `year`      | `pay_year`     | `int`         | `@Column(name = "pay_year")`    |
| `basic`     | `basic`        | `BigDecimal`  |                                 |
| `allowances`| `allowances`   | `BigDecimal`  |                                 |
| `deductions`| `deductions`   | `BigDecimal`  |                                 |
| `netSalary` | `net_salary`   | `BigDecimal`  |                                 |
| `generatedOn`| `generated_on`| `LocalDate`   |                                 |

Unique constraint: `@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "pay_month", "pay_year"}))`

---

## 3. Repository Methods

### `UserRepository`
```java
Optional<User> findByUsername(String username);
long count();
```

### `DepartmentRepository`
```java
// standard CRUD via JpaRepository
Optional<Department> findByName(String name);
```

### `EmployeeRepository`
```java
List<Employee> findByStatus(EmployeeStatus status);
List<Employee> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
List<Employee> findByDepartment(Department department);
List<Employee> findByDepartmentAndFullNameContainingIgnoreCaseOrDepartmentAndEmailContainingIgnoreCase(
    Department d1, String name, Department d2, String email);
Optional<Employee> findByEmail(String email);
long countByDepartment(Department department);
long countByStatus(EmployeeStatus status);
```

### `AttendanceRepository`
```java
Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
List<Attendance> findByEmployeeAndDateBetweenOrderByDateDesc(Employee employee, LocalDate from, LocalDate to);
List<Attendance> findByDate(LocalDate date);
long countByDateAndStatus(LocalDate date, AttendanceStatus status);
```

### `LeaveRequestRepository`
```java
List<LeaveRequest> findByEmployee(Employee employee);
List<LeaveRequest> findByStatus(LeaveStatus status);
List<LeaveRequest> findAllByOrderByAppliedOnDesc();
List<LeaveRequest> findTop5ByOrderByAppliedOnDesc();
long countByStatus(LeaveStatus status);
long countByEmployeeAndStatusAndFromDateBetween(Employee emp, LeaveStatus status, LocalDate yearStart, LocalDate yearEnd);
List<LeaveRequest> findByEmployeeAndStatusAndFromDateBetween(Employee emp, LeaveStatus status, LocalDate yearStart, LocalDate yearEnd);
```

### `PayrollRepository`
```java
List<Payroll> findByEmployee(Employee employee);
Optional<Payroll> findByEmployeeAndMonthAndYear(Employee employee, int month, int year);
List<Payroll> findByMonthAndYear(int month, int year);
```

---

## 4. Service Layer — Key Methods

### `CustomUserDetailsService` implements `UserDetailsService`
- `loadUserByUsername(String username)`:
  - Look up `User` by username.
  - If user's role is `EMPLOYEE` and linked employee's status is `INACTIVE`, set `enabled = false`.
  - Return `org.springframework.security.core.userdetails.User` with `ROLE_` + user.role.

### `DepartmentService`
- `findAll()`, `findById(Long id)`, `save(Department dept)`, `deleteById(Long id)`
- `deleteById`: check `employeeRepository.countByDepartment(dept) > 0` → throw `IllegalStateException` with friendly message.

### `EmployeeService`
- `findAll()`, `findById(Long id)`, `findByEmail(String email)`
- `search(String keyword, Long departmentId)`: combines name/email search with optional department filter.
- `save(Employee employee)`: if new → save employee, generate `empCode` = `String.format("EMP%03d", employee.getId())`, then create a `User` with `username = employee.email`, password = BCrypt(`Welcome@123`), role = `EMPLOYEE`, linked employee. All in `@Transactional`.
- `update(Employee employee)`: update existing employee fields.
- `deactivate(Long id)`: set `status = INACTIVE`.

### `AttendanceService`
- `checkIn(Employee employee)`: create `Attendance` for today with `checkIn = LocalTime.now()`, status `PRESENT`. Reject if record already exists for today.
- `checkOut(Employee employee)`: find today's record, set `checkOut = LocalTime.now()`. Reject if no check-in or already checked out.
- `getHistory(Employee employee, int days)`: last N days attendance.
- `getByDate(LocalDate date)`: all attendance records for a date (admin view).
- `getPresentCountToday()`: count of PRESENT for today.

### `LeaveService`
- `apply(LeaveRequest request, Employee employee)`: validate `fromDate >= today`, `toDate >= fromDate`; set `appliedOn = today`, `status = PENDING`, link employee.
- `getByEmployee(Employee employee)`: employee's leave requests.
- `getAll()`, `getByStatus(LeaveStatus status)`: for admin.
- `approve(Long id, String remark)`, `reject(Long id, String remark)`.
- `getUsedLeaveDays(Employee employee, int year)`: sum of days from APPROVED leaves in that year.
  - Days = for each approved leave: `ChronoUnit.DAYS.between(fromDate, toDate) + 1`.
- Total annual leaves = 12. Remaining = 12 - used.

### `PayrollService`
- `generate(int month, int year)`: for all ACTIVE employees, skip if `findByEmployeeAndMonthAndYear` exists. Compute:
  - `basic = employee.basicSalary`
  - `allowances = basic * 0.20` (scale 2, HALF_UP)
  - `deductions = basic * 0.12` (scale 2, HALF_UP)
  - `netSalary = basic + allowances - deductions`
  - `generatedOn = LocalDate.now()`
- `getByEmployee(Employee employee)`, `findById(Long id)`.
- `getByMonthAndYear(int month, int year)`.

---

## 5. Controller Routes

### `AuthController`
| Method | URL           | Action                        | Model Attributes |
|--------|---------------|-------------------------------|------------------|
| GET    | `/login`      | Show login page               | —                |
| GET    | `/`           | Redirect to `/dashboard`      | —                |

### `DashboardController`
| Method | URL           | Action                        | Model Attributes |
|--------|---------------|-------------------------------|------------------|
| GET    | `/dashboard`  | Show role-based dashboard     | `totalEmployees`, `presentToday`, `pendingLeaves`, `totalDepartments`, `recentLeaves` (admin); `employee`, `todayAttendance`, `totalLeaves`=12, `usedLeaves`, `remainingLeaves`, `latestPayslip` (employee) |

### `DepartmentController`
| Method | URL                        | Action              | Model Attributes |
|--------|----------------------------|----------------------|------------------|
| GET    | `/departments`             | List departments     | `departments`    |
| GET    | `/departments/new`         | Show add form        | `department`     |
| GET    | `/departments/edit/{id}`   | Show edit form       | `department`     |
| POST   | `/departments/save`        | Save/update          | redirect         |
| GET    | `/departments/delete/{id}` | Delete department    | redirect (+error msg if employees exist) |

### `EmployeeController`
| Method | URL                           | Action               | Model Attributes |
|--------|-------------------------------|-----------------------|------------------|
| GET    | `/employees`                  | List + search/filter  | `employees`, `departments`, `keyword`, `departmentId` |
| GET    | `/employees/new`              | Show add form         | `employee`, `departments` |
| GET    | `/employees/edit/{id}`        | Show edit form        | `employee`, `departments` |
| GET    | `/employees/view/{id}`        | View details          | `employee`       |
| POST   | `/employees/save`             | Save new/update       | redirect         |
| GET    | `/employees/deactivate/{id}`  | Deactivate employee   | redirect         |

### `AttendanceController`
| Method | URL                     | Action                 | Model Attributes |
|--------|-------------------------|------------------------|------------------|
| GET    | `/attendance`           | Show attendance page   | `attendance` (today), `history` (employee); `attendanceList`, `selectedDate` (admin) |
| POST   | `/attendance/checkin`   | Check in               | redirect         |
| POST   | `/attendance/checkout`  | Check out              | redirect         |

### `LeaveController`
| Method | URL                        | Action                 | Model Attributes |
|--------|----------------------------|------------------------|------------------|
| GET    | `/leaves`                  | List leaves            | `leaves`, `totalLeaves`, `usedLeaves`, `remainingLeaves` (employee); `leaves`, `filterStatus` (admin) |
| GET    | `/leaves/apply`            | Show apply form        | `leaveRequest`, `leaveTypes` |
| POST   | `/leaves/apply`            | Submit leave           | redirect         |
| POST   | `/leaves/approve/{id}`     | Approve (admin)        | redirect         |
| POST   | `/leaves/reject/{id}`      | Reject (admin)         | redirect         |

### `PayrollController`
| Method | URL                       | Action                 | Model Attributes |
|--------|---------------------------|------------------------|------------------|
| GET    | `/payroll`                | List payroll           | `payrolls`, `months`, `years` (admin); `payrolls` (employee) |
| POST   | `/payroll/generate`       | Generate payroll       | redirect         |
| GET    | `/payroll/payslip/{id}`   | View payslip           | `payroll`        |

### `CustomErrorController` implements `ErrorController`
| Method | URL        | Action              | Model Attributes |
|--------|------------|----------------------|------------------|
| GET    | `/error`   | Show error page      | `status`, `message` |

---

## 6. Security Rules (`SecurityConfig`)

```
URL Pattern              | Access
-------------------------|-------------------
/login, /css/**          | permitAll
/departments/**, /employees/** | hasRole("ADMIN")
/payroll/generate        | hasRole("ADMIN")
/dashboard, /attendance, /leaves, /payroll, /payroll/payslip/** | authenticated
```

- Default success URL after login: `/dashboard`
- Logout URL: `/logout` → redirect to `/login?logout`
- CSRF enabled (default).
- In-controller checks: employee payslip access verifies the payslip belongs to the logged-in employee (otherwise 403).

---

## 7. Thymeleaf Templates & Model Attribute Names

### `layout.html`
- Fragment `layout(title, content)` using `th:fragment`.
- Sidebar links: Dashboard, Departments (admin only), Employees (admin only), Attendance, Leaves, Payroll.
- Active state: pass `activeMenu` attribute from each controller method.
- Topbar: page `title`, logged-in user's username, Logout button (POST form).
- The content area uses `th:replace="~{:: content}"` pattern or equivalent.

### `login.html`
- Standalone page (no layout). Centered card with "HRGenius" wordmark, username + password fields, login button.
- Shows error message on `?error` and logout message on `?logout`.

### `dashboard.html`
- Uses layout. `th:if="${isAdmin}"` switches between admin/employee views.
- Admin: 4 stat cards (`totalEmployees`, `presentToday`, `pendingLeaves`, `totalDepartments`), recent leaves table.
- Employee: today's attendance card, leave balance, latest payslip link.

### Template Model Attributes Summary

| Template                 | Key Attributes                                                                |
|--------------------------|-------------------------------------------------------------------------------|
| `dashboard.html`         | `isAdmin`, `totalEmployees`, `presentToday`, `pendingLeaves`, `totalDepartments`, `recentLeaves`, `employee`, `todayAttendance`, `totalLeaves`, `usedLeaves`, `remainingLeaves`, `latestPayslip` |
| `departments/list.html`  | `departments`, `error`                                                        |
| `departments/form.html`  | `department`                                                                  |
| `employees/list.html`    | `employees`, `departments`, `keyword`, `departmentId`                         |
| `employees/form.html`    | `employee`, `departments`                                                     |
| `employees/view.html`    | `employee`                                                                    |
| `attendance/index.html`  | `isAdmin`, `attendance`, `history`, `attendanceList`, `selectedDate`, `message` |
| `leaves/index.html`      | `isAdmin`, `leaves`, `totalLeaves`, `usedLeaves`, `remainingLeaves`, `filterStatus` |
| `leaves/apply.html`      | `leaveRequest`, `leaveTypes`                                                  |
| `payroll/index.html`     | `isAdmin`, `payrolls`, `months`, `years`, `selectedMonth`, `selectedYear`, `message` |
| `payroll/payslip.html`   | `payroll`                                                                     |
| `error.html`             | `status`, `message`                                                           |

---

## 8. Seed Data (`DataSeeder`)

Runs only when `userRepository.count() == 0`.

### Admin User
| Field    | Value               |
|----------|---------------------|
| username | `admin@hrgenius.com`|
| password | BCrypt(`Admin@123`) |
| role     | `ADMIN`             |
| employee | `null`              |

### Departments
`Engineering`, `HR`, `Finance`

### Employees (5)

| empCode | fullName       | email                   | department   | basicSalary | designation          |
|---------|---------------|-------------------------|--------------|-------------|----------------------|
| EMP001  | Rahul Sharma  | rahul@hrgenius.com      | Engineering  | 50000       | Software Engineer    |
| EMP002  | Priya Patel   | priya@hrgenius.com      | Engineering  | 55000       | Senior Developer     |
| EMP003  | Amit Kumar    | amit@hrgenius.com       | HR           | 45000       | HR Executive         |
| EMP004  | Sneha Reddy   | sneha@hrgenius.com      | Finance      | 48000       | Financial Analyst    |
| EMP005  | Vikram Singh  | vikram@hrgenius.com     | Finance      | 52000       | Senior Accountant    |

All with `joinDate = 2024-01-15`, `status = ACTIVE`, `phone = "9876543210"`.
Each gets a `User` with `password = BCrypt("Welcome@123")`, `role = EMPLOYEE`.

### Sample Attendance (for today)
- Rahul: PRESENT, checkIn 09:00
- Priya: PRESENT, checkIn 09:15
- Amit: PRESENT, checkIn 09:30
- Sneha: ABSENT (no checkIn/checkOut)
- Vikram: PRESENT, checkIn 09:45

### Sample Leave Requests
1. Rahul: CASUAL, from tomorrow to day after, reason "Family function", status PENDING
2. Priya: SICK, from 2 days ago to yesterday, reason "Unwell", status APPROVED, adminRemark "Get well soon"

### Sample Payroll (current month for Rahul and Priya)
- Rahul: basic=50000, allowances=10000, deductions=6000, net=54000
- Priya: basic=55000, allowances=11000, deductions=6600, net=59400

---

## 9. Execution Phases

### Phase 0: Save this plan to `PLAN.md`

### Phase 1: Foundation
**Files created:**
- `pom.xml` (spring-boot 3.2.x parent, all starters, H2, test deps)
- `application.properties` (H2 file mode, ddl-auto=update, Thymeleaf settings)
- `application-mysql.properties`
- `HrGeniusApplication.java`
- All 5 enums
- All 6 entity classes (with Jakarta annotations, validation, proper column mappings)
- All 6 repository interfaces
- `SecurityConfig.java` (URL rules, BCryptPasswordEncoder bean, form login config)
- `CustomUserDetailsService.java` (loads user, checks inactive employee)
- `DataSeeder.java` (CommandLineRunner, seeds all data)
- `style.css` (complete design system)
- `layout.html` (sidebar + topbar fragment)
- `login.html`
- **Verify:** `mvn test-compile`

### Phase 2: Departments + Employees
**Files created:**
- `DepartmentService.java`
- `DepartmentController.java`
- `departments/list.html`, `departments/form.html`
- `EmployeeService.java`
- `EmployeeController.java`
- `employees/list.html`, `employees/form.html`, `employees/view.html`
- **Verify:** `mvn test-compile`

### Phase 3: Attendance + Leaves
**Files created:**
- `AttendanceService.java`
- `AttendanceController.java`
- `attendance/index.html`
- `LeaveService.java`
- `LeaveController.java`
- `leaves/index.html`, `leaves/apply.html`
- **Verify:** `mvn test-compile`

### Phase 4: Payroll + Dashboard + Error Handling
**Files created:**
- `PayrollService.java`
- `PayrollController.java`
- `payroll/index.html`, `payroll/payslip.html`
- `DashboardController.java`
- `dashboard.html`
- `GlobalExceptionHandler.java`
- `CustomErrorController.java`
- `error.html`
- `AuthController.java` (simple redirect `/` → `/dashboard`)
- **Verify:** `mvn test-compile`

### Phase 5: Tests + Documentation
**Files created:**
- `HrGeniusApplicationTests.java` (MockMvc tests: admin pages 200, employee pages 200, employee blocked from admin pages 403, cross-employee payslip 403, payroll formula check, inactive login check)
- `README.md` (setup instructions, tech stack, how to run)
- `VIVA_AND_DEMO.md` (10-step demo script + 15 viva Q&As)
- **Verify:** `mvn clean test`

---

## 10. Test Specification (`HrGeniusApplicationTests.java`)

```
@SpringBootTest
@AutoConfigureMockMvc
class HrGeniusApplicationTests {

    // 1. Admin can access all pages → GET /dashboard, /employees, /departments,
    //    /attendance, /leaves, /payroll, /payroll/payslip/1 → 200
    // 2. Employee (rahul) can access own pages → GET /dashboard, /attendance,
    //    /leaves, /payroll → 200
    // 3. Employee (rahul) cannot access admin pages → GET /employees → 403,
    //    GET /departments → 403
    // 4. Employee (rahul) cannot access another employee's payslip → 403
    // 5. Payroll formula: basic=50000 → allowances=10000, deductions=6000, net=54000
    // 6. Deactivated employee cannot log in (UserDetails.enabled = false)
}
```

---

## 11. Definition of Done Checklist

- [ ] App boots cleanly and H2 file database seeds automatically
- [ ] Admin login and employee login both work, deactivated employees are blocked
- [ ] Role access is enforced and an employee cannot open another employee's payslip via URL
- [ ] All 8 features work end-to-end
- [ ] UI matches the minimalist black-and-white design system
- [ ] `mvn clean test` passes 100% of MockMvc and unit tests
- [ ] `README.md` and `VIVA_AND_DEMO.md` are written
