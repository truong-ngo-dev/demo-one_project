package vn.truongngo.apartcom.one.service.oauth2.application.login_activity.list_my_activities;

import java.util.List;

/**
 * UC-012 paginated response.
 */
public record LoginActivityPage(
        List<LoginActivityView> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {}
