# Domain: Activity (LoginActivity)

## Mô tả
Bản ghi audit cho mỗi lần thử đăng nhập — cả thành công lẫn thất bại. Immutable sau khi tạo, chỉ append, không sửa, không xóa.

---

## Trách nhiệm
- Ghi nhận kết quả của mỗi lần thử đăng nhập.
- Lưu đủ thông tin để correlate với Device và Session khi cần audit.

## Không thuộc trách nhiệm
- Không trigger bất kỳ hành vi nào — chỉ là audit log.
- Không quản lý session hay device.

---

## Thuộc tính

| Thuộc tính      | SUCCESS                          | FAILED                                    |
|-----------------|----------------------------------|-------------------------------------------|
| `id`            | Định danh duy nhất               | Định danh duy nhất                        |
| `userId`        | Có                               | Null — không xác định được user           |
| `username`      | Có — từ request param            | Có — từ request param                     |
| `result`        | `SUCCESS`                        | `WRONG_PASSWORD`, `USER_NOT_FOUND`, v.v.  |
| `ipAddress`     | Có                               | Có                                        |
| `userAgent`     | Có                               | Có                                        |
| `compositeHash` | Có — từ Device fingerprint       | Có — dùng để correlate với Device sau này |
| `deviceId`      | Có — FK đến Device               | Null — device chưa được tạo khi fail      |
| `sessionId`     | Có — định danh của OAuth Session | Null                                      |
| `provider`      | `LOCAL` / `GOOGLE` [PLANNED]     | `LOCAL` / `GOOGLE` [PLANNED]              |
| `createdAt`     | Thời điểm ghi nhận               | Thời điểm ghi nhận                        |

---

## Login Result

| Giá trị          | Mô tả                  |
|------------------|------------------------|
| `SUCCESS`        | Đăng nhập thành công   |
| `WRONG_PASSWORD` | Sai password           |
| `USER_NOT_FOUND` | Username không tồn tại |
| *(mở rộng)*      | Có thể thêm khi cần    |

---

## Login Provider

| Giá trị  | Mô tả                            |
|----------|----------------------------------|
| `LOCAL`  | Đăng nhập bằng username/password |
| `GOOGLE` | [PLANNED] Social Login           |

---

## Invariants
- Toàn bộ thuộc tính là immutable sau khi tạo — không có update, không có delete.
- `userId` có thể null khi login fail và username không tồn tại trên hệ thống.
- `deviceId` có giá trị ở luồng SUCCESS — null ở luồng FAILED vì Device chưa được tạo.
- `sessionId` chỉ có giá trị ở luồng SUCCESS — null ở luồng FAILED vì chưa có OAuth Session.
- `compositeHash` luôn có giá trị — dùng để correlate với Device dù login thành công hay thất bại.

---

## Quan hệ

| Domain  | Quan hệ                                                                         |
|---------|---------------------------------------------------------------------------------|
| Device  | `deviceId` trỏ trực tiếp về Device (nullable). `compositeHash` vẫn lưu để audit |
| Session | `sessionId` trỏ về định danh của OAuth Session                                  |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC-001b: Log Login Activity](../use-cases/UC-003_log_activity)
- [Authentication Flow](../flows/001_authentication_flow.md)