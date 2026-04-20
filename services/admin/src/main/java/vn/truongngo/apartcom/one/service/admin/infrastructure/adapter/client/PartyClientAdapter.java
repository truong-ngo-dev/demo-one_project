package vn.truongngo.apartcom.one.service.admin.infrastructure.adapter.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import vn.truongngo.apartcom.one.service.admin.domain.party.PartyClient;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class PartyClientAdapter implements PartyClient {

    private final RestClient restClient;

    public PartyClientAdapter(@Value("${party-service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<String> getMembers(String partyId) {
        try {
            List<String> members = restClient.get()
                    .uri("/internal/parties/{partyId}/members", partyId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<String>>() {});
            return members != null ? members : Collections.emptyList();
        } catch (Exception e) {
            log.warn("Failed to fetch members for party {}: {}", partyId, e.getMessage());
            return Collections.emptyList();
        }
    }
}
