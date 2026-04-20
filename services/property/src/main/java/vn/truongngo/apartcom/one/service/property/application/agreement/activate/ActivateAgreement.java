package vn.truongngo.apartcom.one.service.property.application.agreement.activate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementException;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementId;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementRepository;
import vn.truongngo.apartcom.one.service.property.domain.agreement.event.OccupancyAgreementActivatedEvent;

public class ActivateAgreement {

    public record Command(String agreementId) {}

    public record Result() {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final OccupancyAgreementRepository agreementRepository;
        private final EventDispatcher eventDispatcher;

        @Override
        @Transactional
        public Result handle(Command command) {
            var agreement = agreementRepository.findById(OccupancyAgreementId.of(command.agreementId()))
                    .orElseThrow(OccupancyAgreementException::notFound);

            agreement.activate();
            agreementRepository.save(agreement);

            eventDispatcher.dispatch(new OccupancyAgreementActivatedEvent(
                    agreement.getId(),
                    agreement.getPartyId(),
                    agreement.getPartyType(),
                    agreement.getAssetId(),
                    agreement.getAgreementType()
            ));

            return new Result();
        }
    }
}
