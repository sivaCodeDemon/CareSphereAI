package com.caresphere.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Reads the authenticated principal's email from the security context. Used to attribute
 * auditable actions (e.g. who created a patient record) to the caller.
 */
@Component
public class AuthenticatedUser {

    /**
     * @return the authenticated user's email, or {@code null} if no authenticated user is present
     *         (e.g. in unauthenticated or test contexts).
     */
    public String getUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return auth.getName();
    }
}
