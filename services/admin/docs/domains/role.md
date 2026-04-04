# Domain: Role

## Mô tả
Aggregate Root quản lý role trong hệ thống. Role được gán cho User để phân quyền tại Resource Server.

---

## Trách nhiệm
- Quản lý vòng đời của role (tạo, cập nhật, xóa).
- Đảm bảo tính toàn vẹn khi xóa role đang được sử dụng.

## Không thuộc trách nhiệm
- Không enforce permission — đó là trách nhiệm của Resource Server.
- Không biết user nào đang dùng role — query ngược qua User aggregate.

---

## Cấu trúc Aggregate

```
Role
├── RoleId          (Value Object — typed UUID)
├── RoleName        (Value Object — unique, immutable)
├── Description     (String — optional)
└── Auditable       (createdAt, updatedAt, createdBy, updatedBy)
```

---

## Value Objects

### RoleName
- Unique trong hệ thống.
- Immutable sau khi tạo — các service khác có thể dùng để reference.
- Convention: UPPER_SNAKE_CASE (ví dụ: `MANAGER`, `CONTENT_EDITOR`).

---

## Hành vi

| Hành vi             | Điều kiện | Mô tả                                                           |
|---------------------|-----------|-----------------------------------------------------------------|
| `create`            | —         | Tạo role mới                                                    |
| `updateDescription` | —         | Cập nhật description — `name` là immutable, không được thay đổi |
| `delete`            | —         | Hard delete — phải kiểm tra không có user nào đang dùng         |

---

## Invariants
- `name` là immutable sau khi tạo.
- Không được xóa role đang được gán cho user.

---

## Events

Role không phát sinh domain event ở giai đoạn hiện tại — không có downstream consumer cần notify.

---

## Error Codes

| Code                  | HTTP | Mô tả                                   |
|-----------------------|------|-----------------------------------------|
| `ROLE_NOT_FOUND`      | 404  |                                         |
| `ROLE_ALREADY_EXISTS` | 409  | Role name đã tồn tại                    |
| `ROLE_NAME_IMMUTABLE` | 422  | Không cho đổi tên role sau khi tạo      |
| `ROLE_IN_USE`         | 409  | Không xóa role đang được gán cho user   |

---

## Quan hệ

| Domain | Quan hệ                                                        |
|--------|----------------------------------------------------------------|
| User   | User giữ `List<RoleId>` — Role không biết mình được gán cho ai |

---

## Tham khảo
- [Glossary](../glossary.md)
- [UC Index](../use-cases/UC-000_index.md)