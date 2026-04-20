# Domain: Person

## Mô tả

Aggregate Root đại diện cho **cá nhân** trong hệ thống. Person share `PartyId` với `Party` AR (composition + shared ID) — không phải inheritance. Mọi thao tác tạo Person phải tạo cả `Party` lẫn `Person` trong cùng một transaction tại application layer.

---

## Trách nhiệm
- Lưu giữ thông tin cá nhân (họ tên, ngày sinh, giới tính).
- Là subtype của Party — identity core nằm ở Party AR.

## Không thuộc trách nhiệm
- Không quản lý PartyIdentification — thuộc về `Party` AR.
- Không biết về membership hay employment — thuộc về `PartyRelationship` / `Employment` aggregate.
- Không biết về User (admin-service) hay RoleContext.

---

## Cấu trúc Aggregate

```
Person
├── PartyId        (Value Object — shared với Party AR, đồng thời là PK)
├── firstName      (String)
├── lastName       (String)
├── dob            (LocalDate — optional)
└── gender         (Gender enum: MALE, FEMALE, OTHER — optional)
```

> **Không có createdAt/updatedAt riêng** — sử dụng từ `Party` AR.

---

## Value Objects

*(Không có Value Object riêng — PartyId được định nghĩa trong Party domain.)*

---

## Trạng thái

Person không có status riêng — status được kế thừa từ `Party.status` (ACTIVE / INACTIVE). Deactivate Person = deactivate Party tương ứng.

---

## Hành vi

| Hành vi          | Điều kiện                       | Mô tả                                          |
|------------------|---------------------------------|------------------------------------------------|
| `updateProfile`  | Party.status = ACTIVE           | Cập nhật firstName, lastName, dob, gender      |

---

## Invariants
- `PartyId` immutable — không thể thay đổi sau khi tạo.
- `firstName` và `lastName` không được rỗng.
- Person không tồn tại độc lập — phải có Party record với cùng ID và `type = PERSON`.

---

## Events

*(Person không phát event trực tiếp — `PersonCreatedEvent` được phát từ application layer sau khi tạo Party + Person atomic.)*

---

## Error Codes

| Code                   | HTTP | Mô tả                                              |
|------------------------|------|----------------------------------------------------|
| `PERSON_NOT_FOUND`     | 404  | Không tìm thấy Person với partyId đã cho           |
| `PERSON_ALREADY_EXISTS`| 409  | Person đã tồn tại cho partyId này (internal guard) |

---

## Quan hệ

| Domain          | Quan hệ                                                               |
|-----------------|-----------------------------------------------------------------------|
| `Party`         | Share `PartyId` — tạo atomic, deactivate qua Party                    |
| `Household`     | Person có thể là head của Household (reference từ Household AR)        |
| `PartyRelationship` | Person có thể là `from_party` trong MEMBER_OF hoặc EMPLOYED_BY  |

---

## Tham khảo
- [Domain: Party](party.md)
- [Party Service Design](../../../../docs/development/260416_01_design_party_model/01_party_service.md)
