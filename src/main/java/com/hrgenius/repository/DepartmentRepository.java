package com.hrgenius.repository;

import com.hrgenius.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Data access operations for Department entities.
// Provides CRUD capabilities and lookup by department name.
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);
}
