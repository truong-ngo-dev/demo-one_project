package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.query.iam_dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview.IamOverviewData;
import vn.truongngo.apartcom.one.service.oauth2.application.iam_dashboard.overview.IamOverviewQueryPort;

/**
 * UC-011: Tổng hợp KPI counts từ các bảng local.
 * Bypass domain layer (CQRS read side).
 *
 * Lưu ý: totalUsers dùng COUNT(DISTINCT user_id) FROM devices vì oauth2-service
 * không lưu bảng users riêng. Giá trị này phản ánh số user đã đăng nhập thành công
 * ít nhất một lần (có device đã đăng ký), không phải tổng user trong Admin Service.
 */
@Service
@RequiredArgsConstructor
public class IamOverviewQueryService implements IamOverviewQueryPort {

    private static final String COUNT_USERS_SQL =
            "SELECT COUNT(DISTINCT user_id) FROM devices";

    private static final String COUNT_DEVICES_SQL =
            "SELECT COUNT(*) FROM devices";

    private static final String COUNT_ACTIVE_SESSIONS_SQL =
            "SELECT COUNT(*) FROM oauth_sessions WHERE status = 'ACTIVE'";

    private static final String COUNT_FAILED_LOGINS_TODAY_SQL =
            "SELECT COUNT(*) FROM login_activities WHERE result != 'SUCCESS' AND created_at >= CURDATE()";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public IamOverviewData query() {
        long totalUsers = count(COUNT_USERS_SQL);
        long totalDevices = count(COUNT_DEVICES_SQL);
        long activeSessions = count(COUNT_ACTIVE_SESSIONS_SQL);
        long failedLoginsToday = count(COUNT_FAILED_LOGINS_TODAY_SQL);

        return new IamOverviewData(totalUsers, totalDevices, activeSessions, failedLoginsToday);
    }

    private long count(String sql) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result != null ? result : 0L;
    }
}
