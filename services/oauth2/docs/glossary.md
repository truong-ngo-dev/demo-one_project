# Glossary — OAuth2 Service

Định nghĩa các thuật ngữ riêng của OAuth2 service. Cập nhật khi có khái niệm mới xuất hiện trong doc.

> Các thuật ngữ chung của hệ thống (token types, service descriptions) xem tại [Global Glossary](../../../docs/glossary.md).

---

## Authorization

### Authorization Record
Bản ghi lưu trạng thái của một lần cấp phép OAuth2, bao gồm tất cả token liên quan (access, refresh, id token) và metadata của phiên đó.
> Được quản lý bởi Spring Authorization Server, lưu tại DB.

---

## Session & Device

### OAuth Session
Phiên đăng nhập của user sau khi xác thực thành công, được tạo ra ở Phase 2. Lưu thông tin liên kết giữa user, device, và Authorization Record.
> Khác với Authorization Record — OAuth Session là domain object của service này, không phải của Spring.

### Device
Thiết bị mà user dùng để đăng nhập. Được identify bằng device fingerprint từ request. Một user có thể có nhiều device.

---

## Activity & Audit

### Login Activity
Bản ghi audit cho mỗi lần thử đăng nhập — cả thành công lẫn thất bại. Append-only, không sửa, không xóa.