package com.hrgenius.model;

// Tracks the approval lifecycle of a leave request.
// PENDING = awaiting admin action, APPROVED/REJECTED = final states.
public enum LeaveStatus {
    PENDING,
    APPROVED,
    REJECTED
}
