# AI Handoff

## Mục tiêu hiện tại

- Hoàn tất và kiểm chứng end-to-end luồng quên mật khẩu an toàn qua email cho Device.
- Đưa feature từ `Verifying` tới `Done` bằng bằng chứng database, email và trình duyệt trên môi trường test/staging tách biệt.

## Trạng thái hiện tại

- Backend, migration, email template và frontend đã được triển khai.
- `bash scripts/verify.sh safe` và `bash scripts/verify.sh integration` được chạy lại ngày 2026-10-08 lúc 13:55-13:58 +07: safe đạt toàn bộ, integration đạt 31/31 test trên MySQL Testcontainers.
- MySQL integration, Flyway V1/V2, full Compose frontend/backend/MySQL/Mailpit và Playwright Chromium browser E2E đã đạt local.
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

- MySQL Testcontainers/Compose, frontend container, Mailpit và browser E2E local đã có; chưa có clean-machine/CI run.
- Playwright Chromium E2E hiện điều khiển DOM thật và yêu cầu login thành công; PRD-107 đã đạt local. PRD-110 và các điều kiện Gate G1 còn lại chưa tự động đóng.
- `source-map-js` là dependency bắc cầu qua Vite/PostCSS, đã lên 1.2.2; `npm audit --audit-level=high` ngày 2026-10-09 báo 0 vulnerability.
- SMTP hiện đồng bộ trong request transaction, có thể tạo timing side-channel và giữ database lock khi gọi mạng.
- Cooldown theo user chưa thay thế rate limit theo IP/identity cho endpoint public.

## Việc tiếp theo

1. Push branch/mở PR và xác nhận ba job Device CI, đặc biệt `compose-smoke`, chạy xanh trên GitHub; không suy ra trạng thái CI từ file YAML.
2. Người dùng thực hiện PRD-110 theo `docs/runbooks/local-stack.md`.
3. Sau CI và xác nhận người dùng, cập nhật các điều kiện Gate G1 còn lại trước khi bắt đầu Giai đoạn 2.
4. Trước public production, hoàn tất PR-207/208/209 trong `tasks.md`.
5. Chỉ đóng Gate G1 sau khi browser E2E và CI có bằng chứng.

## Cảnh báo vận hành

- Không lưu mật khẩu hoặc reset token dạng rõ trong repository/log/response.
- Từ 2026-10-08, `DeviceApplicationTests` được ép dùng MySQL Testcontainers (`jdbc:tc`) và `scripts/verify.sh integration` bỏ biến DB môi trường, yêu cầu Docker trước khi chạy. Không đổi test sang Aiven/production khi Docker không có.
- Dùng `bash scripts/verify.sh safe` làm cổng mặc định; script không chạy test cần datasource thật.
- Nếu Mockito không tự attach được trong sandbox, dùng cách Byte Buddy javaagent đã ghi trong `verification.md`.

## Cập nhật tài liệu hệ thống ngày 2026-10-07

- Đã thêm `docs/diagrams/device-management.md` bằng tiếng Việt, gồm luồng trạng
  thái thiết bị và sequence diagram cho use case cấp phát thiết bị.
- Sơ đồ phản ánh đúng `DeviceState`, assignment, return và repair workflow hiện
  có; không thêm trạng thái chưa được code hỗ trợ.
- Đã thêm liên kết thư mục sơ đồ vào `PROJECT.md`.
- Đây là thay đổi tài liệu, không thay đổi code hoặc database.
- `bash scripts/verify.sh safe` ngày 2026-10-07 đã đạt: kiểm tra tài liệu/diff,
  backend compile, 15 test mục tiêu, frontend lint và build.

## Kế hoạch production-readiness đã được duyệt

- Người dùng đã yêu cầu tạo nguồn trạng thái chung để Codex và
  Antigravity/Gemini đồng hành theo hướng hệ thống vận hành được, chịu dữ liệu
  lớn và phục hồi sự cố.
- Bộ hồ sơ mức L nằm tại `specs/production-readiness/`, gồm `spec.md`,
  `plan.md`, `tasks.md` và `verification.md`.
- Người dùng duyệt ngày 2026-10-08, gồm contract phân trang mặc định
  `page=0`, `size=20`, tối đa `100`. Spec đã chuyển `Approved`; Codex đang bắt
  đầu Giai đoạn 0/1. Antigravity vẫn cần review frontend dependency `PRD-003`
  trước khi nhận task frontend.
- Password reset vẫn là feature đang `Verifying` và là dependency của Giai đoạn
  1/4; không được bỏ qua các task và bằng chứng còn mở trong
  `specs/password-reset/`.
- Codex sở hữu backend/database/tài liệu chung; Antigravity sở hữu `frontend/`;
  người dùng thực hiện các quyết định, thao tác cloud/secret và kiểm tra thủ công
  được đánh dấu `[Owner: User] [AI hướng dẫn]` trong `tasks.md`.

## Tiến độ triển khai production-readiness ngày 2026-10-08

- Giai đoạn 0 hoàn tất ở mức cấu hình/baseline: `bash scripts/verify.sh safe` đạt,
  15 test mục tiêu đạt, frontend lint/build đạt. Datasource test dùng
  Testcontainers và được ép ở `DeviceApplicationTests`.
- Đã thêm Dockerfile backend non-root, Compose MySQL/Mailpit/backend, `.env.example`,
  profile `dev`/`test`/`demo`, hướng dẫn tại `docs/runbooks/local-stack.md` và
  workflow Device CI ở Git root `.github/workflows/device-ci.yml`.
- Docker Engine 29.1.3 và Compose 2.40.3 đã hoạt động. `bash scripts/verify.sh
  integration` đạt 31 test trên MySQL 8.4.11 tạm; Flyway V1/V2 và password reset
  database integration đạt, không dùng Aiven/production.
- Compose backend/MySQL/Mailpit đang chạy healthy. Backend image chạy non-root
  `10001:10001`; backend/Mailpit chỉ bind loopback. `.env` local mode 600 bị Git
  ignore và không được ghi giá trị vào tài liệu.
- Lần start đầu phát hiện JDBC thiếu public-key retrieval; đã sửa chỉ trong URL
  Compose local. Smoke API đạt: OpenAPI/Mailpit 200, login 200, reset request 202,
  Mailpit nhận email, confirm 200, reuse token 400, email không tồn tại 202 và
  không tạo email. Token/mật khẩu không được in trong output.
- `bash scripts/verify.sh safe` chạy lại sau sửa và đạt toàn bộ compile, 15 test
  mục tiêu, frontend lint/build 92 module. Workflow CI chưa có run trên GitHub.
- Không tự đánh dấu Gate G1 hoặc feature password-reset Done trước các lần chạy
  MySQL/Mailpit/browser thực tế và bằng chứng tương ứng trong hai verification.

## Cập nhật tiếp quản ngày 2026-10-08 13:55-13:58 +07

- Đã đọc lại quy tắc, `PROJECT.md`, handoff, hai bộ spec, Git status/diff và giữ nguyên toàn bộ thay đổi có sẵn; không sửa `frontend/`.
- Docker Engine client/server 29.1.3, Compose 2.40.3; backend, MySQL và Mailpit đang `healthy`. Backend chạy `10001:10001`; OpenAPI và Mailpit UI loopback trả HTTP 200.
- MySQL Compose xác nhận Flyway V1/V2 thành công. `.env` bị ignore, mode `600`; `application-dev.yaml` không chứa credential.
- `bash scripts/verify.sh integration` đạt 31 test; `bash scripts/verify.sh safe` đạt compile, 15 test mục tiêu, frontend lint/build 92 module.
- Đã hoàn tất lại PR-501 (review bảo mật/migration) và ghi bằng chứng trong `specs/password-reset/verification.md`; không phát hiện secret production/raw token trong log hoặc cấu hình mới.
- Gate G1 vẫn mở vì browser E2E, frontend/CI run và xác nhận người dùng chưa có. Không bắt đầu Giai đoạn 2 khi G1 chưa đạt.

## Tích hợp frontend ngày 2026-10-08 16:19 +07

- Đã kiểm tra diff Antigravity: `PRD-003` và `PRD-103` có đủ bằng chứng; `PRD-109` được tích hợp vào script/workflow chung. Codex không sửa source frontend.
- Compose hiện gồm frontend Nginx bind loopback `5173`; cả bốn service healthy. SPA route, security headers và API smoke qua Nginx proxy đạt.
- `npm test`, lint và build đạt; live `bash scripts/verify.sh frontend-e2e` đạt, không skip.
- `bash scripts/verify.sh safe` đạt sau tích hợp; `bash scripts/verify.sh integration` đạt lại 31/31 test trên MySQL 8.4.11 Testcontainers và Flyway V1/V2.
- PRD-107 chưa được chấp nhận hoàn tất: test bàn giao là Node API/static test, chưa chạy trình duyệt hoặc DOM thật. PRD-110 và Gate G1 tiếp tục mở.
- CI job `compose-smoke` sinh credential tạm trong runner, không commit secret; workflow chưa có run GitHub.
- Không bắt đầu PRD-201 vì dependency Gate G1 chưa đạt. Backend/Codex tiếp theo chỉ có thể tiếp tục sau browser E2E và CI run, trừ khi người dùng duyệt thay đổi thứ tự gate.

## Chế độ phát triển backend bằng IntelliJ ngày 2026-10-09

- Đã thêm và kiểm chứng cấu hình hybrid: Docker chạy MySQL/Mailpit, IntelliJ chạy backend bằng profile `dev,ide`, frontend chạy Vite riêng.
- `compose.ide.yaml` publish MySQL `127.0.0.1:3307` và SMTP `127.0.0.1:1025`; `.run/Device Backend (IDE).run.xml` không chứa secret và profile `ide` đọc `.env` local.
- Dùng `bash scripts/dev-ide.sh up`, sau đó chọn `Device Backend (IDE)` trong IntelliJ. Dùng `bash scripts/dev-ide.sh full` để trở lại full Compose.
- Cấu hình IntelliJ cá nhân cũ `Unnamed` thiếu `DB_URL` và có key ` DB_USERNAME` thừa khoảng trắng; không tiếp tục dùng cấu hình đó.
- Runtime pass ngày 2026-10-09: `scripts/dev-ide.sh up` đưa MySQL/Mailpit về healthy; backend `dev,ide` khởi động trên 8080, kết nối MySQL loopback 3307, Flyway xác nhận schema V2 và `/v3/api-docs` trả HTTP 200. Tiến trình Maven kiểm tra đã được dừng graceful; MySQL/Mailpit được để chạy cho phiên IntelliJ tiếp theo.
- `bash scripts/verify.sh safe` sau tích hợp hybrid: exit 0; 15 backend test, 3 frontend test, lint và build đều đạt.

## Tích hợp Playwright browser E2E ngày 2026-10-09

- Antigravity bổ sung `frontend/e2e/auth-password-reset.spec.ts`, `frontend/playwright.config.ts` và Playwright dependency; Codex review và chạy lại trên full Compose.
- `bash scripts/verify.sh frontend-e2e`: exit 0; 1 Chromium test pass trong 1.1 phút. Luồng DOM thật gồm login thành công, forgot password, lấy link Mailpit, xóa token khỏi URL, kiểm tra `Referer`, reset, login mật khẩu mới, từ chối mật khẩu cũ và khôi phục mật khẩu ban đầu.
- `bash scripts/verify.sh safe` sau tích hợp: exit 0; backend compile, 15 backend test, 3 frontend test, lint và build 92 module đều đạt.
- `bash scripts/verify.sh integration` sau tích hợp: exit 0; 31 test, MySQL 8.4.11 Testcontainers và Flyway V1/V2 đạt.
- `source-map-js@1.2.2` là dependency bắc cầu `vite -> postcss`; `npm audit --audit-level=high` báo 0 vulnerability.
- `.github/workflows/device-ci.yml` đã có bước cài Chromium và system dependencies trong `compose-smoke`; GitHub đã chạy được qua cài browser và dựng full Compose trước khi phát hiện race của test.
- PR #1 đã mở. Hai run đầu có `safe`/`integration` pass nhưng `compose-smoke` fail tại bước reset khôi phục do thiếu chờ route SPA; test đã được sửa để đợi `/forgot-password`, credential tạm trong workflow cũng đã được mask trước `GITHUB_ENV`.
- Chromium E2E local sau race fix vẫn pass `1 passed (1.1m)`; cần xem rerun GitHub trên commit sửa trước khi tuyên bố `compose-smoke` xanh.
