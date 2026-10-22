package com.hrgenius.repository;

import com.hrgenius.model.Department;
import com.hrgenius.model.Employee;
import com.hrgenius.model.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Data access operations for Employee records.
// Includes search by name/email, department filtering, and status-based counting.
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByStatus(EmployeeStatus status);

    Optional<Employee> findByEmail(String email);

    long countByDepartment(Department department);

    long countByStatus(EmployeeStatus status);

    List<Employee> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);

    List<Employee> findByDepartment(Department department);

    @Query("SELECT e FROM Employee e WHERE e.department.id = :deptId AND " +
           "(LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Employee> searchByDepartmentAndKeyword(@Param("deptId") Long deptId, @Param("keyword") String keyword);

    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Employee> searchByKeyword(@Param("keyword") String keyword);

    List<Employee> findByDepartmentId(Long departmentId);
}
