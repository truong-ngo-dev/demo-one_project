package vn.truongngo.apartcom.one.service.admin.application.policy.update_policy;

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
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionVO;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.SpelValidator;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;

public class UpdatePolicy {

    public record Command(Long id, String targetExpression, CombineAlgorithmName combineAlgorithm) {
        public Command {
            Assert.notNull(id, "id is required");
            Assert.notNull(combineAlgorithm, "combineAlgorithm is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PolicyRepository repository;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Void handle(Command command) {
            PolicyDefinition policy = repository.findById(PolicyId.of(command.id()))
                    .orElseThrow(PolicyException::policyNotFound);
            ExpressionVO target = null;
            if (command.targetExpression() != null && !command.targetExpression().isBlank()) {
                SpelValidator.validate(command.targetExpression());
                ExpressionVO existing = policy.getTargetExpression();
                Long existingId = existing != null ? existing.id() : null;
                target = new ExpressionVO(existingId, command.targetExpression());
            }
            PolicyDefinition updated = policy.updatePolicy(target, command.combineAlgorithm());
            repository.save(updated);

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "name", policy.getName(),
                    "combineAlgorithm", command.combineAlgorithm().name()
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.POLICY, command.id(), policy.getName(),
                    AuditActionType.UPDATED, AuditHelper.currentPerformedBy(), snapshot));
            return null;
        }
    }
}
