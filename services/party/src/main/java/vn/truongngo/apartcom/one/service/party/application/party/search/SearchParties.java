package vn.truongngo.apartcom.one.service.party.application.party.search;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.party.domain.party.Party;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyRepository;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyStatus;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyType;

import java.time.Instant;

public class SearchParties {

    public record Query(String keyword, PartyType type, PartyStatus status, int page, int size) {

        public static Query of(String keyword, PartyType type, PartyStatus status,
                               Integer page, Integer size) {
            return new Query(
                    keyword,
                    type,
                    status,
                    page != null ? page : 0,
                    size != null ? Math.min(size, 100) : 20
            );
        }
    }

    public record PartySummaryView(String id, PartyType type, String name, PartyStatus status, Instant createdAt) {}

    static class Mapper {
        static PartySummaryView toSummary(Party party) {
            return new PartySummaryView(
                    party.getId().getValue(),
                    party.getType(),
                    party.getName(),
                    party.getStatus(),
                    party.getCreatedAt()
            );
        }

        static Pageable toPageable(Query query) {
            return PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Page<PartySummaryView>> {

        private final PartyRepository partyRepository;

        @Override
        public Page<PartySummaryView> handle(Query query) {
            Pageable pageable = Mapper.toPageable(query);
            return partyRepository.search(query.keyword(), query.type(), query.status(), pageable)
                    .map(Mapper::toSummary);
        }
    }
}
