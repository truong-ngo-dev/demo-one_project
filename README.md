# One Project (Monorepo Demo)

## Cấu hình & Thông tin bảo mật (Credentials)

Dự án này đã được cấu hình để chạy Demo nhanh chóng bằng cách sử dụng file `.env`.

### 1. File .env
Tôi đã trích xuất các thông tin quan trọng ra file `.env` tại thư mục gốc, bao gồm:
- **Database:** Tài khoản root/123456 và URL kết nối MySQL.
- **Google OAuth2:** Client ID và Secret phục vụ Social Login.
- **JWT:** Các khóa mã hóa token.

### 2. Cách sử dụng cho Demo
Các service Spring Boot trong project đã được cấu hình để tự động nhận các biến này. Bạn có thể:
- Chạy trực tiếp nếu môi trường của bạn hỗ trợ load file `.env`.
- Hoặc copy nội dung trong `.env` vào phần **Environment Variables** trong cấu hình Run của IDE (IntelliJ/VS Code).

### 3. Lưu ý
- File `.env` này được phép đẩy lên Git để phục vụ mục đích Demo theo yêu cầu.
- Trong thực tế, các thông tin này nên được quản lý qua Vault hoặc Environment Variables an toàn hơn.

---
*Ghi chú: Sau khi tải về, bạn chỉ cần đảm bảo MySQL đang chạy và đúng thông tin như trong .env là các service sẽ hoạt động.*
