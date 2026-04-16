package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.lib.abac.pep.PreEnforce;
import vn.truongngo.apartcom.one.lib.abac.rap.ResourceMapping;
import vn.truongngo.apartcom.one.service.admin.application.expression.delete_named.DeleteNamedExpression;
import vn.truongngo.apartcom.one.service.admin.application.expression.list_named.ListNamedExpressions;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;

@RestController
@RequestMapping("/api/v1/abac/expressions")
@RequiredArgsConstructor
public class ExpressionController {

    private final ListNamedExpressions.Handler listHandler;
    private final DeleteNamedExpression.Handler deleteHandler;

    @GetMapping
    @ResourceMapping(resource = "abac_expression", action = "LIST")
    @PreEnforce
    public ResponseEntity<ApiResponse<ListNamedExpressions.Result>> list() {
        return ResponseEntity.ok(ApiResponse.of(listHandler.handle(new ListNamedExpressions.Query())));
    }

    @DeleteMapping("/{id}")
    @ResourceMapping(resource = "abac_expression", action = "DELETE")
    @PreEnforce
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteHandler.handle(new DeleteNamedExpression.Command(id));
        return ResponseEntity.noContent().build();
    }
}
