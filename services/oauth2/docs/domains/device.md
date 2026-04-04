# Domain: Device

## Mô tả
Thiết bị mà user dùng để đăng nhập. Một user có thể có nhiều device. Device được identify bằng fingerprint tổng hợp từ nhiều tín hiệu của request.

---

## Trách nhiệm
- Lưu trữ thông tin thiết bị của user.
- Theo dõi trạng thái và lần sử dụng gần nhất của từng device.
- Quản lý trạng thái trusted/revoked của device.

## Không thuộc trách nhiệm
- Không quản lý session — đó là trách nhiệm của Session domain.
- Không xác thực user — đó là trách nhiệm của Authorization Server.

---

## Thuộc tính

| Thuộc tính      | Mô tả                                                       |
|-----------------|-------------------------------------------------------------|
| `id`            | Định danh duy nhất của device                               |
| `userId`        | Reference đến user sở hữu device — immutable sau khi tạo    |
| `fingerprint`   | Value object định danh thiết bị — xem chi tiết bên dưới     |
| `name`          | Tên thiết bị — system detect từ User-Agent, có thể cập nhật |
| `trusted`       | Thiết bị có được tin tưởng không                            |
| `status`        | Trạng thái hiện tại của device                              |
| `registeredAt`  | Thời điểm device được đăng ký lần đầu — immutable           |
| `lastSeenAt`    | Thời điểm device được dùng gần nhất                         |
| `lastIpAddress` | IP address lần đăng nhập gần nhất                           |

---

## Device Fingerprint

Fingerprint là value object tổng hợp từ nhiều tín hiệu, dùng để nhận diện thiết bị qua các lần đăng nhập.

| Thành phần       | Nguồn                         | Mô tả                                                                          |
|------------------|-------------------------------|--------------------------------------------------------------------------------|
| `deviceHash`     | JavaScript phía client        | SHA-256 của canvas, screen resolution, timezone, hardwareConcurrency, language |
| `userAgent`      | Request header                | Thông tin browser và OS                                                        |
| `acceptLanguage` | Request header                | Ngôn ngữ của browser                                                           |
| `compositeHash`  | Tổng hợp từ 3 thành phần trên | SHA-256(`deviceHash '\|' userAgent '\|' acceptLanguage`) — dùng để so khớp     |

So khớp device dựa trên `compositeHash` — hai fingerprint giống nhau khi `compositeHash` bằng nhau.

`deviceHash` được tính bởi vanilla JS (SubtleCrypto SHA-256) trên login form và gửi qua hidden input `device_hash`.

### deviceHash theo provider

| Provider | Cách lấy deviceHash | Ghi chú |
|----------|---------------------|---------|
| `LOCAL`  | Tính trên login form (canvas + screen + timezone + hardwareConcurrency + language) → gửi qua hidden input `device_hash` | Có đủ JS context trước khi submit |
| `GOOGLE` | JS tính hash → `POST /login/device-hint` lưu vào HTTP session trước khi redirect sang Google → success handler đọc lại từ session (`pre_auth_device_hash`) | Session tồn tại xuyên suốt OAuth2 redirect flow |

Khi `deviceHash` không có (Google login chưa implement), `deviceHash = ""` → `compositeHash = SHA-256(""|userAgent|acceptLanguage)` — vẫn đủ để nhận diện device theo browser.

---

## Trạng thái (DeviceStatus)

```
[Chưa tồn tại]
      │
      │ user đăng nhập lần đầu trên thiết bị
      ▼
   [ACTIVE] ◀─── user đăng nhập lại (cập nhật lastSeenAt, IP)
      │
      │ revoke (user đăng xuất thiết bị từ xa)
      ▼
  [REVOKED] ──── không thể dùng để login cho đến khi đăng ký lại
```

---

## Hành vi

| Hành vi          | Điều kiện       | Mô tả                                                |
|------------------|-----------------|------------------------------------------------------|
| `register`       | —               | Đăng ký device mới lần đầu đăng nhập                 |
| `recordActivity` | status = ACTIVE | Cập nhật `lastSeenAt` và IP khi user đăng nhập lại   |
| `trust`          | status = ACTIVE | Đánh dấu device là trusted                           |
| `revoke`         | status = ACTIVE | Revoke device — thường từ xa, reset trusted về false |
| `updateName`     | status = ACTIVE | Cập nhật tên device khi system detect User-Agent mới |

---

## Invariants
- `userId` và `registeredAt` là immutable sau khi tạo.
- Một `userId` + `compositeHash` chỉ tương ứng với một Device duy nhất.
- Chỉ device có `status = ACTIVE` mới thực hiện được các hành vi.
- Chỉ trusted device mới có thể revoke device khác.

---

## Quan hệ

| Domain   | Quan hệ                                            |
|----------|----------------------------------------------------|
| Session  | Một Device có thể liên kết với nhiều OAuth Session |
| Activity | Một Device liên kết với nhiều Login Activity       |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC-001a: Save / Update Device](../use-cases/UC-002_save_update_device)