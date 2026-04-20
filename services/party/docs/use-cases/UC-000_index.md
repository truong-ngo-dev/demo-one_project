# Use Case Index — Party Service

---

## Phase 1 — Party & Subtypes

### Party Creation

| ID     | Tên              | Mô tả ngắn                                                           | Trạng thái  | File                          |
|--------|------------------|----------------------------------------------------------------------|-------------|-------------------------------|
| UC-001 | Tạo Person       | BQL_MANAGER tạo Party(PERSON) + Person record trong cùng transaction | Implemented | UC-001_create_person.md       |
| UC-002 | Tạo Organization | BQL_MANAGER tạo Party(ORGANIZATION) + Organization record            | Implemented | UC-002_create_organization.md |
| UC-003 | Tạo Household    | BQL_MANAGER tạo Party(HOUSEHOLD) + Household record, chỉ định chủ hộ | Implemented | UC-003_create_household.md    |

### Party Identification

| ID     | Tên                    | Mô tả ngắn                                                                | Trạng thái  | File                            |
|--------|------------------------|---------------------------------------------------------------------------|-------------|---------------------------------|
| UC-004 | Thêm định danh pháp lý | BQL_MANAGER thêm PartyIdentification (CCCD, hộ chiếu, MST, ...) vào Party | Implemented | UC-004_add_identification.md    |
| UC-005 | Xoá định danh pháp lý  | BQL_MANAGER xoá PartyIdentification theo id                               | Not started | UC-005_remove_identification.md |

### Party Query

| ID     | Tên               | Mô tả ngắn                                                 | Trạng thái  | File                       |
|--------|-------------------|------------------------------------------------------------|-------------|----------------------------|
| UC-006 | Tìm Party theo ID | Query Party (+ subtype data) theo PartyId                  | Implemented | UC-006_find_party_by_id.md |
| UC-007 | Search Parties    | Tìm kiếm và filter danh sách Party theo type, name, status | Implemented | UC-007_search_parties.md   |

### Internal API

| ID     | Tên                          | Mô tả ngắn                                                                         | Trạng thái  | File                           |
|--------|------------------------------|------------------------------------------------------------------------------------|-------------|--------------------------------|
| UC-008 | Get Party info (internal)    | `GET /internal/parties/{id}` — admin/property service lấy basic info               | Implemented | UC-008_internal_get_party.md   |
| UC-009 | Get Party members (internal) | `GET /internal/parties/{id}/members` — admin-service lấy members của Household/Org | Implemented | UC-009_internal_get_members.md |

---

## Phase 2 — PartyRelationship

| ID     | Tên                          | Mô tả ngắn                                                                   | Trạng thái  | File                         |
|--------|------------------------------|------------------------------------------------------------------------------|-------------|------------------------------|
| UC-010 | Thêm thành viên              | Tạo PartyRelationship(MEMBER_OF): Person → Household hoặc Person → TenantOrg | Implemented | UC-010_add_member.md         |
| UC-011 | Xoá thành viên               | Kết thúc PartyRelationship(MEMBER_OF): set end_date + status=ENDED           | Implemented | UC-011_remove_member.md      |
| UC-012 | Tìm relationships theo Party | Query danh sách PartyRelationship theo fromPartyId hoặc toPartyId            | Implemented | UC-012_find_relationships.md |

---

## Phase 3 — Employment

| ID     | Tên                        | Mô tả ngắn                                                                   | Trạng thái  | File                                |
|--------|----------------------------|------------------------------------------------------------------------------|-------------|-------------------------------------|
| UC-013 | Tạo Employment (BQL staff) | Tạo PartyRelationship(EMPLOYED_BY) + Employment atomic; validate orgType=BQL | Implemented | UC-013_create_employment.md         |
| UC-014 | Terminate Employment       | Employment.status=TERMINATED, set end_date — emit EmploymentTerminated       | Implemented | UC-014_terminate_employment.md      |
| UC-015 | Giao chức vụ               | Tạo PositionAssignment trên Employment đang ACTIVE; support kiêm nhiệm       | Implemented | UC-015_assign_position.md           |
| UC-016 | Tìm nhân sự theo Org       | Query Employment JOIN PositionAssignment WHERE org_id=BQL Org                | Implemented | UC-016_find_employments_by_org.md   |
| UC-017 | Tìm employment theo Person | Query Employment của một Person cụ thể                                       | Implemented | UC-017_find_employment_by_person.md |

---

## Ghi chú

| Trạng thái    | Ý nghĩa                                                  |
|---------------|----------------------------------------------------------|
| `Not started` | Chưa implement, nằm trong Phase 1 scope                  |
| `Planned`     | Chưa implement, Phase 2 / Phase 3 — không tự ý implement |
| `Implemented` | Đã implement và có test                                  |
