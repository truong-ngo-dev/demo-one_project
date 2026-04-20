# Domain: Organization

## Mô tả

Aggregate Root đại diện cho **pháp nhân** trong hệ thống — Ban Quản Lý (BQL), Tenant Org, Vendor, hoặc Other. Organization share `PartyId` với `Party` AR (composition + shared ID). Mọi thao tác tạo Organization phải tạo cả `Party` lẫn `Organization` trong cùng một transaction tại application layer.

---

## Trách nhiệm
- Lưu giữ thông tin pháp nhân (loại tổ chức, mã số thuế, số đăng ký kinh doanh).
- Phân loại tổ chức theo `OrgType` để các aggregate khác (Employment, RoleContext) sử dụng làm điều kiện nghiệp vụ.

## Không thuộc trách nhiệm
- Không quản lý PartyIdentification — thuộc về `Party` AR.
- Không biết về membership hay employment — thuộc về `PartyRelationship` / `Employment` aggregate.
- Không biết về RoleContext (admin-service) hay FixedAsset (property-service).

---

## Cấu trúc Aggregate

```
Organization
├── PartyId          (Value Object — shared với Party AR, đồng thời là PK)
├── OrgType          (enum: BQL, TENANT, VENDOR, OTHER)
├── taxId            (String — optional, mã số thuế)
└── registrationNo   (String — optional, số đăng ký kinh doanh)
```

> **Không có createdAt/updatedAt riêng** — sử dụng từ `Party` AR.

---

## Value Objects

*(Không có Value Object riêng — PartyId được định nghĩa trong Party domain.)*

---

## Enums

### OrgType

| Value    | Ý nghĩa                                                          |
|----------|------------------------------------------------------------------|
| `BQL`    | Ban Quản Lý — tổ chức vận hành tòa nhà, có Employment lifecycle |
| `TENANT` | Tenant Org — công ty thuê mặt bằng (LEASE agreement)            |
| `VENDOR` | Nhà cung cấp dịch vụ, bảo trì                                    |
| `OTHER`  | Tổ chức khác không phân loại                                     |

> **Chỉ `BQL` Org mới có Employment lifecycle.** Hệ thống không quản lý HR của TENANT/VENDOR.

---

## Trạng thái

Organization không có status riêng — status được kế thừa từ `Party.status` (ACTIVE / INACTIVE). Deactivate Organization = deactivate Party tương ứng.

---

## Hành vi

| Hành vi          | Điều kiện                       | Mô tả                                                    |
|------------------|---------------------------------|----------------------------------------------------------|
| `updateInfo`     | Party.status = ACTIVE           | Cập nhật taxId, registrationNo (OrgType không đổi được) |

---

## Invariants
- `PartyId` immutable — không thể thay đổi sau khi tạo.
- `OrgType` immutable sau khi tạo — không chuyển BQL thành TENANT.
- Organization không tồn tại độc lập — phải có Party record với cùng ID và `type = ORGANIZATION`.
- `taxId` và `registrationNo` không bắt buộc, nhưng nếu có thì phải unique toàn hệ thống.

---

## Events

*(Organization không phát event trực tiếp — `OrganizationCreatedEvent` được phát từ application layer sau khi tạo Party + Organization atomic.)*

---

## Error Codes

| Code                          | HTTP | Mô tả                                                   |
|-------------------------------|------|---------------------------------------------------------|
| `ORGANIZATION_NOT_FOUND`      | 404  | Không tìm thấy Organization với partyId đã cho          |
| `ORGANIZATION_ALREADY_EXISTS` | 409  | Organization đã tồn tại cho partyId này (internal guard)|
| `INVALID_ORG_TYPE`            | 422  | OrgType không hợp lệ cho thao tác này                   |

---

## Quan hệ

| Domain          | Quan hệ                                                                        |
|-----------------|--------------------------------------------------------------------------------|
| `Party`         | Share `PartyId` — tạo atomic, deactivate qua Party                             |
| `Employment`    | Employment.orgId trỏ đến Organization (chỉ khi orgType = BQL)                 |
| `PartyRelationship` | Organization có thể là `to_party` trong MEMBER_OF hoặc EMPLOYED_BY       |

---

## Tham khảo
- [Domain: Party](party.md)
- [Party Service Design](../../../../docs/development/260416_01_design_party_model/01_party_service.md)
