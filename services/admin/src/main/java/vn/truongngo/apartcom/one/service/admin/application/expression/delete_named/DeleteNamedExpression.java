package vn.truongngo.apartcom.one.service.admin.application.expression.delete_named;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.truongngo.apartcom.one.lib.common.application.CommandHandler;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionId;
import vn.truongngo.apartcom.one.service.admin.domain.abac.expression.NamedExpressionRepository;
import vn.truongngo.apartcom.one.service.admin.domain.abac.policy.PolicyException;

public class DeleteNamedExpression {

    public record Command(Long id) {
        public Command {
            Assert.notNull(id, "id is required");
        }
    }

    @Component
    @RequiredArgsConstructor
    public static class Handler implements CommandHandler<Command, Void> {

        private final NamedExpressionRepository namedExpressionRepository;

        @Override
        @Transactional
        public Void handle(Command command) {
            NamedExpressionId id = NamedExpressionId.of(command.id());
            if (namedExpressionRepository.isInUse(id)) {
                throw PolicyException.namedExpressionInUse();
            }
            namedExpressionRepository.delete(id);
            return null;
        }
    }
}
