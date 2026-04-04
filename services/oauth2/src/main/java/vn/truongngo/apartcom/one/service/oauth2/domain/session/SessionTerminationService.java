package vn.truongngo.apartcom.one.service.oauth2.domain.session;

/**
 * Domain port — hủy Authorization Record tương ứng với session.
 * Adapter dùng Spring AS OAuth2AuthorizationService (không dùng raw JDBC).
 */
public interface SessionTerminationService {

    /** Lookup session by authorizationId rồi revoke — dùng cho logout flow. */
    Oauth2Session terminateSession(String authorizationId);

    /** Session đã có sẵn (đã load để check ownership) — tránh double-load. Dùng cho remote revoke. */
    void terminateSession(Oauth2Session session);
}
