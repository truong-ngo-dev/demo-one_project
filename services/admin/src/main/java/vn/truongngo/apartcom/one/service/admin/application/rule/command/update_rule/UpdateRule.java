package vn.truongngo.apartcom.one.service.admin.application.rule.command.update_rule;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogEvent;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.AbacPolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionVO;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.SpelValidator;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;

public class UpdateRule {

    public record Command(Long policyId, Long ruleId, String name, String description,
                          String targetExpression, String conditionExpression, Effect effect) {
        public Command {
            Assert.notNull(policyId, "policyId is required");
            Assert.notNull(ruleId, "ruleId is required");
            Assert.hasText(name, "name is required");
            Assert.notNull(effect, "effect is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final PolicyRepository policyRepository;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Void handle(Command command) {
            PolicyDefinition policy = policyRepository.findById(PolicyId.of(command.policyId()))
                    .orElseThrow(AbacPolicyException::policyNotFound);
            RuleId ruleId = RuleId.of(command.ruleId());
            RuleDefinition existing = policy.getRules().stream()
                    .filter(r -> ruleId.equals(r.getId()))
                    .findFirst()
                    .orElseThrow(AbacPolicyException::ruleNotFound);

            ExpressionVO target = resolveExpression(command.targetExpression(),
                    existing.getTargetExpression() != null ? existing.getTargetExpression().id() : null);
            ExpressionVO condition = resolveExpression(command.conditionExpression(),
                    existing.getConditionExpression() != null ? existing.getConditionExpression().id() : null);

            PolicyDefinition updated = policy.updateRule(ruleId, command.name(), command.description(),
                    target, condition, command.effect());
            policyRepository.save(updated);

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "name", command.name(),
                    "effect", command.effect().name(),
                    "targetExpression", command.targetExpression() != null ? command.targetExpression() : "",
                    "conditionExpression", command.conditionExpression() != null ? command.conditionExpression() : ""
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.RULE, command.ruleId(), command.name(),
                    AuditActionType.UPDATED, AuditHelper.currentPerformedBy(), snapshot));
            return null;
        }

        private ExpressionVO resolveExpression(String spel, Long existingId) {
            if (spel == null || spel.isBlank()) return null;
            SpelValidator.validate(spel);
            return new ExpressionVO(existingId, spel);
        }
    }
}
