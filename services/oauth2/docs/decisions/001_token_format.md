# ADR-001: Token Format

- **Trạng thái**: Accepted
- **Ngày**: 2025-06
- **Người quyết định**: Backend Team

---
## Bối cảnh

OAuth2 service cần phát hành 3 loại token phục vụ các mục đích khác nhau. Mỗi loại có yêu cầu riêng về bảo mật, khả năng revoke, và cách verify:

- **Access Token**: Dùng để truy cập Resource Server. Yêu cầu verify nhanh, không muốn phụ thuộc vào Authorization Server tại mỗi request.
- **Refresh Token**: Dùng để tái cấp Access Token. Yêu cầu revoke được ngay lập tức, không nên chứa thông tin nhạy cảm.
- **ID Token**: Dùng để truyền thông tin identity cho client (OIDC). Client cần decode trực tiếp.

---

## Quyết định

| Token         | Format | Lý do                                                                                |
|---------------|--------|--------------------------------------------------------------------------------------|
| Access Token  | JWT    | Resource Server verify local qua JWKS — không cần gọi về Authorization Server.       |
| Refresh Token | Opaque | Không expose thông tin bên trong. Revoke đơn giản bằng cách xóa record DB.           |
| ID Token      | JWT    | OIDC standard — client decode trực tiếp để lấy identity claims mà không cần gọi API. |

**JWT Customization**: Thêm claim `sid` vào Access Token và ID Token, ánh xạ từ primary key của Authorization Record. Claim này là cầu nối giữa token và session — phục vụ logout và revocation.

---

## Hệ quả

**Tích cực:**
- Access Token verify hoàn toàn offline tại Resource Server — giảm latency, không tạo thêm dependency.
- Refresh Token opaque đảm bảo chỉ Authorization Server mới đọc được nội dung.
- `sid` claim cho phép trace từ bất kỳ token nào về đúng session gốc.

**Đánh đổi:**
- Access Token JWT không thể revoke tức thời sau khi phát hành — phải chờ hết TTL. Đây là trade-off đã chấp nhận, giải quyết bằng cách đặt TTL ngắn.
- JWKS endpoint tại Authorization Server phải luôn available và được cache đúng cách tại Resource Server.

---

## Tham khảo
- [RFC 9068 – JWT Profile for OAuth 2.0 Access Tokens](https://datatracker.ietf.org/doc/html/rfc9068)
- [OpenID Connect Core – ID Token](https://openid.net/specs/openid-connect-core-1_0.html#IDToken)
- [ADR-003: Revocation Strategy](003_revocation_strategy.md)