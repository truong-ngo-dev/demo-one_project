# Domain: Session (UserSession)

## Mô tả
Phiên đăng nhập của user sau khi xác thực thành công. Lưu thông tin liên kết giữa user, device, và Authorization Record. Là source of truth cho trạng thái session trong hệ thống.

---

## Trách nhiệm
- Theo dõi trạng thái phiên đăng nhập của user trên từng device.
- Liên kết session với Authorization Record để phục vụ revocation.
- Phát sinh event khi session bị revoke.

## Không thuộc trách nhiệm
- Không tự revoke Authorization Record — đó là trách nhiệm của Authorization Server.
- Không quản lý thông tin thiết bị — đó là trách nhiệm của Device domain.

---

## Thuộc tính

| Thuộc tính        | Mô tả                                                               |
|-------------------|---------------------------------------------------------------------|
| `id`              | Định danh duy nhất của session                                      |
| `userId`          | Reference đến user sở hữu session — immutable                       |
| `deviceId`        | Reference đến device đăng nhập — immutable                          |
| `idpSessionId`    | ID của HttpSession tại IdP (port 9000) — dùng để revoke IdP session |
| `authorizationId` | Primary key của Authorization Record — dùng để revoke token khi cần |
| `ipAddress`       | IP address tại thời điểm tạo session — immutable                    |
| `status`          | Trạng thái hiện tại của session                                     |
| `createdAt`       | Thời điểm session được tạo — immutable                              |

> `authorizationId` là cầu nối giữa Session domain và Authorization Record — không thay đổi sau khi tạo.

---

## Trạng thái (SessionStatus)

```
[Chưa tồn tại]
      │
      │ user đăng nhập thành công (Phase 2)
      ▼
   [ACTIVE]
      │         │
      │ revoke  │ expire
      ▼         ▼
  [REVOKED]  [EXPIRED]
```

| Trạng thái          | Mô tả                                                           |
|---------------------|-----------------------------------------------------------------|
| `ACTIVE`            | Session đang hoạt động bình thường                              |
| `REVOKED`           | Session bị revoke chủ động — user đăng xuất từ xa               |
| `EXPIRED` [PLANNED] | Session hết hạn tự nhiên — cần trigger từ bên ngoài để cập nhật |

---

## Hành vi

| Hành vi            | Điều kiện       | Mô tả                                                                                                        |
|--------------------|-----------------|--------------------------------------------------------------------------------------------------------------|
| `create`           | —               | Tạo session mới sau khi token được phát hành thành công                                                      |
| `revoke`           | status = ACTIVE | Revoke chủ động — phát sinh `SessionRevokedEvent`                                                            |
| `expire` [PLANNED] | status = ACTIVE | Đánh dấu hết hạn khi Authorization Record expire — cần trigger từ bên ngoài (background job hoặc lazy check) |

---

## Domain Service

| Service                    | Mô tả                                                                                          |
|----------------------------|------------------------------------------------------------------------------------------------|
| `SessionTerminationService` | Port — thực hiện dọn dẹp toàn diện tại IdP khi một session bị thu hồi: hủy Authorization Record (tokens) và xóa local IdP Session (Servlet session). |

---

## Invariants
- `userId`, `deviceId`, `authorizationId`, `ipAddress`, `createdAt` là immutable sau khi tạo.
- Chỉ session có `status = ACTIVE` mới thực hiện được các hành vi.

---

## Events

| Event                 | Trigger    | Consumer                                                         |
|-----------------------|------------|------------------------------------------------------------------|
| `SessionRevokedEvent` | `revoke()` | Application layer — trigger notify Web Gateway xóa Redis session |

---

## Quan hệ

| Domain               | Quan hệ                                                |
|----------------------|--------------------------------------------------------|
| Device               | Một Device có thể liên kết với nhiều Session           |
| Authorization Record | Một Session liên kết với đúng một Authorization Record |

---

## Events

| Event                 | Trigger    | Consumer                                             |
|-----------------------|------------|------------------------------------------------------|
| `SessionRevokedEvent` | `revoke()` | Application layer — notify Gateway xóa Redis session |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC-001c: Create OAuth Session](../use-cases/UC-004_create_session.md)
- [UC-002: Logout](../use-cases/UC-005_logout.md)
- [Authentication Flow](../flows/001_authentication_flow.md)