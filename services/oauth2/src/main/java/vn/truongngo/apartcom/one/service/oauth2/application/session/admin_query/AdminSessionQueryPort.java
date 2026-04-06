package vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query;

import java.util.List;

/**
 * Read-side port — query active sessions toàn hệ thống cho Admin.
 * Bypass domain layer (CQRS read side).
 */
public interface AdminSessionQueryPort {
    List<ActiveSessionView> findAllActive();
}
