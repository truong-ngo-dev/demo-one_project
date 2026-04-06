package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.query.session;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query.ActiveSessionView;
import vn.truongngo.apartcom.one.service.oauth2.application.session.admin_query.AdminSessionQueryPort;

import java.sql.Timestamp;
import java.util.List;

/**
 * UC-013: Active sessions toàn hệ thống cho Admin.
 * LEFT JOIN devices để lấy tên thiết bị.
 * Username lấy từ login_activities gần nhất (SUCCESS) của cùng userId — correlated subquery.
 * Bypass domain layer (CQRS read side).
 */
@Service
@RequiredArgsConstructor
public class AdminSessionQueryService implements AdminSessionQueryPort {

    private static final String FIND_ACTIVE_SQL = """
            SELECT s.id            AS session_id,
                   s.user_id,
                   (SELECT la.username
                    FROM login_activities la
                    WHERE la.user_id = s.user_id
                      AND la.result = 'SUCCESS'
                    ORDER BY la.created_at DESC
                    LIMIT 1)      AS username,
                   d.device_name,
                   s.ip_address,
                   s.created_at
            FROM oauth_sessions s
                     LEFT JOIN devices d ON d.id = s.device_id
            WHERE s.status = 'ACTIVE'
            ORDER BY s.created_at DESC
            """;

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<ActiveSessionView> findAllActive() {
        return jdbcTemplate.query(
                FIND_ACTIVE_SQL,
                (rs, rowNum) -> new ActiveSessionView(
                        rs.getString("session_id"),
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("device_name"),
                        rs.getString("ip_address"),
                        rs.getTimestamp("created_at") != null
                                ? ((Timestamp) rs.getTimestamp("created_at")).toInstant()
                                : null
                ));
    }
}
