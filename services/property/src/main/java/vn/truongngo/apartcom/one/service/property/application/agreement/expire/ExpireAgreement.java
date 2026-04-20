package vn.truongngo.apartcom.one.service.property.application.agreement.expire;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementException;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementId;
import vn.truongngo.apartcom.one.service.property.domain.agreement.OccupancyAgreementRepository;
import vn.truongngo.apartcom.one.service.property.domain.agreement.event.OccupancyAgreementTerminatedEvent;

public class ExpireAgreement {

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

            agreement.expire();
            agreementRepository.save(agreement);

            eventDispatcher.dispatch(new OccupancyAgreementTerminatedEvent(
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
