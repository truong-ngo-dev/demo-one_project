# ADR-003: Token Revocation Strategy

- **Trạng thái**: Accepted
- **Ngày**: 2025-06
- **Người quyết định**: Backend Team

---

## Bối cảnh

Khi user logout hoặc session bị terminate, các token liên quan cần được vô hiệu hóa. Có hai hướng tiếp cận phổ biến:

1. **Token Introspection**: Resource Server gọi về Authorization Server để kiểm tra token còn hợp lệ không tại mỗi request.
2. **Delete Authorization Record**: Xóa Authorization Record, dùng `id_token` làm định danh để tìm đúng record cần xóa.

Yêu cầu:
- Logout phải có hiệu lực ngay lập tức với Refresh Token.
- Không muốn tăng latency tại Resource Server.

---

## Quyết định

**Xóa Authorization Record via `id_token`**.
> Spring Authorization Server không cung cấp sẵn operation này — cần mở rộng `OAuth2AuthorizationService`.

Lý do không dùng Introspection:
- Introspection tạo thêm một network hop tại mỗi request đến Resource Server → tăng latency.
- Resource Server đã verify JWT local qua JWKS — introspection là redundant (xem ADR-001).
- Mô hình stateless verification tại Resource Server là ưu tiên thiết kế — introspection đi ngược lại điều đó.

Lý do dùng `id_token` làm key để revoke:
- `id_token` luôn được client giữ và gửi kèm trong logout request theo OIDC standard.
- `sid` claim trong `id_token` ánh xạ trực tiếp từ primary key của Authorization Record — tạo liên kết rõ ràng để tìm đúng record.

---

## Hệ quả

**Tích cực:**
- Revoke Refresh Token có hiệu lực ngay — record bị xóa, không thể dùng để tái cấp Access Token.
- Logic tập trung tại một operation duy nhất, dễ trace và audit.
- Không overhead introspection tại Resource Server.

**Đánh đổi:**
- Access Token JWT đã phát hành vẫn hợp lệ đến hết TTL sau khi record bị xóa. Đây là trade-off đã chấp nhận (xem ADR-001) — giải quyết bằng TTL ngắn.
- Bắt buộc phải đi qua operation xóa theo `id_token` — không được xóa Authorization Record bằng cách khác vì có thể bỏ sót downstream cleanup.

---

## Ghi chú cho Agent
Mọi logic revoke token đều phải đi qua operation xóa Authorization Record theo `id_token` trên `OAuth2AuthorizationService` — không bypass qua repository trực tiếp.

---

## Tham khảo
- [OpenID Connect RP-Initiated Logout](https://openid.net/specs/openid-connect-rpinitiated-1_0.html)
- [ADR-001: Token Format](001_token_format.md)
- [Flow: Logout](../flows/002_logout_flow.md)