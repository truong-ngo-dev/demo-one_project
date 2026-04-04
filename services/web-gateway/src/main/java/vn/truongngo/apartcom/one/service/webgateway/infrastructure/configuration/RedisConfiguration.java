package vn.truongngo.apartcom.one.service.webgateway.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

/**
 * Enables Redis-backed WebFlux sessions via Spring Session.
 * @EnableRedisWebSession registers ReactiveRedisSessionRepository (implements ReactiveSessionRepository)
 * which is required by SessionRevokeController and the session filter chain.
 * LettuceConnectionFactory and ReactiveStringRedisTemplate are auto-configured
 * by spring-boot-starter-data-redis-reactive from spring.data.redis.* properties.
 */
@Configuration
@EnableRedisWebSession
public class RedisConfiguration {

    /**
     * Use a distinct cookie name so the browser doesn't conflate this service's session
     * with the oauth2 service's SESSION cookie. Both run on localhost and browsers share
     * cookie scope across ports — without this, the oauth2 login flow overwrites the
     * web-gateway's SESSION cookie, causing the PKCE verifier to be lost on callback.
     */
    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("WEBGW_SESSION");
        return resolver;
    }
}
