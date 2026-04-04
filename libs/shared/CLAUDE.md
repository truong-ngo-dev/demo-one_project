# libs/shared

Thư viện chứa types và DTOs dùng chung giữa các services.
**Chỉ đặt vào đây những gì thực sự cần share giữa nhiều service.**

Base package: `vn.truongngo.apartcom.one.lib.shared`

---

## Hiện có

| Class                   | Package      | Mô tả                                                                                    |
|-------------------------|--------------|------------------------------------------------------------------------------------------|
| `UserId`                | `domain.user` | Typed ID của User — dùng khi service khác cần reference đến User                        |
| `UserIdentityResponse`  | `dto.user`   | Response DTO cho internal identity endpoint — admin-service produces, oauth2 consumes    |
| `SocialRegisterResponse`| `dto.user`   | Response DTO cho internal social register endpoint — admin-service produces, oauth2 consumes |

---

## Quy tắc

1. **Không đặt business logic** vào lib này — chỉ chứa types, DTOs, constants
2. **Không đặt vào đây** những gì chỉ 1 service dùng — giữ trong service đó
3. Khi thêm type mới → confirm rằng ít nhất 2 service cần dùng
4. **Không sửa** lib này khi đang làm việc trong service — đây là shared code