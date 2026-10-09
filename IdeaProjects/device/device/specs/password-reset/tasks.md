# Công việc: Khôi phục mật khẩu

## Trạng thái tổng thể

- Backend: Đã triển khai, qua cổng an toàn và kiểm chứng trên MySQL Testcontainers/Compose local.
- Frontend: Đã triển khai; lint/build và Playwright Chromium browser E2E thành công local/GitHub.
- Phát hành: Chưa sẵn sàng.

## Phân công

- Backend và database owner: Codex/GPT.
- Frontend owner: Antigravity/Gemini.
- Integration, tài liệu chung và handoff owner: Codex/GPT.
- Antigravity không sửa backend; Codex không sửa frontend nếu người dùng không giao ngoại lệ rõ ràng.
- Hai phía dùng API contract trong `spec.md`; blocker hoặc thay đổi contract phải được ghi trước khi bên phụ thuộc tiếp tục.

## 1. Đặc tả và thiết kế

- [x] PR-001 Xác định mục tiêu, ngoài phạm vi và tiêu chí chấp nhận.
- [x] PR-002 Thiết kế token một lần, thời hạn và cooldown.
- [x] PR-003 Thiết kế thu hồi JWT cũ bằng `tokenVersion`.
- [x] PR-004 Ghi quyết định kiến trúc và kế hoạch kiểm chứng.

## 2. Backend và database

- [x] PR-101 Tạo DTO request và validation.
- [x] PR-102 Tạo entity và repository cho reset token.
- [x] PR-103 Tạo migration V2.
- [x] PR-104 Cài đặt request/confirm service.
- [x] PR-105 Thêm email template và cấu hình base URL/thời hạn/cooldown.
- [x] PR-106 Thêm public controller endpoints.
- [x] PR-107 Thêm `tokenVersion` vào user và JWT.
- [x] PR-108 Kiểm tra `tokenVersion` ở resource server và introspect.
- [x] PR-109 Thêm unit test và web-security test liên quan.

## 3. Frontend

- [x] PR-201 Thêm type và service cho request/confirm API.
- [x] PR-202 Thêm trang `/forgot-password`.
- [x] PR-203 Thêm trang `/reset-password`.
- [x] PR-204 Thêm route và liên kết từ trang đăng nhập.
- [x] PR-205 Thêm trạng thái loading, success và error cơ bản.
- [x] PR-206 Xóa token khỏi URL sau khi nạp và chặn gửi referrer.

## 4. Follow-up bảo mật trước production

- [ ] PR-207 Chọn và triển khai rate limit không phụ thuộc vào việc email tồn tại.
- [ ] PR-208 Tách gửi email khỏi transaction/request path bằng async event hoặc outbox có retry.
- [ ] PR-209 Kiểm tra timing của email tồn tại/không tồn tại sau khi hoàn tất PR-207/208.

## 5. Kiểm chứng tự động

- [x] PR-301 Backend compile thành công; xem bằng chứng trong `verification.md`.
- [x] PR-302 Nhóm 28 test backend cô lập thành công; xem bằng chứng trong `verification.md`.
- [x] PR-303 `git diff --check` thành công; xem bằng chứng trong `verification.md`.
- [x] PR-304 Chạy lại 15 backend test mục tiêu cho password reset/JWT/security.
- [x] PR-305 Chạy `npm run lint`.
- [x] PR-306 Chạy `npm run build`.

## 6. Kiểm chứng tích hợp

- [x] PR-401 Tạo database test/staging từ V1 và áp dụng migration V2.
- [x] PR-402 Cấu hình SMTP test và `PASSWORD_RESET_BASE_URL`.
- [x] PR-403 Kiểm tra yêu cầu reset với email tồn tại.
- [x] PR-404 Kiểm tra phản hồi trung lập với email không tồn tại.
- [ ] PR-405 Kiểm tra token đúng, sai, hết hạn và dùng lại.
- [ ] PR-406 Kiểm tra cooldown và yêu cầu đồng thời.
- [ ] PR-407 Xác nhận JWT cũ bị từ chối sau khi đổi mật khẩu.
- [x] PR-408 Playwright xác nhận mật khẩu cũ thất bại và mật khẩu mới đăng nhập thành công.
- [x] PR-409 Playwright Chromium chạy toàn bộ luồng DOM qua Mailpit trên full Compose local và GitHub.
- [x] PR-410 Playwright xác nhận token biến mất khỏi address bar và raw token không xuất hiện trong header `Referer` tiếp theo.

## 7. Hoàn tất

- [x] PR-501 Review diff bảo mật và migration. Review lại ngày 2026-10-08 xác nhận token chỉ lưu hash, endpoint public đúng phạm vi, JWT có `tokenVersion`, V2 áp dụng thành công trên MySQL cô lập và không có secret production trong cấu hình mới; PR-207/208/209 vẫn là blocker phát hành.
- [x] PR-502 Cập nhật `verification.md` bằng kết quả thật.
- [x] PR-503 Cập nhật `AI-HANDOFF.md`.
- [x] PR-504 Đã tạo branch/commit và PR #1; ba job GitHub CI đạt trên commit `5837be2`.

## Quy tắc cập nhật

- Chỉ đánh dấu `[x]` khi có code hoặc bằng chứng kiểm tra tương ứng.
- Nếu task bị chặn, ghi lý do và điều kiện gỡ chặn ngay dưới task.
- Không dùng trạng thái trong lịch sử chat thay cho file này.
