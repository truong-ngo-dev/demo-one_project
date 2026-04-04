# Use Case Index

## User Management

| ID     | Tên                               | Mô tả ngắn                                                                     | Trạng thái  | File                                                                  |
|--------|-----------------------------------|--------------------------------------------------------------------------------|-------------|-----------------------------------------------------------------------|
| UC-001 | Admin tạo user                    | Admin tạo tài khoản user mới                                                   | Implemented | [UC-001_admin_create_user.md](UC-001_admin_create_user.md)            |
| UC-002 | User tự đăng ký                   | User tự tạo tài khoản                                                          | Implemented | [UC-002_register.md](UC-002_register.md)                              |
| UC-003 | Xác thực Social Login             | Handler thông tin xác thực social sau khi đăng nhập (internal)                 | Modify      | [UC-003_post_social_auth_handler.md](UC-003_post_social_auth_handler) |
| UC-004 | Tìm user theo ID                  | Query user theo UserId                                                         | Implemented | [UC-004_get_user.md](UC-004_get_user.md)                              |
| UC-005 | Tìm user theo identity (internal) | oauth2 service dùng để xác thực credentials                                    | Implemented | [UC-005_get_identity.md](UC-005_get_identity.md)                      |
| UC-006 | Search user                       | Admin tìm kiếm và filter danh sách user                                        | Implemented | [UC-006_search_user.md](UC-006_search_user.md)                        |
| UC-007 | Lock / Unlock user                | Admin khoá hoặc mở khoá tài khoản                                              | Implemented | [UC-007_lock_unlock_user.md](UC-007_lock_unlock_user.md)              |
| UC-016 | Update profile                    | Cập nhật thông tin cá nhân — bao gồm lần đầu complete profile sau social login | Implemented | [UC-016_update_profile.md](UC-016_update_profile.md)                  |
| UC-017 | Change password                   | User tự đổi hoặc tạo password                                                  | Implemented | [UC-017_change_password.md](UC-017_change_password.md)                |
| UC-018 | Delete user                       | Soft delete tài khoản                                                          |             | [UC-018_delete_user.md](UC-018_delete_user.md)                        |

---

## Role Management

| ID     | Tên               | Mô tả ngắn                                   | Status      | File                                             |
|--------|-------------------|----------------------------------------------|-------------|--------------------------------------------------|
| UC-008 | Tạo role          | Admin tạo role mới                           | Implemented | [UC-008_create_role.md](UC-008_create_role.md)   |
| UC-009 | Tìm role theo ID  | Query role theo RoleId                       | Implemented | [UC-009_get_role.md](UC-009_get_role.md)         |
| UC-010 | Danh sách roles   | Admin xem và tìm kiếm danh sách role         | Implemented | [UC-010_list_roles.md](UC-010_list_roles.md)     |
| UC-011 | Cập nhật role     | Admin cập nhật description của role          | Implemented | [UC-011_update_role.md](UC-011_update_role.md)   |
| UC-012 | Xóa role          | Admin xóa role — kiểm tra không có user dùng | Implemented | [UC-012_delete_role.md](UC-012_delete_role.md)   |
| UC-013 | Gán role cho user | Admin gán một hoặc nhiều role cho user       | Implemented | [UC-013_assign_roles.md](UC-013_assign_roles.md) |
| UC-014 | Gỡ role khỏi user | Admin gỡ role khỏi user                      | Implemented | [UC-014_remove_role.md](UC-014_remove_role.md)   |

---

## [PLANNED]

| ID     | Tên                      | Mô tả ngắn                     |
|--------|--------------------------|--------------------------------|
| UC-015 | Verify email             | Xác thực email sau khi đăng ký |
| UC-019 | Policy management (ABAC) | Quản lý policy phân quyền      |
| UC-020 | MFA config               | Cấu hình xác thực đa yếu tố    |