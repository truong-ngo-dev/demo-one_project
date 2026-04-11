# UC-026: Subject Attribute Enrichment

## Tóm tắt
Mở rộng `AdminSubjectProvider` để load thêm user profile attributes vào `Subject.attributes` — cho phép SpEL conditions tham chiếu các thuộc tính của subject ngoài `roles`, ví dụ `department`, `tenantId`, `employeeType`.

## Actor
System (transparent — xảy ra khi `AdminSubjectProvider.getSubject()` được gọi)

## Trạng thái
Planned

---

## Bối cảnh

Phase 1 `AdminSubjectProvider` chỉ load `userId` + `roles`:

```java
Subject {
    userId: "abc-123",
    roles: ["MANAGER", "EMPLOYEE"],
    attributes: {}   ← luôn rỗng
}
```

Điều này giới hạn SpEL — chỉ có thể viết policy theo `roles`, không thể theo attribute người dùng:

```
// Phase 1: được
subject.roles.contains('MANAGER')

// Phase 1: KHÔNG được — attributes rỗng
subject.getAttribute('department') == resource.data.department
```

Phase 2 mở rộng `attributes` bằng cách map User profile fields vào Subject.

---

## UC-026-1: Cấu hình attribute mapping

**Config trong `application.yml`**:

```yaml
abac:
  subject:
    attribute-mappings:
      - source: user.fullName
        key: fullName
      - source: user.status
        key: status
      # Thêm các field khác khi User domain được mở rộng
```

**Hoặc hard-code trong Phase 2** (simpler — chỉ có ít field):

```java
subject.addAttribute("status", user.getStatus().name());
subject.addAttribute("fullName", user.getFullName());
```

---

## UC-026-2: Cập nhật AdminSubjectProvider

```java
@Override
public Subject getSubject(Principal principal) {
    Subject subject = new Subject();
    subject.setAttributes(new HashMap<>());

    if (principal == null) {
        subject.setRoles(List.of());
        return subject;
    }

    String userId = principal.getName();
    subject.setUserId(userId);

    userRepository.findById(UserId.of(userId)).ifPresentOrElse(
        user -> {
            // Roles
            List<String> roles = roleRepository.findAllByIds(user.getRoleIds()).stream()
                .map(Role::getName).toList();
            subject.setRoles(roles);

            // Attributes (Phase 2+)
            subject.addAttribute("status", user.getStatus().name());
            if (user.getFullName() != null) subject.addAttribute("fullName", user.getFullName());
        },
        () -> subject.setRoles(List.of())
    );

    return subject;
}
```

---

## UC-026-3: SpEL examples sau khi enrich

```
// Chỉ ACTIVE user được thao tác
subject.getAttribute('status') == 'ACTIVE'

// MANAGER chỉ đọc employee trong department của mình
subject.roles.contains('MANAGER') &&
subject.getAttribute('department') == resource.data.department
```

---

## Tương lai (Phase 3+)

Khi User domain mở rộng (thêm `department`, `employeeType`...), chỉ cần cập nhật `AdminSubjectProvider` — không cần thay đổi PdpEngine hay policy structure.

---

## Ghi chú thiết kế

- Attribute enrichment xảy ra mỗi request — không cache. Nếu performance là vấn đề → Phase 3 thêm cache với TTL ngắn (30s).
- Không expose sensitive attributes (password hash, token) vào Subject.
- `subject.attributes` là `Map<String, Object>` — SpEL có thể gọi method trên value nếu là complex type.
