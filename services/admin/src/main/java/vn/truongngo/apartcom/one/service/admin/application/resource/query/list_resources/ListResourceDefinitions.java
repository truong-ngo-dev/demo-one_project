package vn.truongngo.apartcom.one.service.admin.application.resource.query.list_resources;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.resource.ResourceDefinitionRepository;

public class ListResourceDefinitions {

    public record Query(String keyword, int page, int size) {

        public static Query of(String keyword, Integer page, Integer size) {
            return new Query(
                    keyword,
                    page != null ? Math.max(page, 0) : 0,
                    size != null ? Math.min(Math.max(size, 1), 100) : 20);
        }
    }

    public record ResourceSummaryView(Long id, String name, String serviceName, int actionCount) {}

    static class Mapper {
        static ResourceSummaryView toSummary(ResourceDefinition resource) {
            return new ResourceSummaryView(
                    resource.getId() != null ? resource.getId().getValue() : null,
                    resource.getName(),
                    resource.getServiceName(),
                    resource.getActions().size());
        }

        static Pageable toPageable(Query query) {
            return PageRequest.of(query.page(), query.size(), Sort.by(Sort.Direction.ASC, "name"));
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Page<ResourceSummaryView>> {

        private final ResourceDefinitionRepository repository;

        @Override
        public Page<ResourceSummaryView> handle(Query query) {
            Pageable pageable = Mapper.toPageable(query);
            return repository.findAll(query.keyword(), pageable)
                    .map(Mapper::toSummary);
        }
    }
}
