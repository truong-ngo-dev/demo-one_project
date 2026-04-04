package vn.truongngo.apartcom.one.service.webgateway.infrastructure.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;
    private final SessionMappingAuthenticationSuccessHandler sessionMappingSuccessHandler;
    private final ReactiveStringRedisTemplate redisTemplate;

    @Value("${app.logout.post-redirect-uri}")
    private String postLogoutRedirectUri;

    @Value("${app.logout.end-session-uri}")
    private String endSessionUri;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .headers(conf -> conf.frameOptions(ServerHttpSecurity.HeaderSpec.FrameOptionsSpec::disable))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/webgw/internal/**").permitAll()
                        .pathMatchers(org.springframework.http.HttpMethod.POST, "/api/admin/v1/users/register").permitAll()
                        .pathMatchers("/api/**").authenticated()
                        .anyExchange().permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(sessionMappingSuccessHandler))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(smartAuthenticationEntryPoint()))
                .logout(spec -> spec
                        .logoutUrl("/webgw/auth/logout")
                        .logoutSuccessHandler(logoutSuccessHandler()))
                .build();
    }

    private ServerLogoutSuccessHandler logoutSuccessHandler() {
        WebGatewayLogoutSuccessHandler handler = new WebGatewayLogoutSuccessHandler(
                this.clientRegistrationRepository, this.redisTemplate);
        handler.setPostLogoutRedirectUri(this.postLogoutRedirectUri);
        handler.setEndSessionUri(this.endSessionUri);
        return handler;
    }

    @Bean
    public ServerAuthenticationEntryPoint smartAuthenticationEntryPoint() {
        return (exchange, e) -> {
            String path = exchange.getRequest().getPath().value();
            MediaType accept = exchange.getRequest().getHeaders().getAccept()
                    .stream().findFirst().orElse(MediaType.ALL);

            boolean isApiCall = path.startsWith("/api") || !accept.includes(MediaType.TEXT_HTML);

            return isApiCall
                    ? new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED).commence(exchange, e)
                    : new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/web-gateway").commence(exchange, e);
        };
    }
}
