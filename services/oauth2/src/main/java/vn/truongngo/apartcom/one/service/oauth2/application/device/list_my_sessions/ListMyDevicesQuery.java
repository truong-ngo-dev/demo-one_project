package vn.truongngo.apartcom.one.service.oauth2.application.device.list_my_sessions;

/**
 * UC-007: List My Devices & Sessions.
 *
 * @param userId         sub claim từ Access Token
 * @param currentSid     sid claim từ Access Token — authorizationId của session hiện tại
 */
public record ListMyDevicesQuery(String userId, String currentSid) {}
