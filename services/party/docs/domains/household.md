# Domain: Household

## Mô tả

Aggregate Root đại diện cho **hộ gia đình** — nhóm không chính thức (informal group), tương đương hộ khẩu trong bối cảnh Việt Nam. Không có tư cách pháp nhân, không đăng ký kinh doanh. Household share `PartyId` với `Party` AR (composition + shared ID). Mọi thao tác tạo Household phải tạo cả `Party` lẫn `Household` trong cùng một transaction tại application layer.

---

## Trách nhiệm
- Lưu giữ thông tin hộ gia đình và chủ hộ (`headPersonId`).
- Là đơn vị gắn kết với `OccupancyAgreement` (property-service) cho trường hợp cư trú.

## Không thuộc trách nhiệm
- Không quản lý danh sách thành viên trực tiếp — membership được quản lý qua `PartyRelationship` aggregate (Phase 2).
- Không quản lý PartyIdentification — thuộc về `Party` AR.
- Không biết về FixedAsset, OccupancyAgreement, hay RoleContext.

---

## Cấu trúc Aggregate

```
Household
├── PartyId          (Value Object — shared với Party AR, đồng thời là PK)
└── headPersonId     (PartyId — reference đến Person AR)
```

> **Không có createdAt/updatedAt riêng** — sử dụng từ `Party` AR.

---

## Value Objects

*(Không có Value Object riêng — PartyId được định nghĩa trong Party domain.)*

---

## Trạng thái

Household không có status riêng — status được kế thừa từ `Party.status` (ACTIVE / INACTIVE). Deactivate Household = deactivate Party tương ứng.

---

## Hành vi

| Hành vi          | Điều kiện                                  | Mô tả                                                               |
|------------------|--------------------------------------------|---------------------------------------------------------------------|
| `changeHead`     | Party.status = ACTIVE; Person phải tồn tại | Thay đổi chủ hộ — validate Person là member của Household (Phase 2) |

> `changeHead` ở Phase 1 chỉ validate Person tồn tại. Validate "phải là member" được enforce ở Phase 2 khi có `PartyRelationship`.

---

## Invariants
- `PartyId` immutable — không thể thay đổi sau khi tạo.
- `headPersonId` phải trỏ đến một `Person` tồn tại và có `Party.status = ACTIVE`.
- `headPersonId` phải là member của Household này (enforce tại application layer khi Phase 2 có `PartyRelationship`).
- Household không tồn tại độc lập — phải có Party record với cùng ID và `type = HOUSEHOLD`.

---

## Events

*(Household không phát event trực tiếp — `HouseholdCreatedEvent` được phát từ application layer sau khi tạo Party + Household atomic.)*

---

## Error Codes

| Code                         | HTTP | Mô tả                                                     |
|------------------------------|------|-----------------------------------------------------------|
| `HOUSEHOLD_NOT_FOUND`        | 404  | Không tìm thấy Household với partyId đã cho               |
| `HOUSEHOLD_ALREADY_EXISTS`   | 409  | Household đã tồn tại cho partyId này (internal guard)     |
| `HEAD_PERSON_NOT_FOUND`      | 404  | headPersonId trỏ đến Person không tồn tại                 |
| `HEAD_NOT_MEMBER`            | 422  | headPersonId không phải member của Household này (Phase 2)|

---

## Quan hệ

| Domain              | Quan hệ                                                                   |
|---------------------|---------------------------------------------------------------------------|
| `Party`             | Share `PartyId` — tạo atomic, deactivate qua Party                        |
| `Person`            | `headPersonId` reference đến Person AR                                    |
| `PartyRelationship` | Household là `to_party` trong MEMBER_OF relationship (Phase 2)            |

---

## Tham khảo
- [Domain: Party](party.md)
- [Domain: Person](person.md)
- [Party Service Design](../../../../docs/development/260416_01_design_party_model/01_party_service.md)
