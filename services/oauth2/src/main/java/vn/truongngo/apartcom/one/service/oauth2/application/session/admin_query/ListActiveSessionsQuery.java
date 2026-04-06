package vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query;

/**
 * UC-013: Admin xem danh sách tất cả phiên đang ACTIVE toàn hệ thống.
 * Không có filter — luôn trả về toàn bộ ACTIVE sessions.
 */
public record ListActiveSessionsQuery() {}
