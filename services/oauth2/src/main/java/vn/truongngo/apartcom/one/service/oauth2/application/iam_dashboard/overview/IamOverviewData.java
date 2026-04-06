package vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview;

/**
 * UC-011 response payload — các chỉ số KPI của IAM Dashboard.
 *
 * @param totalUsers        Số user phân biệt có ít nhất 1 device đã đăng ký — proxy local,
 *                          vì oauth2-service không lưu bảng users riêng.
 * @param totalDevices      Tổng số thiết bị đã ghi nhận trong hệ thống.
 * @param activeSessions    Số phiên đang ở trạng thái ACTIVE.
 * @param failedLoginsToday Số lượt đăng nhập thất bại trong ngày hiện tại (từ 00:00:00).
 */
public record IamOverviewData(
        long totalUsers,
        long totalDevices,
        long activeSessions,
        long failedLoginsToday
) {}
