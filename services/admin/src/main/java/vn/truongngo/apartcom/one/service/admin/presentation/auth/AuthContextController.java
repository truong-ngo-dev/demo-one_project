package vn.truongngo.apartcom.one.service.admin.presentation.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.truongngo.apartcom.one.service.admin.application.auth.get_contexts.GetUserContexts;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthContextController {

    private final GetUserContexts.Handler getUserContextsHandler;

    @GetMapping("/contexts")
    public ResponseEntity<ApiResponse<List<GetUserContexts.ContextView>>> getUserContexts(
            @RequestParam String userId) {
        List<GetUserContexts.ContextView> result = getUserContextsHandler.handle(
                new GetUserContexts.Query(userId));
        return ResponseEntity.ok(ApiResponse.of(result));
    }
}
