package com.hrgenius.repository;

import com.hrgenius.model.Employee;
import com.hrgenius.model.LeaveRequest;
import com.hrgenius.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

// Data access operations for employee leave applications.
// Provides queries for approval queues, employee history, and annual balances.
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeOrderByAppliedOnDesc(Employee employee);

    List<LeaveRequest> findByStatusOrderByAppliedOnDesc(LeaveStatus status);

    List<LeaveRequest> findAllByOrderByAppliedOnDesc();

    List<LeaveRequest> findTop5ByOrderByAppliedOnDesc();

    long countByStatus(LeaveStatus status);

    List<LeaveRequest> findByEmployeeAndStatus(Employee employee, LeaveStatus status);

    List<LeaveRequest> findByEmployeeAndStatusAndFromDateBetween(
            Employee employee, LeaveStatus status, LocalDate start, LocalDate end);
}
