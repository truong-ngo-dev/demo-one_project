package vn.truongngo.apartcom.one.service.admin.application.event.organization;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReference;
import vn.truongngo.apartcom.one.service.admin.domain.reference.OrgReferenceRepository;

@Component
@RequiredArgsConstructor
public class OrganizationCreatedEventHandler {

    private final OrgReferenceRepository orgReferenceRepository;

    @Transactional
    public void handle(OrganizationCreatedPayload payload) {
        if ("BQL".equals(payload.orgType())) {
            orgReferenceRepository.upsert(
                    OrgReference.of(payload.partyId(), payload.name(), payload.orgType()));
        }
        // other orgTypes: no-op
    }

    public record OrganizationCreatedPayload(String partyId, String name, String orgType) {}
}
