package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.query.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.LoginActivityPage;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.LoginActivityQueryPort;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities.LoginActivityView;

import java.sql.Timestamp;
import java.util.List;

/**
 * Read-side query — left join login_activities với devices theo compositeHash + userId.
 * Bypass domain layer (CQRS read side).
 */
@Service
@RequiredArgsConstructor
public class LoginActivityQueryService implements LoginActivityQueryPort {

    private static final String FIND_SQL = """
            SELECT la.result,
                   la.ip_address,
                   la.user_agent,
                   la.provider,
                   la.created_at,
                   la.device_id,
                   d.device_name
            FROM login_activities la
                     LEFT JOIN devices d ON d.id = la.device_id
            WHERE la.user_id = ?
            ORDER BY la.created_at DESC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM login_activities WHERE user_id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public LoginActivityPage findByUserId(String userId, int page, int size) {
        int offset = page * size;

        Long total = jdbcTemplate.queryForObject(COUNT_SQL, Long.class, userId);
        long totalElements = total != null ? total : 0L;
        int totalPages = (int) Math.ceil((double) totalElements / size);

        List<LoginActivityView> content = jdbcTemplate.query(
                FIND_SQL,
                (rs, rowNum) -> new LoginActivityView(
                        rs.getString("result"),
                        rs.getString("ip_address"),
                        rs.getString("user_agent"),
                        rs.getString("provider"),
                        rs.getTimestamp("created_at") != null
                                ? ((Timestamp) rs.getTimestamp("created_at")).toInstant()
                                : null,
                        rs.getString("device_id"),
                        rs.getString("device_name")
                ),
                userId, size, offset);

        return new LoginActivityPage(content, page, size, totalElements, totalPages);
    }
}
