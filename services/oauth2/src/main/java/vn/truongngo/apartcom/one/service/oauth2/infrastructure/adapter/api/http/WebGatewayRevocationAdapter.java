package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.api.http;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.oauth2.application.session.event.RevocationNotifier;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.api.http.internal.webgateway.WebGatewayClient;

@Component
@RequiredArgsConstructor
public class WebGatewayRevocationAdapter implements RevocationNotifier {

    private final WebGatewayClient webGatewayClient;

    @Override
    public void notify(String sid) {
        webGatewayClient.notifyRevocation(sid);
    }
}
