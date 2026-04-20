package vn.truongngo.apartcom.one.service.property.application.agreement.create;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.service.property.application.agreement.AgreementView;
import vn.truongngo.apartcom.one.service.property.domain.agreement.*;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetException;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetId;
import vn.truongngo.apartcom.one.service.property.domain.fixed_asset.FixedAssetRepository;

import java.time.LocalDate;

public class CreateOccupancyAgreement {

    public record Command(
            String partyId,
            PartyType partyType,
            String assetId,
            OccupancyAgreementType agreementType,
            LocalDate startDate,
            LocalDate endDate,
            String contractRef
    ) {}

    public record Result(String agreementId) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final FixedAssetRepository fixedAssetRepository;
        private final OccupancyAgreementRepository agreementRepository;

        @Override
        @Transactional
        public Result handle(Command command) {
            var asset = fixedAssetRepository.findById(FixedAssetId.of(command.assetId()))
                    .orElseThrow(FixedAssetException::notFound);

            if (command.agreementType() == OccupancyAgreementType.OWNERSHIP
                    && agreementRepository.existsActiveByAssetIdAndType(command.assetId(), OccupancyAgreementType.OWNERSHIP)) {
                throw OccupancyAgreementException.ownershipAlreadyExists();
            }
            if (command.agreementType() == OccupancyAgreementType.LEASE
                    && agreementRepository.existsActiveByAssetIdAndType(command.assetId(), OccupancyAgreementType.LEASE)) {
                throw OccupancyAgreementException.leaseAlreadyExists();
            }

            var agreement = OccupancyAgreement.create(
                    command.partyId(), command.partyType(), command.assetId(),
                    asset.getType(), command.agreementType(),
                    command.startDate(), command.endDate(), command.contractRef()
            );

            var saved = agreementRepository.save(agreement);
            return new Result(saved.getId().getValue());
        }
    }
}
