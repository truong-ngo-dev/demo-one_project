package vn.truongngo.apartcom.one.service.webgateway.infrastructure.configuration.security;

import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Logout success handler for BFF pattern.
 * Initiates OIDC RP-Initiated Logout and returns 202 Accepted + Location header
 * instead of 302, so the Angular SPA can navigate to the logout URL manually.
 */
public class WebGatewayLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    private final WebGatewayOAuth2RedirectStrategy redirectStrategy =
            new WebGatewayOAuth2RedirectStrategy(HttpStatus.ACCEPTED);

    private final RedirectServerLogoutSuccessHandler fallbackHandler =
            new RedirectServerLogoutSuccessHandler();

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;
    private final ReactiveStringRedisTemplate redisTemplate;

    private String postLogoutRedirectUri;
    private URI endSessionUri;

    public WebGatewayLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository,
                                          ReactiveStringRedisTemplate redisTemplate) {
        Assert.notNull(clientRegistrationRepository, "clientRegistrationRepository cannot be null");
        Assert.notNull(redisTemplate, "redisTemplate cannot be null");
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.redisTemplate = redisTemplate;
    }

    @Override
    @SuppressWarnings("all")
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        OidcUser user = (OidcUser) authentication.getPrincipal();
        String authroizationId = user.getUserInfo().getClaimAsString("sid");
        return Mono.just(authentication)
                .filter(OAuth2AuthenticationToken.class::isInstance)
                .filter(token -> token.getPrincipal() instanceof OidcUser)
                .map(OAuth2AuthenticationToken.class::cast)
                .map(OAuth2AuthenticationToken::getAuthorizedClientRegistrationId)
                .flatMap(this.clientRegistrationRepository::findByRegistrationId)
                .flatMap(clientRegistration -> {
                    URI endSessionEndpoint = endSessionEndpoint(clientRegistration);
                    if (endSessionEndpoint == null) return Mono.empty();
                    String idToken = idToken(authentication);
                    String postLogoutUri = postLogoutRedirectUri(exchange.getExchange().getRequest(), clientRegistration);
                    return Mono.just(endpointUri(endSessionEndpoint, idToken, postLogoutUri));
                })
                .switchIfEmpty(this.fallbackHandler.onLogoutSuccess(exchange, authentication).then(Mono.empty()))
                .flatMap(endpointUri -> cleanUpSessionMappings(authroizationId)
                        .then(this.redirectStrategy.sendRedirect(exchange.getExchange(), URI.create(endpointUri))));
    }

    public void setPostLogoutRedirectUri(String postLogoutRedirectUri) {
        Assert.notNull(postLogoutRedirectUri, "postLogoutRedirectUri cannot be null");
        this.postLogoutRedirectUri = postLogoutRedirectUri;
    }

    public void setEndSessionUri(String endSessionUri) {
        Assert.hasText(endSessionUri, "endSessionUri must not be empty");
        this.endSessionUri = URI.create(endSessionUri);
    }

    public void setLogoutSuccessUrl(URI logoutSuccessUrl) {
        Assert.notNull(logoutSuccessUrl, "logoutSuccessUrl cannot be null");
        this.fallbackHandler.setLogoutSuccessUrl(logoutSuccessUrl);
    }

    private Mono<Void> cleanUpSessionMappings(String authorizationId) {
        return Mono.just(authorizationId)
                .flatMap(aid -> {
                    String oauthKey = SessionMappingAuthenticationSuccessHandler.WEBGW_OAUTH_KEY_PREFIX + aid;
                    return redisTemplate.opsForValue().get(oauthKey)
                            .flatMap(sid -> {
                                String sessionKey = SessionMappingAuthenticationSuccessHandler.WEBGW_SESSION_KEY_PREFIX + sid;
                                return redisTemplate.delete(oauthKey, sessionKey).then();
                            })
                            .onErrorResume(e -> Mono.empty());
                })
                .onErrorResume(e -> Mono.empty());
    }

    private URI endSessionEndpoint(ClientRegistration clientRegistration) {
        // Prefer explicitly configured URI (set when issuer-uri based OIDC discovery is not used)
        if (this.endSessionUri != null) return this.endSessionUri;
        // Fallback: read from OIDC discovery metadata (populated only when issuer-uri is configured)
        if (clientRegistration == null) return null;
        Object endpoint = clientRegistration.getProviderDetails()
                .getConfigurationMetadata()
                .get("end_session_endpoint");
        return endpoint != null ? URI.create(endpoint.toString()) : null;
    }

    private String endpointUri(URI endSessionEndpoint, String idToken, String postLogoutRedirectUri) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(endSessionEndpoint);
        builder.queryParam("id_token_hint", idToken);
        if (postLogoutRedirectUri != null) {
            builder.queryParam("post_logout_redirect_uri", postLogoutRedirectUri);
        }
        return builder.encode(StandardCharsets.UTF_8).build().toUriString();
    }

    private String idToken(Authentication authentication) {
        return ((OidcUser) Objects.requireNonNull(authentication.getPrincipal())).getIdToken().getTokenValue();
    }

    private String postLogoutRedirectUri(ServerHttpRequest request, ClientRegistration clientRegistration) {
        if (this.postLogoutRedirectUri == null) return null;

        UriComponents uriComponents = UriComponentsBuilder.fromUri(request.getURI())
                .replacePath(request.getPath().contextPath().value())
                .replaceQuery(null)
                .fragment(null)
                .build();

        Map<String, String> uriVariables = new HashMap<>();
        String scheme = uriComponents.getScheme();
        uriVariables.put("baseScheme", scheme != null ? scheme : "");
        uriVariables.put("baseUrl", uriComponents.toUriString());

        String host = uriComponents.getHost();
        uriVariables.put("baseHost", host != null ? host : "");

        String path = uriComponents.getPath();
        uriVariables.put("basePath", path != null ? path : "");

        int port = uriComponents.getPort();
        uriVariables.put("basePort", port == -1 ? "" : ":" + port);

        uriVariables.put("registrationId", clientRegistration.getRegistrationId());

        return UriComponentsBuilder.fromUriString(this.postLogoutRedirectUri)
                .buildAndExpand(uriVariables)
                .toUriString();
    }
}
