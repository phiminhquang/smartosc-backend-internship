# Device Management

## Sứ mệnh

Device Management là hệ thống quản lý vòng đời thiết bị trong tổ chức: danh mục thiết bị, cấp phát, gia hạn, trả thiết bị, sửa chữa, người dùng, vai trò và các chỉ số tổng quan.

Mục tiêu của project là tạo một hệ thống có hành vi rõ ràng, an toàn và có thể tiếp tục phát triển bởi nhiều AI hoặc lập trình viên mà không phụ thuộc vào lịch sử chat.

## Phạm vi sản phẩm

- Xác thực người dùng và phân quyền truy cập.
- Quản lý thiết bị và trạng thái thiết bị.
- Quản lý việc cấp phát, gia hạn và hoàn trả thiết bị.
- Quản lý sửa chữa thiết bị.
- Quản lý người dùng và vai trò.
- Gửi email phục vụ các luồng nghiệp vụ.
- Cung cấp dashboard và giao diện web cho người sử dụng.

Các tính năng mới chỉ được xem là thuộc phạm vi khi có đặc tả và tiêu chí chấp nhận được người dùng duyệt.

## Kiến trúc hiện tại

### Backend

- Java 17 và Spring Boot.
- Spring MVC cung cấp REST API.
- Spring Security và JWT bảo vệ tài nguyên.
- Spring Data JPA truy cập MySQL.
- Flyway quản lý thay đổi schema.
- Thymeleaf và Spring Mail tạo, gửi email HTML.
- Maven quản lý build và test.

### Frontend

- React và TypeScript.
- Vite quản lý môi trường phát triển và build.
- React Router quản lý điều hướng.
- Axios gọi backend API.

### Ranh giới chính

```text
React frontend
      |
      | HTTP/JSON
      v
Spring REST API
      |
      +-- Spring Security / JWT
      +-- Service layer
      +-- JPA repositories
      +-- Email service
      |
      v
    MySQL
```

Frontend không truy cập database trực tiếp. Controller không chứa logic nghiệp vụ dài; nghiệp vụ thuộc service. Thay đổi schema phải đi qua migration Flyway mới và không sửa migration đã được áp dụng.

## Nguồn sự thật của project

Theo thứ tự ưu tiên:

1. Code, migration và test đang được Git theo dõi.
2. Đặc tả đã được duyệt trong `specs/`.
3. Quy tắc thực thi trong `AGENTS.md`.
4. Quyết định kiến trúc trong `docs/decisions/`.
5. Trạng thái công việc tạm thời trong `AI-HANDOFF.md`.

Lịch sử chat không phải nguồn trạng thái của project.

## Quy trình thay đổi

Mọi thay đổi hành vi đi theo chuỗi:

```text
Specify -> Plan -> Tasks -> Implement -> Verify -> Review -> Commit
```

- Thay đổi nhỏ có thể gộp đặc tả, kế hoạch, task và kiểm chứng vào một file.
- Tính năng có API, database, bảo mật hoặc thay đổi cả frontend/backend phải có thư mục riêng trong `specs/<feature>/`.
- Khi triển khai, cập nhật trạng thái trong `tasks.md`.
- Khi kiểm tra, ghi lệnh và kết quả thật trong `verification.md`.
- Dùng `bash scripts/verify.sh safe` làm cổng kiểm tra cục bộ mặc định; full integration test cần database cô lập.
- Khi bàn giao, cập nhật `AI-HANDOFF.md`; không chép lại toàn bộ tài liệu thiết kế vào handoff.

## Cổng chất lượng

Một tính năng chưa hoàn thành nếu còn thiếu một trong các điều kiện áp dụng:

- Tiêu chí chấp nhận đã được xác định.
- Backend compile và test thành công.
- Frontend lint và build thành công.
- Migration đã được review và thử trên database phù hợp.
- Luồng chính và trường hợp lỗi quan trọng đã được kiểm tra.
- Không có secret hoặc dữ liệu nhạy cảm trong code, log hay Git.
- Tài liệu task, verification và handoff phản ánh đúng trạng thái thực tế.

## Nguyên tắc bảo mật

- Không lưu mật khẩu, token xác thực hoặc API key dạng rõ.
- Không tiết lộ sự tồn tại của tài khoản qua các luồng khôi phục danh tính.
- Mọi dữ liệu đầu vào từ client phải được validate ở backend.
- Quyền truy cập được thực thi ở backend, không dựa vào việc ẩn giao diện frontend.
- Thay đổi xác thực, phân quyền và migration phải có test hoặc kế hoạch kiểm tra tương ứng.
- Các hành động phá hủy dữ liệu phải yêu cầu xác nhận rõ ràng và có phương án khôi phục khi khả thi.

## Tài liệu liên quan

- `AGENTS.md`: quy tắc dành cho AI và lập trình viên.
- `CLAUDE.md` và `GEMINI.md`: adapter mỏng giúp từng agent nạp cùng quy tắc chung.
- `AI-HANDOFF.md`: trạng thái phiên làm việc hiện tại.
- `specs/README.md`: cách chọn mức tài liệu và vòng đời feature.
- `specs/_template/`: mẫu dùng lại cho đặc tả, kế hoạch, task và kiểm chứng.
- `specs/<feature>/`: tài liệu và bằng chứng theo từng tính năng.
- `docs/decisions/`: các quyết định kiến trúc có ảnh hưởng lâu dài và ADR template.
- `scripts/verify.sh`: cổng kiểm tra an toàn, có thể lặp lại giữa các agent.
