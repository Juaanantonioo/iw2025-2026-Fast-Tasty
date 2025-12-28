package com.fastfoodmanager.views;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CashUiUtil {
    private CashUiUtil() {}

    public static String currentUsernameOr(String fallback) {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) return fallback;
        String name = a.getName();
        if (name == null || "anonymousUser".equals(name)) return fallback;
        return name;
    }
}
