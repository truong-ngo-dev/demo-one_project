package vn.truongngo.apartcom.one.service.admin.application.rule.query.impact_preview;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.application.rule.SpelExpressionAnalyzer;

import java.util.List;

/**
 * UC-032 — Rule Impact Preview.
 *
 * Stateless query: parses SpEL expressions and extracts subject-side + action conditions
 * via AST walking, without evaluating against any runtime context.
 */
public class GetRuleImpactPreview {

    public record Query(String targetExpression, String conditionExpression) {}

    public record Result(
            List<String> requiredRoles,
            List<String> requiredAttributes,
            List<String> specificActions,
            boolean navigableWithoutData,
            boolean hasInstanceCondition,
            String parseWarning
    ) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        @Override
        public Result handle(Query query) {
            SpelExpressionAnalyzer.AnalysisResult analysis =
                    SpelExpressionAnalyzer.analyze(query.targetExpression(), query.conditionExpression());
            return new Result(
                    analysis.requiredRoles(),
                    analysis.requiredAttributes(),
                    analysis.specificActions(),
                    analysis.navigableWithoutData(),
                    analysis.hasInstanceCondition(),
                    analysis.parseWarning()
            );
        }
    }
}
