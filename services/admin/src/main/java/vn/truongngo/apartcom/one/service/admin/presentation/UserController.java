package vn.truongngo.apartcom.one.service.admin.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import vn.truongngo.apartcom.one.service.admin.application.user.admin_create.AdminCreateUser;
import vn.truongngo.apartcom.one.service.admin.application.user.register.RegisterUser;
import vn.truongngo.apartcom.one.service.admin.application.user.assign_roles.AssignRoles;
import vn.truongngo.apartcom.one.service.admin.application.user.change_password.ChangePassword;
import vn.truongngo.apartcom.one.service.admin.application.user.find_by_id.FindUserById;
import vn.truongngo.apartcom.one.service.admin.application.user.get_my_profile.GetMyProfile;
import vn.truongngo.apartcom.one.service.admin.application.user.lock.LockUser;
import vn.truongngo.apartcom.one.service.admin.application.user.remove_role.RemoveRole;
import vn.truongngo.apartcom.one.service.admin.application.user.search.SearchUsers;
import vn.truongngo.apartcom.one.service.admin.application.user.unlock.UnlockUser;
import vn.truongngo.apartcom.one.service.admin.application.user.update_profile.UpdateProfile;
import vn.truongngo.apartcom.one.service.admin.presentation.base.ApiResponse;
import vn.truongngo.apartcom.one.service.admin.presentation.base.PagedApiResponse;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUser.Handler registerUserHandler;
    private final AdminCreateUser.Handler adminCreateUserHandler;
    private final FindUserById.Handler findUserByIdHandler;
    private final SearchUsers.Handler searchUsersHandler;
    private final LockUser.Handler lockUserHandler;
    private final UnlockUser.Handler unlockUserHandler;
    private final AssignRoles.Handler assignRolesHandler;
    private final RemoveRole.Handler removeRoleHandler;
    private final GetMyProfile.Handler getMyProfileHandler;
    private final UpdateProfile.Handler updateProfileHandler;
    private final ChangePassword.Handler changePasswordHandler;

    // UC-002: User tự đăng ký
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterUser.Result>> register(
            @RequestBody RegisterUserRequest request) {
        RegisterUser.Result result = registerUserHandler.handle(
                new RegisterUser.Command(
                        request.email(),
                        request.username(),
                        request.password(),
                        request.fullName()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    // UC-001: Admin tạo user
    @PostMapping
    public ResponseEntity<ApiResponse<AdminCreateUser.Result>> createUser(
            @RequestBody CreateUserRequest request) {
        AdminCreateUser.Result result = adminCreateUserHandler.handle(
                new AdminCreateUser.Command(
                        request.email(),
                        request.username(),
                        request.fullName(),
                        request.roleIds()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(result));
    }

    // UC-004: Tìm user theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FindUserById.UserDetail>> getUserById(@PathVariable String id) {
        FindUserById.UserDetail detail = findUserByIdHandler.handle(new FindUserById.Query(id));
        return ResponseEntity.ok(ApiResponse.of(detail));
    }

    // UC-006: Search users
    @GetMapping
    public ResponseEntity<PagedApiResponse<SearchUsers.UserSummary>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roleId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort) {
        Page<SearchUsers.UserSummary> result = searchUsersHandler.handle(
                SearchUsers.Query.of(keyword, status, roleId, page, size, sort));
        return ResponseEntity.ok(PagedApiResponse.of(result));
    }

    // UC-007: Lock user
    @PostMapping("/{id}/lock")
    public ResponseEntity<Void> lockUser(@PathVariable String id) {
        lockUserHandler.handle(new LockUser.Command(id));
        return ResponseEntity.noContent().build();
    }

    // UC-007: Unlock user
    @PostMapping("/{id}/unlock")
    public ResponseEntity<Void> unlockUser(@PathVariable String id) {
        unlockUserHandler.handle(new UnlockUser.Command(id));
        return ResponseEntity.noContent().build();
    }

    // UC-013: Gán role cho user
    @PostMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoles(
            @PathVariable String id,
            @RequestBody AssignRolesRequest request) {
        assignRolesHandler.handle(new AssignRoles.Command(id, request.roleIds()));
        return ResponseEntity.noContent().build();
    }

    // UC-014: Gỡ role khỏi user
    @DeleteMapping("/{id}/roles/{roleId}")
    public ResponseEntity<Void> removeRole(
            @PathVariable String id,
            @PathVariable String roleId) {
        removeRoleHandler.handle(new RemoveRole.Command(id, roleId));
        return ResponseEntity.noContent().build();
    }

    // GET /me: Lấy profile của user hiện tại
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetMyProfile.Result>> getMyProfile(
            JwtAuthenticationToken authentication) {
        String userId = authentication.getToken().getSubject();
        GetMyProfile.Result result = getMyProfileHandler.handle(new GetMyProfile.Query(userId));
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    // UC-016: Cập nhật profile
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UpdateProfile.Result>> updateProfile(
            JwtAuthenticationToken authentication,
            @RequestBody UpdateProfileRequest request) {
        String userId = authentication.getToken().getSubject();
        UpdateProfile.Result result = updateProfileHandler.handle(
                new UpdateProfile.Command(userId, request.username(), request.fullName(), request.phoneNumber())
        );
        return ResponseEntity.ok(ApiResponse.of(result));
    }

    // UC-017: Đổi password
    @PostMapping("/me/password")
    public ResponseEntity<ApiResponse<ChangePassword.Result>> changePassword(
            JwtAuthenticationToken authentication,
            @RequestBody ChangePasswordRequest request) {
        String userId = authentication.getToken().getSubject();
        ChangePassword.Result result = changePasswordHandler.handle(
                new ChangePassword.Command(userId, request.currentPassword(), request.newPassword())
        );
        return ResponseEntity.ok(ApiResponse.of(result));
    }
}
