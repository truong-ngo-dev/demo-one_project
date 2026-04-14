package vn.truongngo.apartcom.one.service.admin.application.policy_set.create_policy_set;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.abac.algorithm.CombineAlgorithmName;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogEvent;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;

public class CreatePolicySet {

    public record Command(String name, Scope scope, CombineAlgorithmName combineAlgorithm,
                          boolean isRoot, String tenantId) {
        public Command {
            Assert.hasText(name, "name is required");
            Assert.notNull(scope, "scope is required");
            Assert.notNull(combineAlgorithm, "combineAlgorithm is required");
        }
    }

    public record Result(Long id) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final PolicySetRepository repository;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Result handle(Command command) {
            if (repository.existsByName(command.name())) {
                throw PolicyException.policySetNameDuplicate();
            }
            if (command.isRoot()) {
                repository.findAllRoot().forEach(existing ->
                        repository.save(existing.update(existing.getScope(),
                                existing.getCombineAlgorithm(), false, existing.getTenantId())));
            }
            PolicySetDefinition policySet = PolicySetDefinition.create(
                    command.name(), command.scope(), command.combineAlgorithm(),
                    command.isRoot(), command.tenantId());
            PolicySetDefinition saved = repository.save(policySet);

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "name", saved.getName(),
                    "combineAlgorithm", saved.getCombineAlgorithm().name(),
                    "isRoot", saved.isRoot()
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.POLICY_SET, saved.getId().getValue(), saved.getName(),
                    AuditActionType.CREATED, AuditHelper.currentPerformedBy(), snapshot));

            return new Result(saved.getId().getValue());
        }
    }
}
