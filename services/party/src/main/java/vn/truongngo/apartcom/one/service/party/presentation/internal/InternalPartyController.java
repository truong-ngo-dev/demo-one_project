package vn.truongngo.apartcom.one.service.party.presentation.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.party.application.party.find_by_id.FindPartyById;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyStatus;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyType;
import vn.truongngo.apartcom.one.service.party.presentation.base.ApiResponse;

import java.util.List;

// UC-008, UC-009: Internal API — accessible via internal network only, no public gateway exposure
@RestController
@RequestMapping("/internal/parties")
@RequiredArgsConstructor
public class InternalPartyController {

    private final FindPartyById.Handler findPartyByIdHandler;

    public record PartyBasicView(String id, PartyType type, String name, PartyStatus status) {}

    // UC-008: Basic Party info
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyBasicView>> getPartyBasic(@PathVariable String id) {
        FindPartyById.PartyView view = findPartyByIdHandler.handle(new FindPartyById.Query(id));
        return ResponseEntity.ok(ApiResponse.of(
                new PartyBasicView(view.id(), view.type(), view.name(), view.status())
        ));
    }

    // UC-009: Members of Household/Org
    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<PartyBasicView>>> getMembers(@PathVariable String id) {
        // Validate the party exists (throws PARTY_NOT_FOUND if not)
        FindPartyById.PartyView view = findPartyByIdHandler.handle(new FindPartyById.Query(id));

        if (view.type() == PartyType.PERSON) {
            throw vn.truongngo.apartcom.one.service.party.domain.party.PartyException.invalidPartyType();
        }

        // TODO Phase 2: load members via PartyRelationship aggregate
        return ResponseEntity.ok(ApiResponse.of(List.of()));
    }
}
