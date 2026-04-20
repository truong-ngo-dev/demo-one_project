package vn.truongngo.apartcom.one.service.property.application.agreement.find_by_party;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.property.application.agreement.AgreementView;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementRepository;

import java.util.List;

public class FindAgreementsByParty {

    public record Query(String partyId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, List<AgreementView>> {

        private final OccupancyAgreementRepository agreementRepository;

        @Override
        public List<AgreementView> handle(Query query) {
            return agreementRepository.findByPartyId(query.partyId())
                    .stream()
                    .map(AgreementView::from)
                    .toList();
        }
    }
}
