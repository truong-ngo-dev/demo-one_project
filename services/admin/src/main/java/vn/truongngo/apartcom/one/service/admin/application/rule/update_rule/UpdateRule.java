package vn.truongngo.apartcom.one.service.admin.application.rule.update_rule;

import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.application.EventDispatcher;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.application.expression.ExpressionTreeService;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AbacAuditLogEvent;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditActionType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionNode;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.SpelValidator;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;

public class UpdateRule {

    public record Command(Long policyId, Long ruleId, String name, String description,
                          ExpressionNode targetExpression, ExpressionNode conditionExpression,
                          Effect effect) {
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
        private final ExpressionTreeService expressionTreeService;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Void handle(Command command) {
            PolicyDefinition policy = policyRepository.findById(PolicyId.of(command.policyId()))
                    .orElseThrow(PolicyException::policyNotFound);
            RuleId ruleId = RuleId.of(command.ruleId());
            policy.getRules().stream()
                    .filter(r -> ruleId.equals(r.getId()))
                    .findFirst()
                    .orElseThrow(PolicyException::ruleNotFound);

            validateExpression(command.targetExpression());
            validateExpression(command.conditionExpression());

            PolicyDefinition updated = policy.updateRule(ruleId, command.name(), command.description(),
                    command.targetExpression(), command.conditionExpression(), command.effect());
            policyRepository.save(updated);

            String resolvedTarget = expressionTreeService.resolveFromNode(command.targetExpression());
            String resolvedCondition = expressionTreeService.resolveFromNode(command.conditionExpression());

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "name", command.name(),
                    "effect", command.effect().name(),
                    "targetExpression", resolvedTarget != null ? resolvedTarget : "",
                    "conditionExpression", resolvedCondition != null ? resolvedCondition : ""
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.RULE, command.ruleId(), command.name(),
                    AuditActionType.UPDATED, AuditHelper.currentPerformedBy(), snapshot));
            return null;
        }

        private void validateExpression(ExpressionNode node) {
            if (node == null) return;
            String spel = expressionTreeService.resolveFromNode(node);
            if (spel != null && !spel.isBlank()) {
                SpelValidator.validate(spel);
            }
        }
    }
}
