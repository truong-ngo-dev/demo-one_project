package vn.truongngo.apartcom.one.service.admin.application.expression.list_named;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.QueryHandler;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpression;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionRepository;

import java.util.List;

public class ListNamedExpressions {

    public record Query() {}

    public record NamedExpressionView(Long id, String name, String spel) {}

    public record Result(List<NamedExpressionView> items) {}

    @Component
    @RequiredArgsConstructor
    public static class Handler implements QueryHandler<Query, Result> {

        private final NamedExpressionRepository namedExpressionRepository;

        @Override
        @Transactional(readOnly = true)
        public Result handle(Query query) {
            List<NamedExpressionView> items = namedExpressionRepository.findAll().stream()
                    .map(this::toView)
                    .toList();
            return new Result(items);
        }

        private NamedExpressionView toView(NamedExpression expr) {
            return new NamedExpressionView(
                    expr.getId().value(),
                    expr.getName(),
                    expr.getSpel());
        }
    }
}
