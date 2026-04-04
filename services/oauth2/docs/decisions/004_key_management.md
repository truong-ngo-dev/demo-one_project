# ADR-004: RSA Key Management

- **Trạng thái**: Accepted
- **Ngày**: 2025-06
- **Người quyết định**: Backend Team

---

## Bối cảnh

JWT signing yêu cầu một cặp RSA key — private key để sign, public key để verify. Cần quyết định nơi lưu trữ và lifecycle của key.

Yêu cầu:
- Key phải tồn tại xuyên suốt các lần restart service.
- Nhiều instance phải dùng chung một key (horizontal scaling).
- Key Rotation là `[PLANNED]` — chưa implement trong phase hiện tại.

Các lựa chọn xem xét:
- **File system**: Đơn giản nhưng không phù hợp môi trường containerized (ephemeral storage).
- **Environment variable / properties**: Khó quản lý key dài, không có nền tảng cho rotation sau này.
- **Database**: Persistent, chia sẻ được giữa các instance, dễ mở rộng cho rotation.

---

## Quyết định

**Lưu RSA Key Pairs tại DB**, trong bảng `rsa_key_pairs`.

Khi startup, service kiểm tra bảng này:
- Chưa có key → tự generate cặp RSA mới và lưu vào DB.
- Đã có key → load và dùng.

---

## Hệ quả

**Tích cực:**
- Key persistent qua restart, nhất quán giữa các instance khi scale horizontal.
- Nền tảng sẵn sàng cho Key Rotation — chỉ cần thêm record mới, JWKS trả về multiple keys.

**Đánh đổi:**
- DB là single point of failure cho key — cần đảm bảo DB HA ở production.
- Key hiện lưu plaintext trong DB — production nên cân nhắc encrypt at rest hoặc tích hợp KMS khi implement Key Rotation.

---

## [PLANNED] Key Rotation
Chưa implement. Khi implement sẽ cần:
- Hỗ trợ nhiều key active đồng thời — JWKS trả về multiple public keys.
- Key mới nhất dùng để sign, các key cũ vẫn verify được token đã phát hành.
- TTL / expiry cho từng key pair.