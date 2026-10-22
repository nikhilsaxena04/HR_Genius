package com.hrgenius;

import com.hrgenius.model.Employee;
import com.hrgenius.model.EmployeeStatus;
import com.hrgenius.model.Payroll;
import com.hrgenius.repository.EmployeeRepository;
import com.hrgenius.repository.PayrollRepository;
import com.hrgenius.service.CustomUserDetailsService;
import com.hrgenius.service.PayrollService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Integration and unit test suite verifying security barriers, view rendering, and payroll math.
// Uses MockMvc to simulate requests across both ADMIN and EMPLOYEE authenticated sessions.
@SpringBootTest
@AutoConfigureMockMvc
class HrGeniusApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Test
    @DisplayName("Context loads successfully")
    void contextLoads() {
        assertNotNull(mockMvc);
    }

    @Test
    @DisplayName("Admin has full access to all application endpoints (HTTP 200)")
    @WithMockUser(username = "admin@hrgenius.com", roles = "ADMIN")
    void adminCanAccessAllEndpoints() throws Exception {
        mockMvc.perform(get("/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/employees")).andExpect(status().isOk());
        mockMvc.perform(get("/departments")).andExpect(status().isOk());
        mockMvc.perform(get("/attendance")).andExpect(status().isOk());
        mockMvc.perform(get("/leaves")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll")).andExpect(status().isOk());

        // Payslip 1 was generated for Rahul during DataSeeder startup
        mockMvc.perform(get("/payroll/payslip/1")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Employee can access personal dashboard, attendance, leaves, and payroll (HTTP 200)")
    @WithMockUser(username = "rahul@hrgenius.com", roles = "EMPLOYEE")
    void employeeCanAccessOwnPages() throws Exception {
        mockMvc.perform(get("/dashboard")).andExpect(status().isOk());
        mockMvc.perform(get("/attendance")).andExpect(status().isOk());
        mockMvc.perform(get("/leaves")).andExpect(status().isOk());
        mockMvc.perform(get("/payroll")).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Employee is forbidden from accessing administrative employee & department management (HTTP 403)")
    @WithMockUser(username = "rahul@hrgenius.com", roles = "EMPLOYEE")
    void employeeCannotAccessAdminPages() throws Exception {
        mockMvc.perform(get("/employees")).andExpect(status().isForbidden());
        mockMvc.perform(get("/departments")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Employee can view own payslip but cannot access another employee's payslip (HTTP 403)")
    @WithMockUser(username = "rahul@hrgenius.com", roles = "EMPLOYEE")
    void employeeCannotAccessPeerPayslip() throws Exception {
        // Rahul owns payslip 1 -> should succeed with HTTP 200
        mockMvc.perform(get("/payroll/payslip/1")).andExpect(status().isOk());

        // Priya owns payslip 2 -> Rahul should be rejected with HTTP 403
        mockMvc.perform(get("/payroll/payslip/2")).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Verify payroll calculation formula: 20% allowances, 12% deductions, net = 108% basic")
    void verifyPayrollFormulaCalculation() {
        Employee employee = employeeRepository.findByEmail("rahul@hrgenius.com").orElseThrow();
        BigDecimal basic = employee.getBasicSalary(); // 50,000.00

        BigDecimal expectedAllowances = basic.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedDeductions = basic.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal expectedNet = basic.add(expectedAllowances).subtract(expectedDeductions).setScale(2, RoundingMode.HALF_UP);

        assertEquals(new BigDecimal("10000.00"), expectedAllowances);
        assertEquals(new BigDecimal("6000.00"), expectedDeductions);
        assertEquals(new BigDecimal("54000.00"), expectedNet);

        // Verify seeded payroll matches exact mathematical formula
        List<Payroll> list = payrollRepository.findByEmployeeOrderByYearDescMonthDesc(employee);
        assertFalse(list.isEmpty());
        Payroll sample = list.get(0);

        assertEquals(expectedAllowances, sample.getAllowances());
        assertEquals(expectedDeductions, sample.getDeductions());
        assertEquals(expectedNet, sample.getNetSalary());
    }

    @Test
    @DisplayName("Deactivated employee accounts have UserDetails.isEnabled() set to false")
    void deactivatedEmployeeCannotAuthenticate() {
        Employee sneha = employeeRepository.findByEmail("sneha@hrgenius.com").orElseThrow();
        EmployeeStatus originalStatus = sneha.getStatus();

        try {
            // Active state verification
            UserDetails activeUserDetails = userDetailsService.loadUserByUsername("sneha@hrgenius.com");
            assertTrue(activeUserDetails.isEnabled());

            // Deactivate employee
            sneha.setStatus(EmployeeStatus.INACTIVE);
            employeeRepository.save(sneha);

            // Inactive state verification
            UserDetails inactiveUserDetails = userDetailsService.loadUserByUsername("sneha@hrgenius.com");
            assertFalse(inactiveUserDetails.isEnabled());
        } finally {
            // Restore original state
            sneha.setStatus(originalStatus);
            employeeRepository.save(sneha);
        }
    }
}
