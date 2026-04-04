package vn.truongngo.apartcom.one.service.oauth2.infrastructure.adapter.service.authorization;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.service.oauth2.application.session.revoke.AuthorizationLookupPort;
import vn.truongngo.apartcom.one.service.oauth2.application.session.revoke.AuthorizationTokenType;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpringAuthorizationLookupAdapter implements AuthorizationLookupPort {

    private final OAuth2AuthorizationService oauth2AuthorizationService;

    @Override
    public Optional<String> findAuthorizationId(String tokenValue, AuthorizationTokenType tokenType) {
        OAuth2Authorization authorization = oauth2AuthorizationService.findByToken(
                tokenValue,
                new OAuth2TokenType(tokenType.getValue())
        );
        return Optional.ofNullable(authorization).map(OAuth2Authorization::getId);
    }
}
