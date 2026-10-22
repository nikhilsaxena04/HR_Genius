package com.hrgenius.service;

import com.hrgenius.model.Employee;
import com.hrgenius.model.LeaveRequest;
import com.hrgenius.model.LeaveStatus;
import com.hrgenius.repository.LeaveRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

// Business service processing leave applications, balance checks, and administrative approvals.
// Enforces a 12-day annual quota and disallows retrospective leave submissions.
@Service
public class LeaveService {

    public static final int ANNUAL_LEAVE_QUOTA = 12;

    private final LeaveRequestRepository leaveRequestRepository;

    public LeaveService(LeaveRequestRepository leaveRequestRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
    }

    public List<LeaveRequest> getByEmployee(Employee employee) {
        return leaveRequestRepository.findByEmployeeOrderByAppliedOnDesc(employee);
    }

    public List<LeaveRequest> getAll() {
        return leaveRequestRepository.findAllByOrderByAppliedOnDesc();
    }

    public List<LeaveRequest> getByStatus(LeaveStatus status) {
        return leaveRequestRepository.findByStatusOrderByAppliedOnDesc(status);
    }

    public List<LeaveRequest> getRecent5() {
        return leaveRequestRepository.findTop5ByOrderByAppliedOnDesc();
    }

    public long countPending() {
        return leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
    }

    public int getUsedLeaveDays(Employee employee, int year) {
        LocalDate start = LocalDate.of(year, 1, 1);
        LocalDate end = LocalDate.of(year, 12, 31);
        List<LeaveRequest> approvedList = leaveRequestRepository
                .findByEmployeeAndStatusAndFromDateBetween(employee, LeaveStatus.APPROVED, start, end);

        int totalDays = 0;
        for (LeaveRequest req : approvedList) {
            long days = ChronoUnit.DAYS.between(req.getFromDate(), req.getToDate()) + 1;
            totalDays += (int) days;
        }
        return totalDays;
    }

    public int getRemainingLeaveDays(Employee employee, int year) {
        int used = getUsedLeaveDays(employee, year);
        return Math.max(0, ANNUAL_LEAVE_QUOTA - used);
    }

    @Transactional
    public LeaveRequest apply(LeaveRequest request, Employee employee) {
        if (request.getFromDate() == null || request.getToDate() == null) {
            throw new IllegalArgumentException("Both start and end dates are required.");
        }

        LocalDate today = LocalDate.now();
        // Validation rule: Reject past dates
        if (request.getFromDate().isBefore(today)) {
            throw new IllegalArgumentException("Leave start date cannot be in the past.");
        }

        // Validation rule: End date >= Start date
        if (request.getToDate().isBefore(request.getFromDate())) {
            throw new IllegalArgumentException("Leave end date cannot be earlier than start date.");
        }

        // Validation rule: 12 annual leave quota check
        int year = request.getFromDate().getYear();
        int usedDays = getUsedLeaveDays(employee, year);
        long requestedDays = ChronoUnit.DAYS.between(request.getFromDate(), request.getToDate()) + 1;

        if (usedDays + requestedDays > ANNUAL_LEAVE_QUOTA) {
            int remaining = Math.max(0, ANNUAL_LEAVE_QUOTA - usedDays);
            throw new IllegalStateException("Insufficient leave balance. You requested " + requestedDays +
                    " day(s), but only have " + remaining + " day(s) remaining for " + year + ".");
        }

        request.setEmployee(employee);
        request.setAppliedOn(today);
        request.setStatus(LeaveStatus.PENDING);
        request.setAdminRemark(null);

        return leaveRequestRepository.save(request);
    }

    @Transactional
    public void approve(Long id, String adminRemark) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));
        request.setStatus(LeaveStatus.APPROVED);
        request.setAdminRemark(adminRemark);
        leaveRequestRepository.save(request);
    }

    @Transactional
    public void reject(Long id, String adminRemark) {
        LeaveRequest request = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Leave request not found with id: " + id));
        request.setStatus(LeaveStatus.REJECTED);
        request.setAdminRemark(adminRemark);
        leaveRequestRepository.save(request);
    }
}
