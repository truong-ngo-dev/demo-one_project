# Glossary — Admin Service

Định nghĩa các thuật ngữ riêng của Admin Service. Cập nhật khi có khái niệm mới xuất hiện trong doc.

> Các thuật ngữ chung của hệ thống xem tại [Global Glossary](../../../docs/glossary.md).

---

## User

### UserStatus
Trạng thái vòng đời của tài khoản user.

| Giá trị   | Mô tả                                                        |
|-----------|--------------------------------------------------------------|
| `PENDING` | Mới tạo, chưa verify email hoặc chưa complete profile        |
| `ACTIVE`  | Đang hoạt động bình thường                                   |
| `LOCKED`  | Bị khoá — do admin hoặc oauth2 service notify sau N lần fail |
| `DELETED` | Soft delete — [PLANNED]                                      |

### SocialConnection
Liên kết giữa User và tài khoản mạng xã hội bên ngoài (Google, GitHub, v.v.). Một user có thể có nhiều SocialConnection, mỗi provider một record.

### UserPassword
Value Object lưu password đã được hash (bcrypt). Null nếu user chỉ dùng social login — không bao giờ expose raw password ra ngoài domain.

### requiresProfileCompletion
Flag trả về khi tạo user từ social login. `true` nghĩa là user chưa hoàn thiện profile (username vẫn đang là auto-generated). Đây là hint để UI hiển thị nudge nhắc nhở — không block user sử dụng hệ thống.

### usernameChanged
Flag trên User aggregate — `false` khi username vẫn là auto-generated, `true` sau khi user tự chọn username. Khi `true`, username trở thành immutable — không cho đổi thêm lần nào nữa.

---

## Role

### RoleName
Tên định danh của role — unique, immutable sau khi tạo. Convention: `UPPER_SNAKE_CASE` (ví dụ: `MANAGER`, `CONTENT_EDITOR`).

---

## Internal

### UserIdentity
Response model dùng cho internal endpoint — chứa `hashedPassword` và `roles`. Chỉ trả về cho oauth2 service, không expose ra public endpoint.