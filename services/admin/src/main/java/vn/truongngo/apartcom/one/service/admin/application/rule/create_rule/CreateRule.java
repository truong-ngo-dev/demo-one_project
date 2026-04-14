package vn.truongngo.apartcom.one.service.admin.application.rule.create_rule;

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
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.Effect;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.ExpressionVO;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.RuleDefinition;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.SpelValidator;
import vn.truongngo.apartcom.one.service.admin.infrastructure.cross_cutting.audit.AuditHelper;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CreateRule {

    public record Command(Long policyId, String name, String description,
                          String targetExpression, String conditionExpression,
                          Effect effect, int orderIndex) {
        public Command {
            Assert.notNull(policyId, "policyId is required");
            Assert.hasText(name, "name is required");
            Assert.notNull(effect, "effect is required");
        }
    }

    public record Result(Long id) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Result> {

        private final PolicyRepository policyRepository;
        private final EventDispatcher eventDispatcher;
        private final ObjectMapper objectMapper;

        @Override
        @Transactional
        @SneakyThrows
        public Result handle(Command command) {
            PolicyId policyId = PolicyId.of(command.policyId());
            PolicyDefinition policy = policyRepository.findById(policyId)
                    .orElseThrow(PolicyException::policyNotFound);

            Set<Long> existingIds = policy.getRules().stream()
                    .filter(r -> r.getId() != null)
                    .map(r -> r.getId().getValue())
                    .collect(Collectors.toSet());

            ExpressionVO target = resolveExpression(command.targetExpression(), null);
            ExpressionVO condition = resolveExpression(command.conditionExpression(), null);

            RuleDefinition rule = RuleDefinition.create(
                    policyId, command.name(), command.description(),
                    target, condition, command.effect(), command.orderIndex());

            PolicyDefinition updated = policy.addRule(rule);
            PolicyDefinition saved = policyRepository.save(updated);

            RuleDefinition newRule = saved.getRules().stream()
                    .filter(r -> r.getId() != null && !existingIds.contains(r.getId().getValue()))
                    .findFirst()
                    .orElseThrow(PolicyException::ruleNotFound);

            String snapshot = objectMapper.writeValueAsString(Map.of(
                    "name", newRule.getName(),
                    "effect", newRule.getEffect().name(),
                    "targetExpression", newRule.getTargetExpression() != null ? newRule.getTargetExpression().spElExpression() : "",
                    "conditionExpression", newRule.getConditionExpression() != null ? newRule.getConditionExpression().spElExpression() : ""
            ));
            eventDispatcher.dispatch(new AbacAuditLogEvent(
                    AuditEntityType.RULE, newRule.getId().getValue(), newRule.getName(),
                    AuditActionType.CREATED, AuditHelper.currentPerformedBy(), snapshot));

            return new Result(newRule.getId().getValue());
        }

        private ExpressionVO resolveExpression(String spEl, Long existingId) {
            if (spEl == null || spEl.isBlank()) return null;
            SpelValidator.validate(spEl);
            return new ExpressionVO(existingId, spEl);
        }
    }
}
