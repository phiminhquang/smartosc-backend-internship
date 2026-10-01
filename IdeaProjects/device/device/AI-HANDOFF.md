# AI Handoff

## Mục tiêu hiện tại

- Hoàn tất và kiểm chứng end-to-end luồng quên mật khẩu an toàn qua email cho Device.
- Chuẩn hóa tính năng theo bộ tài liệu `specs/password-reset/` để agent khác có thể tiếp quản từ file thay vì lịch sử chat.

## Phân công agent hiện tại

- Codex/GPT: backend, database/migration, backend test, integration và tài liệu chung.
- Antigravity/Gemini: frontend trong `frontend/`.
- API contract trong `specs/password-reset/spec.md` là giao diện bàn giao giữa hai phía.
- Không agent nào tự sửa phần code của bên kia nếu người dùng không giao ngoại lệ rõ ràng.
- Khi chạy song song phải dùng branch/worktree riêng; không cùng sửa tài liệu chung hoặc cùng một working tree.

## Đã hoàn thành

- Thêm API công khai yêu cầu và xác nhận đặt lại mật khẩu tại `/api/auth/password-reset/*`.
- Token được sinh bằng `SecureRandom`, chỉ lưu SHA-256 hash, hết hạn sau 15 phút và chỉ dùng một lần.
- Phản hồi yêu cầu reset không tiết lộ email có tồn tại hay không; giới hạn gửi lại mặc định 60 giây.
- Dùng khóa bi quan để tránh phát hoặc sử dụng token đồng thời.
- Mật khẩu mới được BCrypt hash; token cũ bị vô hiệu hóa trong cùng transaction.
- Thêm `tokenVersion` vào user và JWT để thu hồi toàn bộ JWT cũ sau khi đổi mật khẩu, bao gồm kiểm tra tại resource server và API introspect.
- Thêm email Thymeleaf và các cấu hình reset có giá trị mặc định không nhạy cảm.
- Thêm frontend `/forgot-password` và `/reset-password`, service gọi API, route và liên kết từ trang đăng nhập.
- Thêm tài liệu nền `PROJECT.md`, đặc tả/kế hoạch/task/verification và ADR cho quyết định bảo mật.
- Thêm `CLAUDE.md`, `GEMINI.md` làm adapter mỏng tới quy tắc dùng chung.
- Thêm hướng dẫn mức S/M/L cùng template feature và ADR để tái sử dụng cho thay đổi sau.
- Sau security review, frontend xóa reset token khỏi URL sau khi nạp và đặt referrer policy `no-referrer`.
- Ghi nhận follow-up trước production: rate limit độc lập với email tồn tại và tách SMTP khỏi request transaction.
- Thêm `scripts/verify.sh` để mọi agent chạy cùng cổng kiểm tra an toàn mà không chạm datasource thật.

## File chính đã thay đổi

- `src/main/java/com/example/device/controller/AuthenticationController.java`
- `src/main/java/com/example/device/configuration/SecurityConfig.java`
- `src/main/java/com/example/device/configuration/JwtTokenVersionValidator.java`
- `src/main/java/com/example/device/service/impl/PasswordResetServiceImpl.java`
- `src/main/java/com/example/device/service/impl/Authenticationimpl.java`
- `src/main/java/com/example/device/model/User.java`
- `src/main/java/com/example/device/model/PasswordResetToken.java`
- `src/main/java/com/example/device/repository/PasswordResetTokenRepository.java`
- `src/main/resources/db/migration/V2__password_reset.sql`
- `src/main/resources/templates/email/password-reset.html`
- Các DTO, interface và test liên quan tới password reset.
- `frontend/src/pages/ForgotPasswordPage.tsx`
- `frontend/src/pages/ResetPasswordPage.tsx`
- `frontend/src/services/authService.ts`
- `frontend/src/App.tsx`
- `frontend/index.html`
- `PROJECT.md`
- `AGENTS.md`
- `CLAUDE.md`
- `GEMINI.md`
- `specs/password-reset/`
- `specs/README.md`
- `specs/_template/`
- `docs/decisions/001-password-reset-token-and-session-revocation.md`
- `docs/decisions/README.md`
- `docs/decisions/000-template.md`
- `scripts/verify.sh`

## Kiểm tra đã thực hiện

- `bash ./mvnw -DskipTests compile`: thành công.
- Chạy 28 unit/web-security test backend cô lập: tất cả thành công, không failure/error.
- Ngày 2026-09-29 chạy lại 15 test mục tiêu cho password reset/JWT/security với Byte Buddy javaagent: tất cả thành công, không failure/error.
- `git diff --check`: thành công.
- Chưa chạy `DeviceApplicationTests.contextLoads` vì test dùng datasource thật và có thể áp dụng Flyway lên Aiven.
- `npm run lint`: thành công.
- `npm run build`: thành công, TypeScript và Vite production build hoàn tất.
- Ngày 2026-09-30 chạy lại lint/build sau thay đổi URL/referrer: thành công.
- Ngày 2026-09-30 chạy `bash scripts/verify.sh safe`: tài liệu/diff đạt, backend compile đạt, 15/15 test mục tiêu đạt, frontend lint/build đạt.
- Chưa chạy luồng email/database/trình duyệt end-to-end.

## Việc tiếp theo

1. Review migration `V2__password_reset.sql`, sau đó áp dụng trên database test/staging tách khỏi Aiven đang dùng thật.
2. Cấu hình SMTP test và `PASSWORD_RESET_BASE_URL` cho môi trường kiểm thử.
3. Chạy các trường hợp trong ma trận `specs/password-reset/verification.md`.
4. Trước public production, hoàn tất PR-207/208/209 trong `tasks.md`.
5. Review diff cuối, cập nhật task/verification rồi mới commit.

## Lưu ý

- Không lưu mật khẩu hoặc reset token dạng rõ trong repository/log/response.
- File frontend chưa được theo dõi `frontend/src/services/deviceService.ts` đã tồn tại trước công việc này và không bị chỉnh sửa.
- `specs/password-reset/spec.md` là nguồn yêu cầu; `tasks.md` là nguồn trạng thái triển khai; `verification.md` là nguồn bằng chứng kiểm tra.
- Dùng `bash scripts/verify.sh safe` làm cổng kiểm tra mặc định trước khi bàn giao; script không chạy test cần datasource thật.
- Trong sandbox hiện tại, test dùng Mockito cần truyền Byte Buddy agent qua Surefire `argLine`; xem lệnh và kết quả trong `verification.md`.
