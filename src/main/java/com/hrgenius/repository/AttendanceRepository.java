package com.hrgenius.repository;

import com.hrgenius.model.Attendance;
import com.hrgenius.model.AttendanceStatus;
import com.hrgenius.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Data access operations for employee attendance logs.
// Enables checking daily status, generating date-range histories, and daily stats.
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);

    List<Attendance> findByEmployeeAndDateBetweenOrderByDateDesc(Employee employee, LocalDate from, LocalDate to);

    List<Attendance> findByDate(LocalDate date);

    long countByDateAndStatus(LocalDate date, AttendanceStatus status);
}
