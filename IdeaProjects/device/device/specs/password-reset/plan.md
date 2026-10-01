# Kế hoạch kỹ thuật: Khôi phục mật khẩu

## Tổng quan giải pháp

Luồng sử dụng một token ngẫu nhiên chỉ hiển thị trong email. Backend lưu hash của token, kiểm tra thời hạn và trạng thái sử dụng trong transaction. Sau khi đổi mật khẩu, phiên bản JWT của user được tăng để mọi JWT cũ mất hiệu lực.

```text
Người dùng
   |
   | email
   v
POST /password-reset/request
   |
   +-- khóa user
   +-- kiểm tra cooldown
   +-- vô hiệu token cũ
   +-- tạo raw token
   +-- lưu SHA-256(raw token)
   +-- gửi link qua email

Người dùng mở link
   |
   | raw token + mật khẩu mới
   v
POST /password-reset/confirm
   |
   +-- khóa token
   +-- kiểm tra tồn tại / hết hạn / đã dùng
   +-- BCrypt mật khẩu mới
   +-- tăng user.tokenVersion
   +-- đánh dấu các reset token đã dùng
```

## Thay đổi dữ liệu

Migration `V2__password_reset.sql`:

- Thêm `users.token_version BIGINT NOT NULL DEFAULT 0`.
- Tạo bảng `password_reset_tokens`.
- Dùng UUID dạng `BINARY(16)` làm khóa chính.
- `token_hash` là `CHAR(64)` với unique index.
- Index theo user/thời điểm tạo và thời điểm hết hạn.
- Xóa user sẽ cascade xóa reset token.

Migration phải được thử trên schema đã áp dụng V1 trước khi triển khai.

## Thành phần backend

- `PasswordResetRequest`: validate email.
- `PasswordResetConfirmRequest`: validate token và mật khẩu mới.
- `PasswordResetToken`: biểu diễn token đã hash và vòng đời sử dụng.
- `PasswordResetTokenRepository`: truy vấn có khóa và cập nhật token hàng loạt.
- `PasswordResetServiceImpl`: điều phối phát token, gửi email và xác nhận.
- `AuthenticationController`: cung cấp hai public endpoint.
- `JwtTokenVersionValidator`: kiểm tra JWT hiện hành so với user.
- `Authenticationimpl`: phát và introspect JWT có `tokenVersion`.
- `SecurityConfig`: mở đúng hai endpoint reset và gắn validator.

## Thành phần frontend

- `ForgotPasswordPage`: thu email và gọi API request.
- `ResetPasswordPage`: đọc query token, validate mật khẩu và gọi API confirm.
- `authService`: đóng gói hai lời gọi API.
- `App.tsx`: khai báo hai public route.
- `LoginPage`: cung cấp đường dẫn vào luồng quên mật khẩu.

## Quyết định bảo mật

- Raw token chỉ tồn tại trong bộ nhớ ứng dụng và email; database lưu SHA-256 hash.
- Token có 32 byte ngẫu nhiên và được mã hóa Base64 URL-safe.
- Response request là trung lập để hạn chế account enumeration.
- Pessimistic locking bảo vệ thao tác cạnh tranh trên user và token.
- `tokenVersion` thu hồi JWT mà không cần lưu deny-list cho từng token.
- Lỗi gửi email không được log raw token và token đã lưu bị đánh dấu không sử dụng được.

Chi tiết lý do được ghi trong `docs/decisions/001-password-reset-token-and-session-revocation.md`.

## Rủi ro và biện pháp

| Rủi ro | Biện pháp |
|---|---|
| Lộ danh sách tài khoản | Phản hồi request giống nhau cho mọi email |
| Token bị đọc từ database | Chỉ lưu hash token |
| Dùng token hai lần | Khóa bản ghi và trường `used_at` |
| Hai yêu cầu song song | Khóa user, cooldown và vô hiệu token cũ |
| JWT cũ tiếp tục hoạt động | Tăng và kiểm tra `tokenVersion` |
| Migration ảnh hưởng database thật | Chỉ thử trên test/staging trước, không dùng test context với datasource production |
| Cấu hình URL sai | Dùng `PASSWORD_RESET_BASE_URL` theo môi trường |
| Email không gửi được | Vô hiệu token vừa lưu và không log dữ liệu nhạy cảm |
| Token lộ qua URL/referrer/history | Dùng `no-referrer`, lấy token một lần rồi thay URL không có query |
| Timing tiết lộ email tồn tại | Trước production, tách gửi email khỏi request transaction và áp dụng rate limit độc lập với sự tồn tại tài khoản |
| Abuse endpoint request | Thiết kế rate limit ở gateway hoặc tầng ứng dụng có storage dùng chung; không dùng map in-memory như giải pháp production |

## Trình tự triển khai

1. Review đặc tả và migration.
2. Hoàn thiện backend và test cô lập.
3. Hoàn thiện frontend, lint và build.
4. Áp dụng migration lên database test/staging.
5. Cấu hình email và reset base URL cho môi trường đó.
6. Chạy kiểm thử end-to-end và các trường hợp lỗi.
7. Review diff bảo mật.
8. Trước public production, hoàn tất quyết định async/outbox và rate limit.
9. Cập nhật verification, handoff và commit.

## Kế hoạch quay lui

- Trước khi migration production, sao lưu database và xác nhận khả năng restore.
- Nếu frontend có lỗi, có thể gỡ liên kết điều hướng trong khi giữ backend endpoint không được quảng bá.
- Không xóa cột hoặc bảng bằng cách sửa V2 đã áp dụng; mọi rollback schema phải dùng migration mới đã được review.
- Nếu validator JWT gây sự cố, sửa bằng thay đổi code có test; không tắt kiểm tra bảo mật một cách âm thầm.
