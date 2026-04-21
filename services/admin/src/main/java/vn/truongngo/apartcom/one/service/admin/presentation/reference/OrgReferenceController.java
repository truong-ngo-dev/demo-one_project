package vn.truongngo.apartcom.one.service.admin.presentation.reference;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.admin.application.reference.register_org.RegisterOrg;

@RestController
@RequestMapping("/api/v1/orgs")
@RequiredArgsConstructor
public class OrgReferenceController {

    private final RegisterOrg.Handler registerOrgHandler;

    @PostMapping
    public ResponseEntity<Void> registerOrg(@RequestBody RegisterOrgRequest req) {
        registerOrgHandler.handle(new RegisterOrg.Command(req.orgId(), req.name(), req.orgType()));
        return ResponseEntity.noContent().build();
    }

    public record RegisterOrgRequest(String orgId, String name, String orgType) {}
}
