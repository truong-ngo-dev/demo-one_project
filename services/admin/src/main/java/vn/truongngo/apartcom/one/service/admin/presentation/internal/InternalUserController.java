package vn.truongngo.apartcom.one.service.admin.presentation.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.lib.shared.dto.user.SocialRegisterResponse;
import vn.truongngo.apartcom.one.lib.shared.dto.user.UserIdentityResponse;
import vn.truongngo.apartcom.one.service.admin.application.user.find_by_identity.GetUserByIdentityHandler;
import vn.truongngo.apartcom.one.service.admin.application.user.find_by_identity.GetUserByIdentityQuery;
import vn.truongngo.apartcom.one.service.admin.application.user.find_by_identity.UserView;
import vn.truongngo.apartcom.one.service.admin.application.user.social_register.SocialRegisterUser;

import java.util.Set;
import java.util.stream.Collectors;

// UC-U05: Tìm user theo email/username — dành cho oauth2 service
@RestController
@RequestMapping("/api/v1/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final GetUserByIdentityHandler getUserByIdentityHandler;
    private final SocialRegisterUser.Handler socialRegisterUserHandler;

    // UC-003: Tạo hoặc tìm user từ social login
    @PostMapping("/social")
    public ResponseEntity<SocialRegisterResponse> socialRegister(
            @RequestBody SocialRegisterRequest request) {
        SocialRegisterUser.Result result = socialRegisterUserHandler.handle(
                new SocialRegisterUser.Command(
                        request.provider(),
                        request.providerUserId(),
                        request.providerEmail()
                )
        );
        SocialRegisterResponse response = new SocialRegisterResponse(
                result.userId(), result.username(), result.requiresProfileCompletion());
        HttpStatus status = result.requiresProfileCompletion() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/identity")
    public ResponseEntity<UserIdentityResponse> getIdentity(@RequestParam String value) {
        UserView view = getUserByIdentityHandler.handle(new GetUserByIdentityQuery(value));

        Set<String> roleNames = view.roles().stream()
                .map(UserView.RoleView::name)
                .collect(Collectors.toSet());

        UserIdentityResponse response = new UserIdentityResponse(
                view.userId(),
                view.username(),
                view.email(),
                view.phoneNumber(),
                view.hashedPassword(),
                view.status().name(),
                roleNames
        );

        return ResponseEntity.ok(response);
    }
}
