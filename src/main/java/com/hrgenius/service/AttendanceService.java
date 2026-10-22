package com.hrgenius.service;

import com.hrgenius.model.Attendance;
import com.hrgenius.model.AttendanceStatus;
import com.hrgenius.model.Employee;
import com.hrgenius.repository.AttendanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// Business service tracking daily employee check-ins, check-outs, and historical attendance.
// Enforces single daily check-ins and prevents clocking out prior to clocking in.
@Service
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = attendanceRepository;
    }

    public Optional<Attendance> getTodayAttendance(Employee employee) {
        return attendanceRepository.findByEmployeeAndDate(employee, LocalDate.now());
    }

    public List<Attendance> getHistory(Employee employee, int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days);
        return attendanceRepository.findByEmployeeAndDateBetweenOrderByDateDesc(employee, start, end);
    }

    public List<Attendance> getByDate(LocalDate date) {
        return attendanceRepository.findByDate(date != null ? date : LocalDate.now());
    }

    public long getPresentCountToday() {
        return attendanceRepository.countByDateAndStatus(LocalDate.now(), AttendanceStatus.PRESENT);
    }

    @Transactional
    public Attendance checkIn(Employee employee) {
        LocalDate today = LocalDate.now();
        Optional<Attendance> existing = attendanceRepository.findByEmployeeAndDate(employee, today);
        if (existing.isPresent()) {
            throw new IllegalStateException("You have already checked in for today.");
        }

        Attendance attendance = new Attendance(
                employee,
                today,
                LocalTime.now(),
                null,
                AttendanceStatus.PRESENT
        );
        return attendanceRepository.save(attendance);
    }

    @Transactional
    public Attendance checkOut(Employee employee) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByEmployeeAndDate(employee, today)
                .orElseThrow(() -> new IllegalStateException("You must check in before checking out."));

        if (attendance.getCheckOut() != null) {
            throw new IllegalStateException("You have already checked out for today.");
        }

        attendance.setCheckOut(LocalTime.now());
        return attendanceRepository.save(attendance);
    }
}
