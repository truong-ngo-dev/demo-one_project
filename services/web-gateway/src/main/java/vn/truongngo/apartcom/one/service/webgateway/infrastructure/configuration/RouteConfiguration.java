package vn.truongngo.apartcom.one.service.webgateway.infrastructure.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfiguration {

    @Value("${webgateway.routes.admin-service.uri}")
    private String adminServiceUri;

    @Value("${webgateway.routes.oauth2-service.uri}")
    private String oauth2ServiceUri;

    @Bean
    public RouteLocator gateway(RouteLocatorBuilder builder) {
        return builder.routes()
                // /api/admin/v1/** → admin-service /api/v1/**
                .route("admin-service", rs -> rs
                        .path("/api/admin/**")
                        .filters(f -> f
                                .tokenRelay()
                                .saveSession()
                                .rewritePath("/api/admin/(?<segment>.*)", "/api/${segment}"))
                        .uri(adminServiceUri))
                // /api/oauth2/v1/** → oauth2-service /api/v1/**
                .route("oauth2-service", rs -> rs
                        .path("/api/oauth2/**")
                        .filters(f -> f
                                .tokenRelay()
                                .saveSession()
                                .rewritePath("/api/oauth2/(?<segment>.*)", "/api/${segment}"))
                        .uri(oauth2ServiceUri))
                .build();
    }
}
