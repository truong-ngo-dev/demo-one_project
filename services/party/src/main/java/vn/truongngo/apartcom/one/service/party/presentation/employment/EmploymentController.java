package vn.truongngo.apartcom.one.service.party.presentation.employment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.party.application.employment.EmploymentView;
import vn.truongngo.apartcom.one.service.party.application.employment.assign_position.AssignPosition;
import vn.truongngo.apartcom.one.service.party.application.employment.create.CreateEmployment;
import vn.truongngo.apartcom.one.service.party.application.employment.find_by_org.FindEmploymentsByOrg;
import vn.truongngo.apartcom.one.service.party.application.employment.find_by_person.FindEmploymentByPerson;
import vn.truongngo.apartcom.one.service.party.application.employment.terminate.TerminateEmployment;
import vn.truongngo.apartcom.one.service.party.domain.employment.EmploymentStatus;
import vn.truongngo.apartcom.one.service.party.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.party.presentation.base.ErrorResponse;
import vn.truongngo.apartcom.one.service.party.presentation.employment.model.AssignPositionRequest;
import vn.truongngo.apartcom.one.service.party.presentation.employment.model.CreateEmploymentRequest;
import vn.truongngo.apartcom.one.service.party.presentation.employment.model.TerminateEmploymentRequest;
import vn.truongngo.apartcom.one.service.party.presentation.party.PartyController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/employments")
@RequiredArgsConstructor
public class EmploymentController {

    private final CreateEmployment.Handler createEmploymentHandler;
    private final TerminateEmployment.Handler terminateEmploymentHandler;
    private final AssignPosition.Handler assignPositionHandler;
    private final FindEmploymentsByOrg.Handler findByOrgHandler;
    private final FindEmploymentByPerson.Handler findByPersonHandler;

    // UC-013: Tạo Employment
    @PostMapping
    public ResponseEntity<ApiResponse<PartyController.IdResponse>> createEmployment(
            @RequestBody CreateEmploymentRequest request) {
        CreateEmployment.Result result = createEmploymentHandler.handle(new CreateEmployment.Command(
                request.personId(),
                request.orgId(),
                request.employmentType(),
                request.startDate() != null ? request.startDate() : LocalDate.now()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(new PartyController.IdResponse(result.employmentId())));
    }

    // UC-014: Terminate Employment
    @PostMapping("/{id}/terminate")
    public ResponseEntity<ApiResponse<Void>> terminateEmployment(
            @PathVariable String id,
            @RequestBody TerminateEmploymentRequest request) {
        terminateEmploymentHandler.handle(new TerminateEmployment.Command(
                id,
                request.endDate() != null ? request.endDate() : LocalDate.now()
        ));
        return ResponseEntity.ok(ApiResponse.of(null));
    }

    // UC-015: Giao chức vụ
    @PostMapping("/{id}/positions")
    public ResponseEntity<ApiResponse<PartyController.IdResponse>> assignPosition(
            @PathVariable String id,
            @RequestBody AssignPositionRequest request) {
        AssignPosition.Result result = assignPositionHandler.handle(new AssignPosition.Command(
                id,
                request.position(),
                request.department(),
                request.startDate() != null ? request.startDate() : LocalDate.now()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(new PartyController.IdResponse(result.positionAssignmentId())));
    }

    // UC-016 + UC-017: Tìm Employment (phân biệt bằng orgId vs personId)
    @GetMapping
    public ResponseEntity<?> findEmployments(
            @RequestParam(required = false) String orgId,
            @RequestParam(required = false) String personId,
            @RequestParam(required = false) EmploymentStatus status) {
        if (orgId != null) {
            List<EmploymentView> result = findByOrgHandler.handle(
                    new FindEmploymentsByOrg.Query(orgId, status));
            return ResponseEntity.ok(ApiResponse.of(result));
        }
        if (personId != null) {
            List<EmploymentView> result = findByPersonHandler.handle(
                    new FindEmploymentByPerson.Query(personId, status));
            return ResponseEntity.ok(ApiResponse.of(result));
        }
        return ResponseEntity.badRequest()
                .body(ErrorResponse.of("INVALID_REQUEST", "Either orgId or personId is required"));
    }
}
