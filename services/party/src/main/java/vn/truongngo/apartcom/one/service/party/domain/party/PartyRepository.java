package vn.truongngo.apartcom.one.service.party.domain.party;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.truongngo.apartcom.one.lib.common.domain.service.Repository;

public interface PartyRepository extends Repository<Party, PartyId> {

    boolean existsByIdentification(PartyIdentificationType type, String value);

    Page<Party> search(String keyword, PartyType type, PartyStatus status, Pageable pageable);
}
