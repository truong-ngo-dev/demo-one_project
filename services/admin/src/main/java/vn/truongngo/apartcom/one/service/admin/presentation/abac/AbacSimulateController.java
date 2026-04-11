package vn.truongngo.apartcom.one.service.admin.presentation.abac;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.admin.application.simulate.reverse_lookup.GetReverseLookup;
import vn.truongngo.apartcom.one.service.admin.application.simulate.simulate_navigation.SimulateNavigation;
import vn.truongngo.apartcom.one.service.admin.application.simulate.simulate_policy.SimulatePolicy;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.NavigationSimulateRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.abac.model.SimulateRequest;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;

@RestController
@RequestMapping("/api/v1/abac/simulate")
@RequiredArgsConstructor
public class AbacSimulateController {

    private final SimulatePolicy.Handler simulateHandler;
    private final SimulateNavigation.Handler navigationHandler;
    private final GetReverseLookup.Handler reverseLookupHandler;

    @PostMapping
    public ResponseEntity<ApiResponse<SimulatePolicy.SimulateResult>> simulate(
            @RequestBody SimulateRequest request) {
        SimulatePolicy.Command command = new SimulatePolicy.Command(
                new SimulatePolicy.SimulateSubjectRequest(
                        request.subject().userId(),
                        request.subject().roles(),
                        request.subject().attributes()
                ),
                new SimulatePolicy.SimulateResourceRequest(
                        request.resource().name(),
                        request.resource().data()
                ),
                request.action(),
                request.policySetId()
        );
        SimulatePolicy.SimulateResult result = simulateHandler.handle(command);
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @PostMapping("/navigation")
    public ResponseEntity<ApiResponse<SimulateNavigation.Result>> simulateNavigation(
            @RequestBody NavigationSimulateRequest request) {
        SimulateNavigation.Query query = new SimulateNavigation.Query(
                request.subject(),
                request.resourceName(),
                request.policySetId()
        );
        SimulateNavigation.Result result = navigationHandler.handle(query);
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    @GetMapping("/reverse")
    public ResponseEntity<ApiResponse<GetReverseLookup.Result>> reverseLookup(
            @RequestParam String resourceName,
            @RequestParam String actionName,
            @RequestParam(required = false) Long policySetId) {
        GetReverseLookup.Query query = new GetReverseLookup.Query(resourceName, actionName, policySetId);
        GetReverseLookup.Result result = reverseLookupHandler.handle(query);
        return ResponseEntity.ok(ApiResponse.of(result));
    }
}
