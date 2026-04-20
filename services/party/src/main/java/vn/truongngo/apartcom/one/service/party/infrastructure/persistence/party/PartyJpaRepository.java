package vn.truongngo.apartcom.one.service.party.infrastructure.persistence.party;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyStatus;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyIdentificationType;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyType;

public interface PartyJpaRepository extends JpaRepository<PartyJpaEntity, String> {

    boolean existsByIdentificationsTypeAndIdentificationsValue(
            PartyIdentificationType type, String value);

    @Query("SELECT p FROM PartyJpaEntity p WHERE " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:type IS NULL OR p.type = :type) AND " +
           "(:status IS NULL OR p.status = :status)")
    Page<PartyJpaEntity> search(@Param("keyword") String keyword,
                                @Param("type") PartyType type,
                                @Param("status") PartyStatus status,
                                Pageable pageable);
}
