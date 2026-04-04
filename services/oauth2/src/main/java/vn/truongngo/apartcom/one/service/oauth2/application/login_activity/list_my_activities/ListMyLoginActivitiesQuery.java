package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities;

/**
 * UC-012: Lấy lịch sử đăng nhập của chính mình.
 *
 * @param userId  sub claim từ Access Token
 * @param page    trang (0-based), default 0
 * @param size    số item mỗi trang, default 20, max 50
 */
public record ListMyLoginActivitiesQuery(String userId, int page, int size) {}
