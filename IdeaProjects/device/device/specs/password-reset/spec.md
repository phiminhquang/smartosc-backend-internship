# Đặc tả: Khôi phục mật khẩu qua email

## Trạng thái

- Giai đoạn: Đang kiểm chứng.
- Phạm vi: Backend, database, email và frontend.
- Nguồn trạng thái triển khai: `tasks.md`.
- Nguồn bằng chứng kiểm tra: `verification.md`.

## Vấn đề

Người dùng quên mật khẩu cần có cách lấy lại quyền truy cập mà không cần quản trị viên đặt mật khẩu thủ công. Luồng phải tránh tiết lộ email nào tồn tại, không lưu reset token dạng rõ và phải vô hiệu hóa các phiên JWT cũ sau khi đổi mật khẩu.

## Mục tiêu

- Cho phép người dùng yêu cầu liên kết đặt lại mật khẩu bằng email.
- Cho phép đặt mật khẩu mới bằng token có thời hạn và chỉ dùng một lần.
- Không cho phép suy đoán tài khoản qua phản hồi API.
- Thu hồi các JWT đã phát trước thời điểm đổi mật khẩu.
- Cung cấp giao diện web cho cả bước yêu cầu và xác nhận.

## Ngoài phạm vi

- Thay đổi email của tài khoản.
- Xác thực đa yếu tố.
- Đăng xuất từng thiết bị riêng lẻ.
- Trang quản trị xem hoặc cấp reset token.
- Tự động dọn các token hết hạn khỏi database.

## Tác nhân

- Người dùng chưa đăng nhập và quên mật khẩu.
- Dịch vụ email gửi liên kết khôi phục.
- Backend xác minh token và cập nhật mật khẩu.

## Yêu cầu chức năng

### FR-1: Yêu cầu đặt lại mật khẩu

- Client gửi email tới `POST /api/auth/password-reset/request`.
- Backend validate định dạng email.
- API trả cùng một thông điệp cho email tồn tại và không tồn tại.
- Nếu tài khoản tồn tại và không nằm trong cooldown, backend tạo token mới và gửi email.
- Yêu cầu lặp lại trong khoảng cooldown không tạo thêm email/token mới.

### FR-2: Nội dung liên kết

- Email chứa liên kết tới frontend với token nằm trong query parameter `token`.
- Thời gian hết hạn được hiển thị cho người dùng.
- Base URL frontend được lấy từ cấu hình, không hard-code theo môi trường production.

### FR-3: Xác nhận đặt lại mật khẩu

- Client gửi token và mật khẩu mới tới `POST /api/auth/password-reset/confirm`.
- Backend từ chối token không tồn tại, đã dùng hoặc hết hạn bằng cùng một lỗi nghiệp vụ.
- Mật khẩu mới phải thỏa validation từ 8 đến 128 ký tự.
- Khi thành công, backend BCrypt-hash mật khẩu và đánh dấu token đã dùng.
- Các token reset chưa dùng khác của cùng tài khoản bị vô hiệu hóa.

### FR-4: Thu hồi phiên đăng nhập cũ

- User có `tokenVersion` lưu trong database.
- JWT mới chứa phiên bản token của user tại thời điểm phát hành.
- Sau khi đổi mật khẩu, `tokenVersion` tăng một đơn vị.
- Resource server và API introspect từ chối JWT có phiên bản cũ.

### FR-5: Giao diện frontend

- Trang `/forgot-password` cho phép nhập email và luôn hiển thị thông điệp trung lập khi backend chấp nhận yêu cầu.
- Trang `/reset-password` đọc token từ URL nhưng vẫn cho phép người dùng nhập token.
- Sau khi lấy token, frontend xóa query parameter khỏi thanh địa chỉ bằng navigation replace.
- Frontend kiểm tra token không rỗng, độ dài mật khẩu và xác nhận mật khẩu trước khi gọi API.
- Sau khi thành công, người dùng có đường dẫn quay lại trang đăng nhập.

## Yêu cầu bảo mật

- Token được tạo bằng bộ sinh số ngẫu nhiên mật mã với tối thiểu 256 bit entropy.
- Database chỉ lưu SHA-256 hash của token.
- Token mặc định hết hạn sau 15 phút và chỉ dùng một lần.
- Không ghi raw token, mật khẩu hoặc nội dung nhạy cảm vào log.
- Frontend đặt referrer policy `no-referrer` và không giữ raw token trong URL lâu hơn thời điểm nạp trang.
- Các thao tác phát và sử dụng token phải chống được cập nhật đồng thời.
- Nếu gửi email thất bại, token vừa tạo phải bị vô hiệu hóa.
- Endpoint request và confirm phải truy cập được khi chưa đăng nhập; các endpoint còn lại giữ nguyên chính sách bảo vệ.
- Trước khi public production, endpoint request phải có rate limit chống abuse không phụ thuộc vào việc email có tồn tại.

## Tiêu chí chấp nhận

- [ ] Email tồn tại nhận được liên kết reset hợp lệ trong môi trường test/staging.
- [ ] Email không tồn tại nhận phản hồi HTTP và thông điệp không phân biệt được với email tồn tại.
- [ ] Token đúng chỉ đổi mật khẩu được một lần.
- [ ] Token sai, hết hạn hoặc đã dùng đều bị từ chối.
- [ ] Hai yêu cầu trong cooldown không phát hai token sử dụng được.
- [ ] Đổi mật khẩu vô hiệu hóa mọi JWT đã phát trước đó.
- [ ] Đăng nhập bằng mật khẩu cũ thất bại và mật khẩu mới thành công.
- [ ] Backend compile/test thành công.
- [ ] Frontend lint/build thành công.
- [ ] Migration chạy thành công trên database test/staging có schema V1.
- [ ] Trước public production, cơ chế gửi email/rate limit không tạo timing side-channel rõ ràng giữa email tồn tại và không tồn tại.

## Giả định

- Mỗi email xác định tối đa một user.
- Hệ thống email và database test/staging được cấu hình ngoài Git.
- Đồng hồ hệ thống của backend là nguồn thời gian cho việc hết hạn token.
