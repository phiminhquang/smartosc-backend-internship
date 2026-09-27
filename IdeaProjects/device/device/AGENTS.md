# Device Management

## Phạm vi dự án

- Backend nằm tại thư mục hiện tại, sử dụng Java 17, Spring Boot, Maven, Spring Security, JPA, MySQL và Flyway.
- Frontend nằm trong `frontend/`, sử dụng React, TypeScript, Vite, React Router và Axios.
- Chỉ làm việc trong dự án Device, không sửa các bài tập hoặc project khác trong repository cha nếu người dùng không yêu cầu.

## Cách làm việc

- Đọc code và cấu hình liên quan trước khi thay đổi; ưu tiên mở rộng cấu trúc hiện có.
- Chỉ sửa những file liên quan trực tiếp đến yêu cầu, không tự thêm tính năng ngoài phạm vi.
- Khi thay đổi API backend, kiểm tra và cập nhật service, type và màn hình tương ứng ở frontend.
- Không sửa hoặc commit `target/`, `node_modules/`, cache IDE và file sinh tự động.
- Không ghi mật khẩu, token, khóa bí mật hoặc thông tin đăng nhập vào code, Markdown hay Git.
- Không thay đổi cấu hình database, migration hoặc dữ liệu khi chưa xác định rõ tác động.
- Nếu thiếu thông tin có thể gây sửa sai hoặc mất dữ liệu, hỏi người dùng trước; nếu chỉ thiếu chi tiết nhỏ, nêu giả định hợp lý.

## Kiểm tra

- Backend: `bash ./mvnw test`.
- Frontend: `cd frontend && npm run lint && npm run build`.
- Với thay đổi liên quan cả hai phía, kiểm tra cả backend và frontend.
- Khi hoàn thành, báo ngắn gọn file đã sửa, kiểm thử đã chạy và lỗi còn lại.

## Chuyển giữa các AI

- Trước khi bàn giao, cập nhật `AI-HANDOFF.md` với mục tiêu, việc đã làm, file đã sửa, kiểm thử, vấn đề còn lại và bước tiếp theo.
- Khi tiếp quản, đọc `AGENTS.md`, `AI-HANDOFF.md`, `git status` và diff liên quan trước khi tiếp tục.
