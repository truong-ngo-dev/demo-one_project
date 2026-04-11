package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.admin.application.audit.list_audit_log.ListAuditLog;
import vn.truongngo.apartcom.one.service.admin.domain.abac.audit.AuditEntityType;
import vn.truongngo.apartcom.one.service.admin.presentation.base.PagedApiResponse;

@RestController
@RequestMapping("/api/v1/abac/audit-log")
@RequiredArgsConstructor
public class AuditLogController {

    private final ListAuditLog.Handler listHandler;

    @GetMapping
    public ResponseEntity<PagedApiResponse<ListAuditLog.AuditLogEntry>> list(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String performedBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        AuditEntityType type = null;
        if (entityType != null && !entityType.isBlank()) {
            type = AuditEntityType.valueOf(entityType.toUpperCase());
        }

        Page<ListAuditLog.AuditLogEntry> result = listHandler.handle(
                new ListAuditLog.Query(type, entityId, performedBy, page, size));
        return ResponseEntity.ok(PagedApiResponse.of(result));
    }
}
