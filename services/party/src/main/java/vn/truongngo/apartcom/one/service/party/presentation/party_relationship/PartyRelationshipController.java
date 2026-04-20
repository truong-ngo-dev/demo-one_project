package vn.truongngo.apartcom.one.service.party.presentation.party_relationship;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.party.application.party_relationship.add_member.AddMember;
import vn.truongngo.apartcom.one.service.party.application.party_relationship.find_by_party.FindRelationshipsByParty;
import vn.truongngo.apartcom.one.service.party.application.party_relationship.remove_member.RemoveMember;
import vn.truongngo.apartcom.one.service.party.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.party.presentation.party.PartyController;
import vn.truongngo.apartcom.one.service.party.presentation.party_relationship.model.AddMemberRequest;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/party-relationships")
@RequiredArgsConstructor
public class PartyRelationshipController {

    private final AddMember.Handler addMemberHandler;
    private final RemoveMember.Handler removeMemberHandler;
    private final FindRelationshipsByParty.Handler findRelationshipsByPartyHandler;

    // UC-010: Thêm thành viên
    @PostMapping
    public ResponseEntity<ApiResponse<PartyController.IdResponse>> addMember(
            @RequestBody AddMemberRequest request) {
        AddMember.Result result = addMemberHandler.handle(new AddMember.Command(
                request.personId(),
                request.groupId(),
                request.fromRole(),
                request.startDate() != null ? request.startDate() : LocalDate.now()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(new PartyController.IdResponse(result.relationshipId())));
    }

    // UC-011: Xoá thành viên
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeMember(@PathVariable String id) {
        removeMemberHandler.handle(new RemoveMember.Command(id));
        return ResponseEntity.noContent().build();
    }

    // UC-012: Tìm relationships theo Party
    @GetMapping
    public ResponseEntity<ApiResponse<List<FindRelationshipsByParty.RelationshipView>>> findRelationships(
            @RequestParam String partyId,
            @RequestParam(required = false) String direction) {
        FindRelationshipsByParty.Query query = new FindRelationshipsByParty.Query(
                partyId,
                direction != null ? direction : "BOTH"
        );
        List<FindRelationshipsByParty.RelationshipView> result = findRelationshipsByPartyHandler.handle(query);
        return ResponseEntity.ok(ApiResponse.of(result));
    }
}
