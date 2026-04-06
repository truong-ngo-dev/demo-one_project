package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.admin_query;

/**
 * Filter parameters cho UC-012 Global Login Activity query.
 *
 * @param ip       filter theo IP address (partial match)
 * @param result   filter theo login result (exact match — SUCCESS, FAILED_WRONG_PASSWORD, v.v.)
 * @param username filter theo username (partial match)
 */
public record AdminLoginActivityFilter(
        String ip,
        String result,
        String username
) {}
