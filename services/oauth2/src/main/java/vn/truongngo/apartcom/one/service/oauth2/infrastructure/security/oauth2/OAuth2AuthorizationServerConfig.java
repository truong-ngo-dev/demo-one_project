package vn.truongngo.apartcom.one.service.oauth2.infrastructure.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.*;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcLogoutAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.server.authorization.oidc.authentication.OidcUserInfoAuthenticationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;
import vn.truongngo.apartcom.one.service.oauth2.application.auth.complete_login.CompleteLogin;
import vn.truongngo.apartcom.one.service.oauth2.application.session.revoke.RevokeSession;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.security.handler.AuthorizationRevokingLogoutSuccessHandler;
import vn.truongngo.apartcom.one.service.oauth2.infrastructure.security.model.DeviceAwareWebAuthenticationDetails;

@Configuration
@RequiredArgsConstructor
public class OAuth2AuthorizationServerConfig {

    private final ApplicationContext context;

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
                .with(authorizationServerConfigurer, configurer -> configurer
                        .oidc(oidcConfigurer -> oidcConfigurer
                                .userInfoEndpoint(userInfo -> userInfo.userInfoMapper(this::mapUserInfo))
                                .logoutEndpoint(logoutEndpointConfigurer -> logoutEndpointConfigurer.logoutResponseHandler(logoutSuccessHandler()))))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .anonymous(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"),
                        new MediaTypeRequestMatcher(MediaType.TEXT_HTML)));

        return http.build();
    }

    @Bean
    public OAuth2AuthorizationService auth2AuthorizationService(
            JdbcOperations jdbcOperations,
            RegisteredClientRepository repository,
            CompleteLogin completeLogin) {

        JdbcOAuth2AuthorizationService jdbcService = new JdbcOAuth2AuthorizationService(jdbcOperations, repository);
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
        JsonMapper jsonMapper = JsonMapper.builder()
                .findAndAddModules()
                .addModules(SecurityJacksonModules.getModules(classLoader))
                .addMixIn(DeviceAwareWebAuthenticationDetails.class, DeviceAwareWebAuthenticationDetailsMixin.class)
                .build();
        JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper rowMapper = new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(repository, jsonMapper);
        jdbcService.setAuthorizationRowMapper(rowMapper);
        return new AuditingOAuth2AuthorizationService(jdbcService, completeLogin);
    }

    @Bean
    public OAuth2AuthorizationConsentService oAuth2AuthorizationConsentService(JdbcOperations jdbcOperations, RegisteredClientRepository repository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, repository);
    }

    /**
     * Expose all access token JWT claims (including custom ones like requires_profile_completion)
     * through the OIDC UserInfo endpoint.
     */
    private OidcUserInfo mapUserInfo(OidcUserInfoAuthenticationContext context) {
        OAuth2Authorization authorization = context.getAuthorization();
        OAuth2Authorization.Token<OAuth2AccessToken> accessToken = authorization.getToken(OAuth2AccessToken.class);
        if (accessToken == null || accessToken.getClaims() == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_TOKEN);
        }
        return new OidcUserInfo(accessToken.getClaims());
    }

    @Bean
    public AuthenticationSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) -> {
            RevokeSession revokeSession = context.getBean(RevokeSession.class);
            OidcLogoutAuthenticationToken oidcLogoutAuthentication = (OidcLogoutAuthenticationToken) authentication;
            LogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            LogoutSuccessHandler logoutSuccessHandler =
                    new AuthorizationRevokingLogoutSuccessHandler(revokeSession);

            if (oidcLogoutAuthentication.isPrincipalAuthenticated() && StringUtils.hasText(oidcLogoutAuthentication.getSessionId())) {
                logoutHandler.logout(request, response, (Authentication) oidcLogoutAuthentication.getPrincipal());
            }
            logoutSuccessHandler.onLogoutSuccess(request, response, oidcLogoutAuthentication);
        };
    }
}
