package vn.truongngo.apartcom.one.service.admin.domain.user;

import lombok.Getter;
import vn.truongngo.apartcom.one.lib.common.domain.exception.DomainException;
import vn.truongngo.apartcom.one.lib.common.domain.model.AbstractAggregateRoot;
import vn.truongngo.apartcom.one.lib.common.domain.model.AggregateRoot;
import vn.truongngo.apartcom.one.lib.common.utils.lang.Assert;
import vn.truongngo.apartcom.one.service.admin.domain.role.RoleId;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
public class User extends AbstractAggregateRoot<UserId> implements AggregateRoot<UserId> {

    private String username;              // nullable
    private final String email;           // nullable, immutable
    private String phoneNumber;           // nullable
    private String fullName;              // nullable
    private UserPassword password;        // nullable — social only user không có password
    private final Set<SocialConnection> socialConnections;
    private UserStatus status;
    private final Set<RoleId> roleIds;
    private boolean usernameChanged;      // false = đang dùng auto-generated, true = đã đổi 1 lần
    private Instant lockedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    User(UserId id,
         String username,
         String email,
         String phoneNumber,
         String fullName,
         UserPassword password,
         Set<RoleId> roleIds) {
        super(id);
        this.username          = username;
        this.email             = email;
        this.phoneNumber       = phoneNumber;
        this.fullName          = fullName;
        this.password          = password;
        this.socialConnections = new HashSet<>();
        this.status            = UserStatus.ACTIVE;
        this.roleIds           = new HashSet<>(roleIds);
        this.usernameChanged   = false;
        this.createdAt         = Instant.now();
        this.updatedAt         = this.createdAt;
    }

    private User(UserId id,
                 String username,
                 String email,
                 String phoneNumber,
                 String fullName,
                 UserPassword password,
                 Set<SocialConnection> socialConnections,
                 UserStatus status,
                 Set<RoleId> roleIds,
                 boolean usernameChanged,
                 Instant lockedAt,
                 Instant createdAt,
                 Instant updatedAt) {
        super(id);
        this.username          = username;
        this.email             = email;
        this.phoneNumber       = phoneNumber;
        this.fullName          = fullName;
        this.password          = password;
        this.socialConnections = new HashSet<>(socialConnections);
        this.status            = status;
        this.roleIds           = new HashSet<>(roleIds);
        this.usernameChanged   = usernameChanged;
        this.lockedAt          = lockedAt;
        this.createdAt         = createdAt;
        this.updatedAt         = updatedAt;
    }

    public static User reconstitute(UserId id,
                                    String username,
                                    String email,
                                    String phoneNumber,
                                    String fullName,
                                    UserPassword password,
                                    Set<SocialConnection> socialConnections,
                                    UserStatus status,
                                    Set<RoleId> roleIds,
                                    boolean usernameChanged,
                                    Instant lockedAt,
                                    Instant createdAt,
                                    Instant updatedAt) {
        return new User(id, username, email, phoneNumber, fullName, password,
                socialConnections, status, roleIds,
                usernameChanged, lockedAt, createdAt, updatedAt);
    }

    // ───────────── Profile ─────────────

    public void updateProfile(String username, String fullName, String phoneNumber) {
        assertActive();
        if (username != null) {
            if (this.usernameChanged) throw UserException.usernameAlreadyChanged();
            this.username        = username;
            this.usernameChanged = true;
        }
        if (fullName != null)    this.fullName    = fullName;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
        this.updatedAt = Instant.now();
    }

    // ───────────── Password ─────────────

    public void changePassword(UserPassword newPassword) {
        assertActive();
        Assert.notNull(newPassword, "newPassword is required");
        this.password  = newPassword;
        this.updatedAt = Instant.now();
        registerEvent(UserPasswordChangedEvent.of(this.getId().getValue()));
    }

    // ───────────── Social ─────────────

    public void connectSocial(String provider, String socialId, String email, Instant connectedAt) {
        if (socialConnections.stream().anyMatch(s -> s.matches(provider, socialId))) return;

        socialConnections.add(new SocialConnection(provider, socialId, email, connectedAt));
        this.updatedAt = Instant.now();
        registerEvent(SocialConnectedEvent.of(this.getId().getValue(), provider, socialId));
    }

    public boolean hasSocialConnection(String provider, String socialId) {
        return socialConnections.stream()
                .anyMatch(s -> s.matches(provider, socialId));
    }

    // ───────────── Status ─────────────

    public void lock() {
        if (isLocked()) return;
        this.status    = UserStatus.LOCKED;
        this.lockedAt  = Instant.now();
        this.updatedAt = this.lockedAt;
        registerEvent(new UserLockedEvent(this.getId().getValue(), this.lockedAt));
    }

    public void unlock() {
        assertLocked();
        this.status    = UserStatus.ACTIVE;
        this.lockedAt  = null;
        this.updatedAt = Instant.now();
        registerEvent(new UserUnlockedEvent(this.getId().getValue()));
    }

    // ───────────── Queries ─────────────

    public boolean isActive()    { return this.status == UserStatus.ACTIVE; }
    public boolean isLocked()    { return this.status == UserStatus.LOCKED; }
    public boolean hasPassword() { return this.password != null; }

    // ───────────── Roles ─────────────

    public void assignRoles(Set<RoleId> roleIdsToAdd) {
        this.roleIds.addAll(roleIdsToAdd);
        this.updatedAt = Instant.now();
    }

    public void removeRole(RoleId roleId) {
        this.roleIds.removeIf(r -> r.getValue().equals(roleId.getValue()));
        this.updatedAt = Instant.now();
    }

    // ───────────── Guards ─────────────

    private void assertActive() {
        if (!isActive()) throw new DomainException(UserErrorCode.USER_NOT_ACTIVE);
    }

    private void assertLocked() {
        if (!isLocked()) throw new DomainException(UserErrorCode.USER_NOT_LOCKED);
    }
}
