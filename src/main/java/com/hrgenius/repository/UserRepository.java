package com.hrgenius.repository;

import com.hrgenius.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// Data access operations for User credentials and account management.
// Used for authentication lookups and initial system seeding checks.
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    long count();
}
