# AI Handoff

## Mục tiêu hiện tại

- Hoàn tất và kiểm chứng end-to-end luồng quên mật khẩu an toàn qua email cho Device.
- Đưa feature từ `Verifying` tới `Done` bằng bằng chứng database, email và trình duyệt trên môi trường test/staging tách biệt.

## Trạng thái hiện tại

- Backend, migration, email template và frontend đã được triển khai.
- `bash scripts/verify.sh safe` ngày 2026-10-02 đã đạt: tài liệu/diff, backend compile, 15 test mục tiêu, frontend lint và build.
- Chưa chạy migration V2 trên database test/staging, SMTP test hoặc luồng trình duyệt end-to-end.
- Chưa sẵn sàng public production vì PR-207/208/209 trong `tasks.md` còn mở.
- Phân công mặc định và ranh giới chỉnh sửa tuân theo `AGENTS.md`; hiện không có ngoại lệ đang hoạt động.

## Nguồn sự thật cần đọc

- Yêu cầu và API contract: `specs/password-reset/spec.md`.
- Thiết kế triển khai và rollback: `specs/password-reset/plan.md`.
- Trạng thái công việc và blocker: `specs/password-reset/tasks.md`.
- Bằng chứng kiểm tra duy nhất: `specs/password-reset/verification.md`.
- Quyết định bảo mật lâu dài: `docs/decisions/001-password-reset-token-and-session-revocation.md`.
- Quy tắc project và cổng kiểm tra: `AGENTS.md` và `scripts/verify.sh`.

## Phạm vi file đã thay đổi

- Backend: controller, security/JWT validator, authentication/password-reset service, user/reset-token model, repository, DTO và test liên quan trong `src/main/` và `src/test/`.
- Database/email: `src/main/resources/db/migration/V2__password_reset.sql` và `src/main/resources/templates/email/password-reset.html`.
- Frontend: các trang forgot/reset password, `authService`, route trong `App.tsx` và chính sách referrer trong `frontend/`.
- Quy trình/tài liệu: `PROJECT.md`, các adapter AI, `specs/password-reset/`, ADR liên quan và `scripts/verify.sh`.
- Dùng Git diff/history để lấy danh sách file chính xác; handoff chỉ giữ phạm vi để tránh lặp dữ liệu.

## Blocker và rủi ro còn lại

- Chưa có MySQL test/staging tách khỏi Aiven đang dùng thật để áp dụng migration và chạy integration test an toàn.
- Chưa có SMTP sandbox/tài khoản email test và `PASSWORD_RESET_BASE_URL` cho môi trường kiểm thử.
- SMTP hiện đồng bộ trong request transaction, có thể tạo timing side-channel và giữ database lock khi gọi mạng.
- Cooldown theo user chưa thay thế rate limit theo IP/identity cho endpoint public.

## Việc tiếp theo

1. Review migration `V2__password_reset.sql`, sau đó áp dụng trên database test/staging tách khỏi Aiven đang dùng thật.
2. Cấu hình SMTP test và `PASSWORD_RESET_BASE_URL` cho môi trường kiểm thử.
3. Chạy các trường hợp trong ma trận `specs/password-reset/verification.md`.
4. Trước public production, hoàn tất PR-207/208/209 trong `tasks.md`.
5. Cập nhật `tasks.md` và `verification.md`, sau đó mới cập nhật lại handoff và review diff cuối.

## Cảnh báo vận hành

- Không lưu mật khẩu hoặc reset token dạng rõ trong repository/log/response.
- Không chạy `DeviceApplicationTests.contextLoads` hoặc full test suite khi chưa xác nhận datasource là môi trường cô lập; Flyway có thể chạm Aiven đang dùng thật.
- Dùng `bash scripts/verify.sh safe` làm cổng mặc định; script không chạy test cần datasource thật.
- Nếu Mockito không tự attach được trong sandbox, dùng cách Byte Buddy javaagent đã ghi trong `verification.md`.
