package com.vp.voicepocket.domain.user.entity.enums;

public enum UserRole {
    ROLE_USER, ROLE_ADMIN;

    public static boolean isUserRole(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
