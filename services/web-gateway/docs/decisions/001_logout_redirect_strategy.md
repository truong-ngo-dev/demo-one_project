# ADR-001: Logout Redirect Strategy

- **Trạng thái**: Accepted
- **Ngày**: 2025-06
- **Người quyết định**: Backend Team

---

## Bối cảnh

Sau khi Web Gateway xử lý logout xong, cần redirect Angular đến oauth2 service để hoàn tất OIDC RP-Initiated Logout. Có hai cách:

1. **302 Redirect**: Web Gateway trả `302` với `Location` header — browser tự follow redirect.
2. **202 Accepted + Location**: Web Gateway trả `202` với `Location` header — Angular tự navigate.

Angular gọi logout qua `HttpClient` (XHR/fetch), không phải navigation trực tiếp của browser.

---

## Quyết định

**Trả `202 Accepted` + `Location` header — Angular tự navigate.**

Lý do không dùng `302`:
- XHR/fetch không follow `302` redirect cross-origin theo cách browser navigation làm — Angular sẽ nhận response của trang oauth2 thay vì được redirect đến đó.
- `302` chỉ hoạt động đúng khi browser tự khởi tạo request (navigation), không phải XHR.

Lý do dùng `202 + Location`:
- Khi Angular navigate bằng `window.location.href`, browser tự động đính kèm cookie của oauth2 service vào request — oauth2 service nhờ đó identify đúng HTTP session cần clear. Đây là lý do cốt lõi: **không có cách nào khác để truyền session cookie của oauth2 về phía oauth2 service để logout đúng session.**
- Angular nhận `202` + `Location` → tự gọi `window.location.href = location` để navigate.
- Giữ Angular kiểm soát hoàn toàn luồng điều hướng — phù hợp với SPA pattern.

---

## Hệ quả

**Tích cực:**
- Hoạt động đúng với Angular SPA dùng `HttpClient`.
- Angular có thể xử lý thêm logic trước khi navigate nếu cần.

**Đánh đổi:**
- Không phải HTTP standard — dev không quen có thể thấy lạ và "fix" thành `302`.
- Angular phải tự xử lý `202` response và navigate — cần đảm bảo client implement đúng.

---

## Tham khảo
- [Logout Flow](../flows/002_logout_flow.md)