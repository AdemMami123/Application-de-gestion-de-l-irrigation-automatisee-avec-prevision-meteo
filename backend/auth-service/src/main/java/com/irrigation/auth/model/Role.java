package com.irrigation.auth.model;

/**
 * Enumeration of user roles in the irrigation system
 */
public enum Role {
    ROLE_ADMIN,      // System administrator - full access
    ROLE_MANAGER,    // Farm manager - manage farms, programs, view all data
    ROLE_OPERATOR,   // Field operator - execute programs, view schedules
    ROLE_VIEWER      // Read-only access - view dashboards and reports
}
