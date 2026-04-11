package vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuditHelper {

    private AuditHelper() {}

    /**
     * Returns the current admin's identifier (userId set by JWT filter), or "system" if unavailable.
     */
    public static String currentPerformedBy() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
                return auth.getName();
            }
        } catch (Exception ignored) {}
        return "system";
    }
}
