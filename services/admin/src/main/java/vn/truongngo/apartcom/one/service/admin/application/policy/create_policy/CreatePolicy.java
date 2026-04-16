package vn.truongngo.apartcom.one.service.admin.application.policy.create_policy;

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
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy_set.PolicySetRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.SpelValidator;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;

public class CreatePolicy {

    public record Command(Long policySetId, String name, String targetExpression,
                          CombineAlgorithmName combineAlgorithm) {
        public Command {
            Assert.notNull(policySetId, "policySetId is required");
            Assert.hasText(name, "name is required");
            Assert.notNull(combineAlgorithm, "combineAlgorithm is required");
        }
    }

    public record Result(Long id) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final PolicyRepository policyRepository;
        private final PolicySetRepository policySetRepository;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Result handle(Command command) {
            PolicySetId policySetId = PolicySetId.of(command.policySetId());
            if (policySetRepository.findById(policySetId).isEmpty()) {
                throw PolicyException.policyNotFound();
            }
            ExpressionNode target = null;
            if (command.targetExpression() != null && !command.targetExpression().isBlank()) {
                SpelValidator.validate(command.targetExpression());
                target = new ExpressionNode.Inline(null, command.targetExpression());
            }
            PolicyDefinition policy = PolicyDefinition.create(
                    policySetId, command.name(), target, command.combineAlgorithm());
            PolicyDefinition saved = policyRepository.save(policy);

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "name", saved.getName(),
                    "combineAlgorithm", saved.getCombineAlgorithm().name()
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.POLICY, saved.getId().getValue(), saved.getName(),
                    AuditActionType.CREATED, AuditHelper.currentPerformedBy(), snapshot));

            return new Result(saved.getId().getValue());
        }
    }
}
