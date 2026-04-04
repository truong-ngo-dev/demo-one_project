package vn.truongngo.apartcom.one.service.oauth2.application.session.event;

/**
 * Port (Application layer) — chịu trách nhiệm thông báo cho các hệ thống bên ngoài
 * khi một session bị thu hồi.
 */
public interface RevocationNotifier {
    /**
     * @param sid authorizationId (sid claim trong JWT)
     */
    void notify(String sid);
}
