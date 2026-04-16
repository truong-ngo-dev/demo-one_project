Task — Sửa Javadoc lỗi thời trong ExpressionTreeService.java
File:                                                                                                  
services/admin/src/main/java/vn/truongngo/apartcom/one/service/admin/application/expression/ExpressionT
reeService.java

Tìm đoạn này:
/**
* Selectively delete an expression tree starting from rootId.
* Behaviour per node type:
*   - LITERAL with named_expression_id → DELETE this row (LibraryRef row), skip the AR
*   - LITERAL with name != null        → SKIP (named inline, potentially shared)
*   - LITERAL anonymous                → DELETE
*   - COMPOSITION                      → recurse children, then DELETE this row
      */

Thay bằng:
/**
* Delete an expression tree starting from rootId.
* Behaviour per node type:
*   - LITERAL → always a LibraryRef row. DELETE this row, never touch the NamedExpression AR.
*   - COMPOSITION → recurse children, then DELETE this row.
* Inline rows do not exist in DB, so no Inline case is handled here.
  */

Không sửa gì thêm — chỉ đúng Javadoc này, implementation bên dưới đã đúng.
