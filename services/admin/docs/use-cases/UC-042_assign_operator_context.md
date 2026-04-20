# UC-042 — Assign Operator Context

## Mô tả

SUPER_ADMIN hoặc BQL_MANAGER gán OPERATOR context cho một User tại một building cụ thể.
User phải đã có `partyId` được liên kết (UC-041). Building phải tồn tại trong `building_reference`.
Tất cả roles được gán phải có `scope = OPERATOR` (B6).

---

## Actors

- **SUPER_ADMIN** — gán operator cho bất kỳ building nào
- **BQL_MANAGER** — gán operator trong building của mình

---

## Preconditions

- `userId` tồn tại trong hệ thống
- `user.partyId != null` — B2
- `buildingId` tồn tại trong `building_reference` — B3
- User chưa có OPERATOR context tại `buildingId` (nếu đã có → lỗi `ROLE_CONTEXT_ALREADY_EXISTS`)

---

## Business Rules

| Rule | Kiểm tra tại      | Mô tả                                                                                 |
|------|-------------------|---------------------------------------------------------------------------------------|
| B2   | Application layer | `user.getPartyId() != null` → throw `PARTY_ID_REQUIRED`                               |
| B3   | Application layer | `buildingReferenceRepository.existsById(buildingId)` → throw nếu không tồn tại        |
| B6   | Application layer | Tất cả roleIds phải thuộc `scope = OPERATOR` — load từ RoleRepository, check mỗi role |

---

## Flow

```
1. userRepository.findById(userId)         → throw USER_NOT_FOUND nếu không có
2. Check B2: user.getPartyId() != null     → throw PARTY_ID_REQUIRED nếu null
3. Check B3: buildingRefRepo.existsById(buildingId) → throw BUILDING_NOT_FOUND nếu false
4. [Optional] Load roles từ roleRepository.findAllByIds(roleIds)
   → Check count khớp → throw ROLE_NOT_FOUND nếu thiếu
   → Mỗi role phải có scope == OPERATOR → throw ROLE_SCOPE_MISMATCH nếu sai
5. user.addRoleContext(OPERATOR, buildingId, FIXED_ASSET, roleIds)
   → domain throw ROLE_CONTEXT_ALREADY_EXISTS nếu đã tồn tại
6. userRepository.save(user)
```

---

## Error Codes cần thêm

| Enum                    | Code  | HTTP | Message                                              |
|-------------------------|-------|------|------------------------------------------------------|
| `BUILDING_NOT_FOUND`    | 10019 | 404  | Building not found in reference cache                |
| `ROLE_SCOPE_MISMATCH`   | 10020 | 422  | Role scope does not match the target context scope   |

> Thêm vào `UserErrorCode.java`

---

## API

```
POST /api/v1/operators/{buildingId}/assign
Body: { userId, roleIds[] }
Response: 200 { data: null }
```
