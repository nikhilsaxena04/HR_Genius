# HRGenius — Human Resource Management System

> **College Project:** A clean, robust, and reliable HR Management System built with Spring Boot 3, Thymeleaf, Spring Security 6, and H2 Database. Designed specifically for simplicity, complete understandability, and viva demonstrations.

---

## 1. Tech Stack Overview

- **Backend Framework:** Java 17, Spring Boot 3.2.5 (Spring MVC, Spring Data JPA, Spring Security 6)
- **View Engine:** Thymeleaf with `thymeleaf-extras-springsecurity6` (Native fragments, zero external layout dependencies)
- **Database:** H2 in File Mode (`jdbc:h2:file:./data/hrgenius;AUTO_SERVER=TRUE`), Hibernate with `ddl-auto=update`
- **Security:** Session-based Form Login, `BCryptPasswordEncoder`, Role-Based Access Control (`ADMIN`, `EMPLOYEE`)
- **Validation:** Bean Validation (`jakarta.validation.*`)
- **Testing:** JUnit 5, Spring Boot Test, Spring Security Test (`MockMvc` with `@WithMockUser`)
- **Frontend Design:** Minimalist White & Black palette, Google Font "Inter", vanilla CSS (`style.css`), print-ready payslips
- **Code Standards:** Explicit getters/setters/constructors (No Lombok), constructor injection, unidirectional JPA relationships, two-line student explanation comments throughout.

---

## 2. Project Directory Structure

```
hrgenius/
├── pom.xml                                      # Maven project configuration
├── PLAN.md                                      # Initial blueprint & architectural contract
├── README.md                                    # Documentation and run guide
├── VIVA_AND_DEMO.md                             # 10-step demo script & 15 viva Q&As
└── src/
    ├── main/
    │   ├── java/com/hrgenius/
    │   │   ├── HrGeniusApplication.java         # Spring Boot entry point
    │   │   ├── config/                          # Security, Seeder, Global Exception Handler
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── DataSeeder.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── model/                           # Entity classes & domain enums
    │   │   │   ├── Department.java
    │   │   │   ├── Employee.java
    │   │   │   ├── User.java
    │   │   │   ├── Attendance.java
    │   │   │   ├── LeaveRequest.java
    │   │   │   ├── Payroll.java
    │   │   │   └── Enums (Role, EmployeeStatus, AttendanceStatus, LeaveType, LeaveStatus)
    │   │   ├── repository/                      # Spring Data JPA repositories
    │   │   ├── service/                         # Core business logic services
    │   │   └── controller/                      # Web controllers and error handlers
    │   └── resources/
    │       ├── application.properties           # H2 file mode configuration
    │       ├── application-mysql.properties     # Optional MySQL profile
    │       ├── static/css/style.css             # Minimalist design system
    │       └── templates/                       # Thymeleaf views and fragments
    │           ├── layout.html
    │           ├── login.html
    │           ├── dashboard.html
    │           ├── departments/ (list, form)
    │           ├── employees/   (list, form, view)
    │           ├── attendance/  (index)
    │           ├── leaves/      (index, apply)
    │           ├── payroll/     (index, payslip)
    │           └── error.html
    └── test/java/com/hrgenius/
        └── HrGeniusApplicationTests.java        # Comprehensive MockMvc integration tests
```

---

## 3. Seeded Accounts & Credentials

The system automatically initializes demo data upon first boot:

| Role | Email | Password | Details |
|---|---|---|---|
| **Administrator (HR)** | `admin@hrgenius.com` | `Admin@123` | Full access across all 8 modules |
| **Employee 1** | `rahul@hrgenius.com` | `Welcome@123` | Software Engineer (Engineering) |
| **Employee 2** | `priya@hrgenius.com` | `Welcome@123` | Senior Developer (Engineering) |
| **Employee 3** | `amit@hrgenius.com` | `Welcome@123` | HR Executive (HR) |
| **Employee 4** | `sneha@hrgenius.com` | `Welcome@123` | Financial Analyst (Finance) |
| **Employee 5** | `vikram@hrgenius.com` | `Welcome@123` | Senior Accountant (Finance) |

---

## 4. How to Run the Application

### Prerequisites
- Java 17 or higher (`java -version`)
- Apache Maven 3.8+ (`mvn -version`)

### Quick Start (Single Command)
```bash
# Clone or navigate to the repository
cd HR_Genius

# Run tests
mvn test

# Start the Spring Boot application
mvn spring-boot:run
```

Once started, open your web browser at:
👉 **`http://localhost:8080`**

### Accessing the H2 Web Console
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:file:./data/hrgenius;AUTO_SERVER=TRUE`
- User Name: `sa`
- Password: *(leave blank)*

### Optional: Running with MySQL Profile
To switch to a local MySQL database:
1. Ensure MySQL is running on `localhost:3306` with database `hrgenius`.
2. Launch with the active profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

---

## 5. Key Features Breakdown

1. **Authentication & Session Security:** Custom login page (`/login`), secure password hashing via `BCrypt`, and immediate rejection of deactivated employee accounts.
2. **Role-Tailored Dashboards:**
   - **Admin:** 4 real-time KPI stat cards + recent leave applications queue.
   - **Employee:** Daily attendance clock status, 12-day annual leave quota counter, and latest salary statement link.
3. **Department Management (Admin):** Full CRUD with referential integrity deletion guards (prevents deletion if staff are assigned).
4. **Employee Management (Admin):** Live keyword search (name/email), department filter, onboarding with auto-generated code (`EMP001`) and automated login account provisioning, plus one-click deactivation.
5. **Attendance Tracking:**
   - **Employee:** Simple "Check In" and "Check Out" buttons with single daily entry enforcement and 30-day personal history.
   - **Admin:** Organization-wide daily attendance inspection with a historical calendar date selector.
6. **Leave Management:**
   - **Employee:** 12-day annual quota calculation, past date blocking, validation (`toDate >= fromDate`), and personal history table.
   - **Admin:** Filter by status (`PENDING`, `APPROVED`, `REJECTED`), inline approval/rejection with remarks.
7. **Payroll & Payslips:**
   - **Admin:** Batch generation per month/year for active employees with duplicate protection. Mathematical formula: Basic Salary + 20% Allowances - 12% Deductions = Net Salary (108% of basic).
   - **Employee:** View and print official salary slips (`window.print()` with clean `@media print` styling). URL tampering is strictly blocked (employees cannot access peers' payslips).
8. **Friendly Error Pages:** Comprehensive `GlobalExceptionHandler` and `CustomErrorController` displaying customized, clean 403, 404, and 500 error pages.
