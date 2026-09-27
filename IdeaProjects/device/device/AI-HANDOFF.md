# AI Handoff

## Mục tiêu hiện tại

- Thiết lập môi trường phát triển dự án Device trên Linux Mint.

## Đã hoàn thành

- Đã clone repository mới vào `/home/quang/Projects/test-git`.
- Đã cài Java 17 và IntelliJ IDEA.
- IntelliJ đã nhận Maven project và JDK 17.
- Frontend đã cài dependency bằng `npm ci` và có thể chạy bằng `npm run dev`.
- Quy tắc AI dùng chung đã được liên kết cho Codex, Claude Code và Antigravity/Gemini.

## Trạng thái backend

- Backend biên dịch và bắt đầu khởi động được bằng Java 17.
- Backend hiện dừng ở bước kết nối MySQL Aiven vì thông tin xác thực database chưa được chuyển từ Windows sang Linux.
- Không thay đổi `application.yaml` và không lưu thông tin bí mật vào repository.

## File đã thay đổi

- `AGENTS.md`: quy tắc làm việc riêng của dự án.
- `AI-HANDOFF.md`: trạng thái bàn giao giữa các AI.

## Kiểm tra đã thực hiện

- `java -version`: Java 17 hoạt động.
- IntelliJ tải được Maven dependencies và khởi chạy tới bước kết nối database.
- `npm ci`: hoàn tất trong `frontend/`.
- `npm run dev`: frontend chạy trên Vite.

## Việc tiếp theo

1. Lấy các biến môi trường cần thiết từ cấu hình Windows/Aiven và nhập vào IntelliJ Run Configuration.
2. Chạy lại backend, xác nhận có dòng `Started DeviceApplication`.
3. Kiểm tra luồng đăng nhập giữa frontend và backend.

## Lưu ý

- Không chép mật khẩu, token hoặc khóa bí mật vào file này.
- Cập nhật file này ngắn gọn trước khi chuyển sang AI khác.
