package com.hrgenius.model;

// Tracks whether an employee is currently active or has been deactivated.
// INACTIVE employees cannot log in (enforced by CustomUserDetailsService).
public enum EmployeeStatus {
    ACTIVE,
    INACTIVE
}
