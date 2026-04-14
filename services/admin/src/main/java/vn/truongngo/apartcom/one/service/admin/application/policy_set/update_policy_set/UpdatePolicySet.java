package vn.truongngo.apartcom.one.service.admin.application.policy_set.update_policy_set;

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
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.Scope;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;

public class UpdatePolicySet {

    public record Command(Long id, Scope scope, CombineAlgorithmName combineAlgorithm,
                          boolean isRoot, String tenantId) {
        public Command {
            Assert.notNull(id, "id is required");
            Assert.notNull(scope, "scope is required");
            Assert.notNull(combineAlgorithm, "combineAlgorithm is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PolicySetRepository repository;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Void handle(Command command) {
            PolicySetDefinition policySet = repository.findById(PolicySetId.of(command.id()))
                    .orElseThrow(PolicySetException::policySetNotFound);
            if (command.isRoot() && !policySet.isRoot()) {
                repository.findAllRoot().forEach(existing ->
                        repository.save(existing.update(existing.getScope(),
                                existing.getCombineAlgorithm(), false, existing.getTenantId())));
            }
            PolicySetDefinition updated = policySet.update(
                    command.scope(), command.combineAlgorithm(), command.isRoot(), command.tenantId());
            repository.save(updated);

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "name", policySet.getName(),
                    "combineAlgorithm", command.combineAlgorithm().name(),
                    "isRoot", command.isRoot()
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.POLICY_SET, command.id(), policySet.getName(),
                    AuditActionType.UPDATED, AuditHelper.currentPerformedBy(), snapshot));
            return null;
        }
    }
}
