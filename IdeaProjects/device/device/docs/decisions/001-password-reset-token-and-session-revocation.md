# ADR-001: Reset token dạng hash và thu hồi JWT bằng token version

- Trạng thái: Được chấp nhận cho implementation hiện tại.
- Ngày: 2026-09-29.
- Phạm vi: Luồng khôi phục mật khẩu.

## Bối cảnh

Luồng khôi phục mật khẩu cần phát một bí mật tạm thời qua email. Nếu database bị đọc trái phép, reset token không được trở thành mật khẩu thay thế. Sau khi mật khẩu đổi, các JWT cũ cũng không được tiếp tục truy cập hệ thống.

## Quyết định

### Reset token

- Sinh 32 byte bằng `SecureRandom`.
- Gửi raw token cho người dùng qua URL-safe Base64.
- Chỉ lưu SHA-256 hash trong database.
- Token có thời hạn mặc định 15 phút.
- Token chỉ được dùng một lần; khi dùng thành công, các token khác của user cũng bị vô hiệu hóa.
- Dùng pessimistic locking cho thao tác phát và xác nhận token.

### Thu hồi JWT

- Thêm `tokenVersion` vào user.
- Đưa giá trị này vào JWT khi phát hành.
- Tăng phiên bản sau khi đổi mật khẩu.
- Mỗi lần xác thực resource server hoặc introspect phải so sánh phiên bản trong JWT với database.

## Lý do

- Hash reset token giảm tác động khi dữ liệu token trong database bị lộ.
- Token ngẫu nhiên 256 bit đủ mạnh cho bí mật ngắn hạn.
- `used_at` và khóa bản ghi giúp thực thi tính chỉ-dùng-một-lần khi có request đồng thời.
- `tokenVersion` thu hồi toàn bộ JWT của một user mà không cần lưu deny-list cho từng JWT.

## Phương án đã cân nhắc

### Lưu raw reset token

Không chọn vì người đọc được database có thể trực tiếp chiếm tài khoản trong thời gian token còn hiệu lực.

### Dùng JWT làm reset token

Không chọn vì việc dùng một lần và thu hồi sớm sẽ cần thêm trạng thái hoặc deny-list; token opaque đã hash đơn giản hơn cho luồng này.

### Đợi access token tự hết hạn

Không chọn vì JWT bị đánh cắp vẫn có thể sử dụng sau khi chủ tài khoản đã đổi mật khẩu.

### Lưu deny-list cho từng JWT

Không chọn vì tăng dung lượng và độ phức tạp vận hành; `tokenVersion` phù hợp với yêu cầu thu hồi toàn bộ phiên của user.

## Hệ quả

### Tích cực

- Database không chứa reset credential sử dụng trực tiếp.
- Có thể thu hồi toàn bộ JWT cũ ngay sau reset.
- Quy tắc dễ kiểm thử bằng clock cố định và repository test doubles.

### Đánh đổi

- Mỗi request JWT cần đọc trạng thái user hoặc cơ chế cache an toàn tương đương.
- Người dùng bị đăng xuất khỏi mọi thiết bị sau khi reset mật khẩu.
- Cần migration database và phối hợp triển khai code/schema.
- Nếu email thất bại sau khi lưu token, implementation phải bảo đảm token bị vô hiệu hóa.

## Điều kiện xem xét lại

- Hệ thống cần đăng xuất theo từng thiết bị thay vì toàn bộ tài khoản.
- Lưu lượng xác thực khiến việc đọc `tokenVersion` từ database trở thành nút thắt đã được đo lường.
- Hệ thống chuyển sang session server-side hoặc cơ chế token revocation tập trung.
