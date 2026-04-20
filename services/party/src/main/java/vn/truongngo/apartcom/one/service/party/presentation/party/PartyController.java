package vn.truongngo.apartcom.one.service.party.presentation.party;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.party.application.party.add_identification.AddPartyIdentification;
import vn.truongngo.apartcom.one.service.party.application.party.create_household.CreateHousehold;
import vn.truongngo.apartcom.one.service.party.application.party.create_organization.CreateOrganization;
import vn.truongngo.apartcom.one.service.party.application.party.create_person.CreatePerson;
import vn.truongngo.apartcom.one.service.party.application.party.find_by_id.FindPartyById;
import vn.truongngo.apartcom.one.service.party.application.party.search.SearchParties;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyId;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyStatus;
import vn.truongngo.apartcom.one.service.party.domain.party.PartyType;
import vn.truongngo.apartcom.one.service.party.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.party.presentation.base.PagedApiResponse;
import vn.truongngo.apartcom.one.service.party.presentation.party.model.AddPartyIdentificationRequest;
import vn.truongngo.apartcom.one.service.party.presentation.party.model.CreatePartyRequest;
import vn.truongngo.apartcom.one.service.party.presentation.party.model.IdentificationRequest;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyController {

    private final CreatePerson.Handler createPersonHandler;
    private final CreateOrganization.Handler createOrganizationHandler;
    private final CreateHousehold.Handler createHouseholdHandler;
    private final AddPartyIdentification.Handler addIdentificationHandler;
    private final FindPartyById.Handler findPartyByIdHandler;
    private final SearchParties.Handler searchPartiesHandler;

    // UC-001/002/003: Tạo Party — dispatch theo type
    @PostMapping
    public ResponseEntity<ApiResponse<IdResponse>> createParty(@RequestBody CreatePartyRequest request) {
        String partyId = switch (request.type()) {
            case PERSON -> createPersonHandler.handle(new CreatePerson.Command(
                    request.partyName(),
                    request.firstName(),
                    request.lastName(),
                    request.dob(),
                    request.gender(),
                    toIdentificationInputs(request.identifications())
            )).partyId();
            case ORGANIZATION -> createOrganizationHandler.handle(new CreateOrganization.Command(
                    request.partyName(),
                    request.orgType(),
                    request.taxId(),
                    request.registrationNo(),
                    toOrganizationIdentificationInputs(request.identifications())
            )).partyId();
            case HOUSEHOLD -> createHouseholdHandler.handle(new CreateHousehold.Command(
                    request.partyName(),
                    request.headPersonId()
            )).partyId();
        };
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(new IdResponse(partyId)));
    }

    // UC-006: Tìm Party theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FindPartyById.PartyView>> getPartyById(@PathVariable String id) {
        FindPartyById.PartyView view = findPartyByIdHandler.handle(new FindPartyById.Query(id));
        return ResponseEntity.ok(ApiResponse.of(view));
    }

    // UC-007: Tìm kiếm Parties
    @GetMapping
    public ResponseEntity<PagedApiResponse<SearchParties.PartySummaryView>> searchParties(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) PartyType type,
            @RequestParam(required = false) PartyStatus status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        SearchParties.Query query = SearchParties.Query.of(keyword, type, status, page, size);
        return ResponseEntity.ok(PagedApiResponse.of(searchPartiesHandler.handle(query)));
    }

    // UC-004: Thêm định danh pháp lý
    @PostMapping("/{id}/identifications")
    public ResponseEntity<ApiResponse<IdResponse>> addIdentification(
            @PathVariable String id,
            @RequestBody AddPartyIdentificationRequest request) {
        addIdentificationHandler.handle(new AddPartyIdentification.Command(
                PartyId.of(id),
                request.type(),
                request.value(),
                request.issuedDate()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(new IdResponse(id)));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private List<CreatePerson.IdentificationInput> toIdentificationInputs(List<IdentificationRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream()
                .map(r -> new CreatePerson.IdentificationInput(r.type(), r.value(), r.issuedDate()))
                .toList();
    }

    private List<CreateOrganization.IdentificationInput> toOrganizationIdentificationInputs(
            List<IdentificationRequest> reqs) {
        if (reqs == null) return null;
        return reqs.stream()
                .map(r -> new CreateOrganization.IdentificationInput(r.type(), r.value(), r.issuedDate()))
                .toList();
    }

    public record IdResponse(String id) {}
}
