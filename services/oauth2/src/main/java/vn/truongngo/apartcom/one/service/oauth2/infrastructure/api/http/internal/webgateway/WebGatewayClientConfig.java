package vn.truongngo.apartcom.one.service.oauth2.infrastructure.api.http.internal.webgateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WebGatewayClientConfig {

    @Bean
    public RestClient gatewayRestClient(@Value("${app.gateway-service.base-url}") String baseUrl) {
        return RestClient.builder().baseUrl(baseUrl).build();
    }
}
