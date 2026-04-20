# Log: Property Service Phase 2 — Application Layer

## Status: ✅ Completed | `mvn clean compile -DskipTests` PASS

---

## Files Created

| File                                                             | Package                                                                          |
|------------------------------------------------------------------|----------------------------------------------------------------------------------|
| `application/agreement/AgreementView.java`                       | `vn.truongngo.apartcom.one.service.property.application.agreement`               |
| `application/agreement/create/CreateOccupancyAgreement.java`     | `vn.truongngo.apartcom.one.service.property.application.agreement.create`        |
| `application/agreement/activate/ActivateAgreement.java`          | `vn.truongngo.apartcom.one.service.property.application.agreement.activate`      |
| `application/agreement/terminate/TerminateAgreement.java`        | `vn.truongngo.apartcom.one.service.property.application.agreement.terminate`     |
| `application/agreement/expire/ExpireAgreement.java`              | `vn.truongngo.apartcom.one.service.property.application.agreement.expire`        |
| `application/agreement/find_by_asset/FindAgreementsByAsset.java` | `vn.truongngo.apartcom.one.service.property.application.agreement.find_by_asset` |
| `application/agreement/find_by_party/FindAgreementsByParty.java` | `vn.truongngo.apartcom.one.service.property.application.agreement.find_by_party` |

---

## PRESENTATION CONTEXT BLOCK

### Handler package paths

```
application.agreement.create.CreateOccupancyAgreement.Handler
application.agreement.activate.ActivateAgreement.Handler
application.agreement.terminate.TerminateAgreement.Handler
application.agreement.expire.ExpireAgreement.Handler
application.agreement.find_by_asset.FindAgreementsByAsset.Handler
application.agreement.find_by_party.FindAgreementsByParty.Handler
```

### AgreementView — full record fields

```java
record AgreementView(
    String id,
    String partyId,
    PartyType partyType,
    String assetId,
    OccupancyAgreementType agreementType,
    OccupancyAgreementStatus status,
    LocalDate startDate,
    LocalDate endDate,       // nullable
    String contractRef       // nullable
) {}
```

Static factory: `AgreementView.from(OccupancyAgreement agreement)`

### Command record fields

| Handler                            | Command fields                                                                                                                                                                                      |
|------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CreateOccupancyAgreement.Command` | `partyId (String)`, `partyType (PartyType)`, `assetId (String)`, `agreementType (OccupancyAgreementType)`, `startDate (LocalDate)`, `endDate (LocalDate nullable)`, `contractRef (String nullable)` |
| `ActivateAgreement.Command`        | `agreementId (String)`                                                                                                                                                                              |
| `TerminateAgreement.Command`       | `agreementId (String)`                                                                                                                                                                              |
| `ExpireAgreement.Command`          | `agreementId (String)`                                                                                                                                                                              |
| `FindAgreementsByAsset.Query`      | `assetId (String)`, `status (OccupancyAgreementStatus nullable)`                                                                                                                                    |
| `FindAgreementsByParty.Query`      | `partyId (String)`                                                                                                                                                                                  |

### Result types

| Handler                    | Result                       |
|----------------------------|------------------------------|
| `CreateOccupancyAgreement` | `Result(String agreementId)` |
| `ActivateAgreement`        | `Result()`                   |
| `TerminateAgreement`       | `Result()`                   |
| `ExpireAgreement`          | `Result()`                   |
| `FindAgreementsByAsset`    | `List<AgreementView>`        |
| `FindAgreementsByParty`    | `List<AgreementView>`        |

### Error codes thrown per handler

| Handler                    | Error codes                                                                                 |
|----------------------------|---------------------------------------------------------------------------------------------|
| `CreateOccupancyAgreement` | `ASSET_NOT_FOUND`, `OWNERSHIP_ALREADY_EXISTS`, `LEASE_ALREADY_EXISTS`, + domain I4–I7 codes |
| `ActivateAgreement`        | `AGREEMENT_NOT_FOUND`, `AGREEMENT_INVALID_STATUS`                                           |
| `TerminateAgreement`       | `AGREEMENT_NOT_FOUND`, `AGREEMENT_INVALID_STATUS`                                           |
| `ExpireAgreement`          | `AGREEMENT_NOT_FOUND`, `AGREEMENT_INVALID_STATUS`                                           |
| `FindAgreementsByAsset`    | —                                                                                           |
| `FindAgreementsByParty`    | —                                                                                           |

---

## Deviations

None — implemented exactly per spec.
