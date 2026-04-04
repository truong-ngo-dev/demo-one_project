package vn.truongngo.apartcom.one.service.webgateway.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * UC-001: Khởi tạo OAuth2 Authorization Code Flow từ Angular.
 * UC-006: Kiểm tra trạng thái xác thực của session hiện tại.
 * Angular gọi GET /webgw/auth/login → redirect đến Spring Security OAuth2 authorization endpoint.
 */
@RestController
@RequestMapping("/webgw/auth")
public class AuthController {

    @GetMapping("/login")
    public Mono<Void> login(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getResponse().getHeaders().setLocation(URI.create("/oauth2/authorization/web-gateway"));
        return exchange.getResponse().setComplete();
    }

    /**
     * UC-006: Kiểm tra session hợp lệ mà không trigger redirect.
     * 200 + { sub, requiresProfileCompletion } → session hợp lệ, 401 → chưa xác thực.
     */
    @GetMapping("/session")
    public Mono<ResponseEntity<SessionResponse>> session(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(p -> {
                    boolean requiresProfileCompletion = false;
                    if (p instanceof OAuth2AuthenticationToken token) {
                        OAuth2User user = token.getPrincipal();
                        Object flag = user.getAttribute("requires_profile_completion");
                        requiresProfileCompletion = Boolean.TRUE.equals(flag);
                    }
                    return ResponseEntity.ok(new SessionResponse(p.getName(), requiresProfileCompletion));
                })
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).<SessionResponse>build());
    }

    record SessionResponse(String sub, boolean requiresProfileCompletion) {}
}
