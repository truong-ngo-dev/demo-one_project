package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities;

/**
 * Read-side port — query login activities với device name join.
 * Bypass domain layer (CQRS read side).
 */
public interface LoginActivityQueryPort {
    LoginActivityPage findByUserId(String userId, int page, int size);
}
