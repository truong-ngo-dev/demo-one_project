# Use Case Index — Property Service

---

## Phase 1 — FixedAsset

| ID     | Tên                    | Mô tả ngắn                                                                        | Trạng thái  |
|--------|------------------------|------------------------------------------------------------------------------------|-------------|
| UC-001 | Tạo Building           | SUPER_ADMIN tạo FixedAsset(BUILDING) với managingOrgId → emit BuildingCreated      | Implemented |
| UC-002 | Tạo Floor              | BQL_MANAGER tạo FixedAsset(FLOOR), parentId = Building                             | Implemented |
| UC-003 | Tạo Unit               | BQL_MANAGER tạo FixedAsset(RESIDENTIAL_UNIT/COMMERCIAL_SPACE) → emit UnitCreated   | Implemented |
| UC-004 | Tạo Other Asset        | BQL_MANAGER tạo FACILITY / MEETING_ROOM / PARKING_SLOT / COMMON_AREA / EQUIPMENT   | Implemented |
| UC-005 | Xem cây tài sản        | BQL_MANAGER query toàn bộ asset trong 1 building theo materialized path             | Implemented |
| UC-006 | Tìm asset theo ID      | Query FixedAsset single record theo assetId                                         | Implemented |

---

## Phase 2 — OccupancyAgreement

| ID     | Tên                         | Mô tả ngắn                                                                   | Trạng thái  |
|--------|-----------------------------|------------------------------------------------------------------------------|-------------|
| UC-007 | Tạo OccupancyAgreement      | BQL_MANAGER tạo Agreement(PENDING), validate invariants I1–I7                | Implemented |
| UC-008 | Activate Agreement          | BQL_MANAGER chuyển → ACTIVE → emit OccupancyAgreementActivated               | Implemented |
| UC-009 | Terminate Agreement         | BQL_MANAGER chuyển → TERMINATED → emit OccupancyAgreementTerminated          | Implemented |
| UC-010 | Expire Agreement            | Chuyển → EXPIRED → emit OccupancyAgreementTerminated (manual hoặc scheduled) | Implemented |
| UC-011 | Xem agreements theo asset   | Query occupancy_agreement WHERE asset_id, filter by status (optional)        | Implemented |
| UC-012 | Xem agreements theo party   | Query occupancy_agreement WHERE party_id                                     | Implemented |

---

## Phase 3 — Operator Portal Support

| ID     | Tên                         | Mô tả ngắn                                                                                   | Trạng thái  |
|--------|-----------------------------|-----------------------------------------------------------------------------------------------|-------------|
| UC-013 | Tìm agreements theo building | Query occupancy_agreement prefix-match trên materialized path của building, filter by status | Implemented |

---

## Ghi chú

| Trạng thái    | Ý nghĩa                                             |
|---------------|-----------------------------------------------------|
| `Not started` | Chưa implement                                      |
| `Implemented` | Đã implement và có test                             |
