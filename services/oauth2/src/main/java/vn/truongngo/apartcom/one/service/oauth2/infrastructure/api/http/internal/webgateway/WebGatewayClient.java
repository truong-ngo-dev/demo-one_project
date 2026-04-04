package vn.truongngo.apartcom.one.service.oauth2.infrastructure.api.http.internal.webgateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP client cho Web Gateway — gom tất cả outbound call sang web-gateway service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebGatewayClient {

    private final RestClient gatewayRestClient;

    /**
     * UC-008: Notify Web Gateway xóa Redis session khi session bị revoke từ xa.
     *
     * @param sid sessionId (aggregateId của Oauth2Session) cần invalidate
     */
    public void notifyRevocation(String sid) {
        log.debug("[WebGatewayClient] Notifying Web Gateway of session revocation — sid={}", sid);
        gatewayRestClient.post()
                .uri("/webgw/internal/sessions/revoke")
                .body(new RevokeSessionRequest(sid))
                .retrieve()
                .toBodilessEntity();
    }

    private record RevokeSessionRequest(String sid) {}
}
