package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.query.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.AdminLoginActivityFilter;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.AdminLoginActivityPage;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.AdminLoginActivityQueryPort;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.AdminLoginActivityView;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.UserLoginActivityPage;
import vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query.UserLoginActivityView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * UC-012: Global login activities cho Admin — dynamic filter, phân trang.
 * UC-015: User-specific login activities — filter theo userId, phân trang.
 * LEFT JOIN với devices để enrich tên thiết bị.
 * Bypass domain layer (CQRS read side).
 */
@Service
@RequiredArgsConstructor
public class AdminLoginActivityQueryService implements AdminLoginActivityQueryPort {

    private static final String BASE_SELECT = """
            SELECT la.id,
                   la.user_id,
                   la.username,
                   la.result,
                   la.ip_address,
                   la.user_agent,
                   la.device_id,
                   d.device_name,
                   la.provider,
                   la.created_at
            FROM login_activities la
                     LEFT JOIN devices d ON d.id = la.device_id
            """;

    private static final String BASE_COUNT = "SELECT COUNT(*) FROM login_activities la ";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public AdminLoginActivityPage findAll(AdminLoginActivityFilter filter, int page, int size) {
        List<Object> params = new ArrayList<>();
        String whereClause = buildWhereClause(filter, params);

        int offset = page * size;

        Long total = jdbcTemplate.queryForObject(
                BASE_COUNT + whereClause,
                Long.class,
                params.toArray());
        long totalElements = total != null ? total : 0L;

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(size);
        queryParams.add(offset);

        List<AdminLoginActivityView> data = jdbcTemplate.query(
                BASE_SELECT + whereClause + "ORDER BY la.created_at DESC LIMIT ? OFFSET ?",
                (rs, rowNum) -> new AdminLoginActivityView(
                        rs.getString("id"),
                        rs.getString("user_id"),
                        rs.getString("username"),
                        rs.getString("result"),
                        rs.getString("ip_address"),
                        rs.getString("user_agent"),
                        rs.getString("device_id"),
                        rs.getString("device_name"),
                        rs.getString("provider"),
                        rs.getTimestamp("created_at") != null
                                ? ((Timestamp) rs.getTimestamp("created_at")).toInstant()
                                : null
                ),
                queryParams.toArray());

        return new AdminLoginActivityPage(
                data,
                new AdminLoginActivityPage.AdminLoginActivityMeta(page, size, totalElements));
    }

    private static final String USER_SELECT = """
            SELECT la.result,
                   la.ip_address,
                   d.device_name,
                   la.provider,
                   la.created_at
            FROM login_activities la
                     LEFT JOIN devices d ON d.id = la.device_id
            WHERE la.user_id = ?
            ORDER BY la.created_at DESC
            LIMIT ? OFFSET ?
            """;

    private static final String USER_COUNT = "SELECT COUNT(*) FROM login_activities WHERE user_id = ?";

    @Override
    public UserLoginActivityPage findByUserId(String userId, int page, int size) {
        int offset = page * size;

        Long total = jdbcTemplate.queryForObject(USER_COUNT, Long.class, userId);
        long totalElements = total != null ? total : 0L;

        List<UserLoginActivityView> data = jdbcTemplate.query(
                USER_SELECT,
                (rs, rowNum) -> new UserLoginActivityView(
                        rs.getString("result"),
                        rs.getString("ip_address"),
                        rs.getString("device_name"),
                        rs.getString("provider"),
                        rs.getTimestamp("created_at") != null
                                ? ((Timestamp) rs.getTimestamp("created_at")).toInstant()
                                : null
                ),
                userId, size, offset);

        return new UserLoginActivityPage(
                data,
                new UserLoginActivityPage.UserLoginActivityMeta(page, size, totalElements));
    }

    private String buildWhereClause(AdminLoginActivityFilter filter, List<Object> params) {
        List<String> conditions = new ArrayList<>();

        if (filter.ip() != null && !filter.ip().isBlank()) {
            conditions.add("la.ip_address LIKE ?");
            params.add("%" + filter.ip().trim() + "%");
        }
        if (filter.result() != null && !filter.result().isBlank()) {
            conditions.add("la.result = ?");
            params.add(filter.result().trim().toUpperCase());
        }
        if (filter.username() != null && !filter.username().isBlank()) {
            conditions.add("la.username LIKE ?");
            params.add("%" + filter.username().trim() + "%");
        }

        if (conditions.isEmpty()) {
            return "";
        }
        return "WHERE " + String.join(" AND ", conditions) + " ";
    }
}
