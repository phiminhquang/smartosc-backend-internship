# AI Handoff

## Mục tiêu hiện tại

- Giai đoạn 3, 4 và 5 đã hoàn tất; Gate G5 đạt local. Phương án demo CV 0đ hiện tại là Render Free Web Service + Aiven MySQL Free + Render Static Site + Resend HTTPS API; Koyeb/SMTP cloud đã bị loại khỏi phương án. PRD-602 đến PRD-605 đã hoàn tất phần chuẩn bị source/local verification gồm backend, frontend, CI/CD, rollback và backup/restore guard. Aiven MySQL 8.4.8 đang Running với database riêng `device_demo`, Render Dashboard đã có nhưng chưa tạo service, và người dùng đã nhận email onboarding Resend tại đúng một địa chỉ. PRD-601/606 còn resource/secret/migration/deploy cloud; PRD-607/608 còn restore/browser/email drill thật.
  - Worker mặc định tắt qua `EMAIL_JOB_WORKER_ENABLED=false` nhưng profile local/dev bật. Không còn producer gọi SMTP trực tiếp trong transaction nghiệp vụ; rate-limit counter dùng MySQL chung giữa các instance.

## Trạng thái hiện tại

- Backend, migration và email template hiện có đã được triển khai. Thay đổi PRD-506/603 trong frontend do Antigravity thực hiện; Codex review/tích hợp và sửa riêng schema Blueprint PRD-603 từ `type: static` sang `type: web` + `runtime: static` theo tài liệu Render chính thức.
- PRD-209 đã merge qua PR #5 vào `main` tại `681ab42`; sáu check push/PR đều xanh. Người dùng đã xác nhận PRD-210 bằng benchmark 1k/10k/100k thành công trong 8 phút 39 giây, không OOM.
- PRD-301 đến PRD-304 và PRD-307 đã merge qua PR #6 vào `main` tại `bf2add0`; commit implementation là `c66ab22`.
- `ConcurrencyCorrectnessIntegrationTest` bao phủ assign, return, extension approve/reject và repair complete/unrepairable. Mỗi race có đúng một HTTP 200 và một HTTP 409, sau đó đọc lại database xác nhận trạng thái nhất quán.
- `bash scripts/verify.sh integration` ngày 2026-10-10 đạt 46/46 test trên MySQL 8.4.11, Flyway V1/V2/V3; tổng thời gian 2 phút 27 giây. Cổng `bash scripts/verify.sh safe` cuối cùng cũng đạt: 20 backend test, generator/benchmark guards, 3 frontend test, lint/build 92 module.
- PR #6 có đủ sáu check push/PR xanh. Post-merge Device CI run `38040530264` trên `main` cũng đạt `safe` 47 giây, `integration` 1 phút 18 giây và `compose-smoke` 2 phút 12 giây.
- PR #3 đã merge PRD-206/207 vào `main` tại `9b9e4fd`; post-merge Device CI run `37910483706` đạt `safe` 35 giây, `integration` 1 phút 18 giây và `compose-smoke` 2 phút 34 giây.
- Giai đoạn 2 đã hoàn tất. Review frontend xác nhận không có màn hình/API call cho assignment, return, extension hoặc repair; các form hiện có đã có double-submit guard, nên PRD-305 và G3-3 đóng N/A mà không sửa `frontend/`.
- Người dùng chạy lại `bash scripts/verify.sh integration` cho PRD-306 ngày 2026-10-10: 46/46 test đạt, không failure/error/skip, `BUILD SUCCESS` trong 1 phút 53 giây. Full suite chứa bốn race scenario và assertions đọc lại MySQL Testcontainers.
- Giai đoạn 3, 4 và 5 đã hoàn tất. Chưa sẵn sàng public production vì Giai đoạn 6-7 còn mở.
- ADR-002 được người dùng Accepted ngày 2026-10-10 với batch 20, lease 60 giây, retry và retention 30/90 ngày. PRD-402 đã đóng.
- PRD-403 thêm Flyway V4 `email_jobs`, entity/enums/repository và claim service dùng `FOR UPDATE SKIP LOCKED`. Focused test đạt 1/1; full integration đạt 47/47 trên MySQL 8.4.11/Flyway V1-V4 trong 3 phút 16 giây.
- PRD-404 thêm worker gửi batch song song ngoài transaction, stable Message-ID, retry có jitter/giới hạn, terminal states, lease recovery sang `UNKNOWN`, redaction và cleanup 30/90 ngày. Focused integration đạt 7/7; full suite đạt 56/56 trong 3 phút 03 giây; cổng `safe` đạt.
- PRD-405 thêm Flyway V5 và password reset producer nguyên tử, AES-256-GCM với key version/nonce, expiry/terminal token invalidation và worker dựng link chỉ trong bộ nhớ. `DeviceApplicationTests` đạt 4/4; full integration đạt 64/64 trên MySQL 8.4.11/Flyway V1-V5 trong 2 phút 42 giây; cổng `safe` đạt 20 backend test, 3 frontend test, lint/build.
- PRD-406 thêm enqueue service chung và Flyway V6 cho assignment job; reminder/overdue marker chỉ được đặt sau `SENT`, retry giữ marker rỗng và job stale đi `EXPIRED`. Reminder, overdue, daily report và hai email extension đều dùng outbox; full integration đạt 74/74 trên MySQL 8.4.11/Flyway V1-V6 trong 3 phút 43 giây; cổng `safe` đạt. PRD-408 đóng N/A vì HTTP/UX contract không đổi và không sửa frontend.
- PRD-407 thêm Flyway V7 `rate_limit_buckets`, fixed-window theo IP/identity cho login/reset, SHA-256 bucket key, atomic upsert, 429/code 1057 + `Retry-After` và cleanup. Full integration cuối đạt 80/80 trong 2 phút 54 giây trên MySQL 8.4.11/Flyway V1-V7; 20 transaction đồng thời không mất increment; cổng `safe` đạt 25 backend test cùng frontend lint/build. PR-207 và G4-3 đã đóng; frontend hiện có xử lý message 429 nên PRD-408 giữ N/A.
- PRD-409 được người dùng xác nhận ngày 2026-10-11 trên full Compose: khi Mailpit dừng, job ở `RETRY_WAIT`; sau khi bật lại, cùng job chuyển `SENT` ở lần thử thứ 3. Database có đúng một job, Mailpit API và kiểm tra trực quan đều có đúng một email; log không thấy raw token/payload. G4-1/G4-2 đã đóng theo contract, không nâng thành cam kết SMTP exactly-once tuyệt đối.
- PRD-410/PR-209 ban đầu phát hiện timing leak rõ: median existing/missing 86,052/48,330 ms. Sau `PasswordResetTimingGuard` mặc định 150 ms + jitter 0-25 ms, benchmark 50 cặp đạt 175,697/170,486 ms, lệch 5,210 ms, ratio 1,031, dominance 0,560. Full integration đạt 83/83; safe đạt 28 backend test cùng frontend lint/build. PR-207/208/209 và Giai đoạn 4 đã đóng.
- PRD-501 xác nhận production Java không còn `System.out`/`System.err`/`printStackTrace`, chuẩn hóa API exception, email worker và scheduler thành log `event` + key/value theo allow-list. Hai redaction test xác nhận exception message, recipient, payload, mật khẩu, JWT và reset token không lọt log; `safe` đạt 30 backend test cùng frontend lint/build. Full integration chưa chạy lại vì Docker daemon không truy cập được; task này không đổi database.
- PRD-502 thêm `X-Request-ID` validate/sinh UUID, response header, error-body `requestId`, request completion/error logs và Flyway V8 `email_jobs.request_id` + index. Enqueue/worker log cùng request/job ID; MySQL integration password reset xác nhận ID đi từ request context vào row rồi sang worker. Full integration cuối đạt 87/87 trên Flyway V1-V8; safe đạt 32 backend test cùng frontend lint/build.
- Full integration PRD-502 lần đầu có 1 failure do scheduler thật chạy trong test và enqueue thêm reminder, không phải duplicate delivery. `app.scheduler.enabled` mặc định bật, profile test tắt để cô lập; lần chạy lại đạt toàn bộ.
- PRD-503 thêm Actuator và chỉ expose health; health/liveness/readiness công khai trả trạng thái tối thiểu, readiness kiểm tra application state + database và không phụ thuộc SMTP async. Actuator index/Prometheus ẩn danh trả 401. Full integration đạt 88/88, safe đạt và backend Compose rebuild dùng readiness healthcheck đã healthy.
- PRD-504 thêm request histogram, datasource/JVM binders và `OperationalMetrics` cho scheduler/email job với tag hữu hạn. Prometheus registry có sẵn nhưng endpoint chưa expose trước PRD-505. Safe đạt 33 backend test; full integration đạt 89/89 trên MySQL/Flyway V1-V8.
- PRD-506 do Antigravity thực hiện trong `frontend/`: body `requestId` ưu tiên, fallback `X-Request-ID`, error message hiển thị “Mã tham chiếu”. Codex review diff và cổng frontend test/lint/build đạt, không sửa source frontend.
- PRD-505 thêm Compose profile Prometheus/Grafana local. Backend management port 8081 chỉ nằm trong Compose network; Prometheus target `device-backend` đạt `up`, query email-job có đủ 7 status và Grafana provision dashboard 8 panel UID `device-operations`. API port 8080 trả 401 cho health/Prometheus khi monitoring bật; endpoint ngoài allow-list trên 8081 cũng trả 401. Safe đạt 33/33 backend test cùng frontend test/lint/build; focused `DeviceApplicationTests` đạt 6/6 trên MySQL/Flyway V1-V8.
- PRD-507 thêm runbook `docs/runbooks/operations-troubleshooting.md` cho API chậm, DB pool cạn và scheduler/email job kẹt. PromQL và truy vấn MySQL metadata đã chạy thử trên local stack; runbook không đọc recipient/payload/secret/SQL text và cấm sửa trạng thái outbox thủ công.
- PRD-508 do người dùng thực hiện: lỗi đăng nhập test hiển thị mã tham chiếu, cùng ID khớp log rejection/completion HTTP 401 và người dùng xác nhận đã lưu ảnh dashboard. PRD-509 review source/test/runtime đạt; host không truy cập 8081, API Actuator 401, nội bộ chỉ health/Prometheus 200 và info 401. Gate G5 đã đóng.
- PRD-602 ngày 2026-10-12 đã đóng: thêm `docker/Dockerfile.demo`, entrypoint tạo CA/truststore runtime, profile demo Hikari 5/email thread-batch 2/Tomcat 16, CORS allow-list chính xác và transport Resend HTTPS. `EmailJobSenderImpl` tách render khỏi transport; SMTP vẫn là mặc định local/dev, còn demo dùng Resend với recipient allow-list, stable idempotency key, timeout và retry/permanent classification. Safe đạt 45/45, full MySQL integration 99/99 trên Flyway V1-V8. Image cuối `3b3fbf302cc0` smoke 512 MiB/0,1 CPU đạt TLS/Resend config/readiness/login/devices/CORS/security; readiness 425 giây, mẫu RAM 268,3 MiB, không OOM. Chưa deploy cloud, nhập secret, gọi Resend thật từ ứng dụng hoặc sửa frontend.
- PRD-603 ngày 2026-10-12 đã đóng cho phần chuẩn bị frontend: Render Static Site build/publish, monorepo root, SPA rewrite, security headers, placeholder/backend URL workflow và hướng dẫn Blueprint Path đã có. `bash scripts/verify.sh frontend` đạt 3/3 test, Oxlint và Vite build 92 module. Chưa tạo Static Site hoặc kiểm tra trình duyệt trên cloud.
- PRD-602 đến PRD-605 cùng các thay đổi production-readiness tích lũy đã được review và commit trên `feature/frontend-prd603` tại `7640718` (`feat(device): harden production readiness and demo deployment`), sau đó push và mở PR #8. Trước commit, `safe` đạt 45/45 backend test và frontend gate; full integration chạy lại đạt 99/99 trên MySQL 8.4.11/Flyway V1-V8 trong 4 phút 06 giây; secret scan, workflow/render YAML, mock post-deploy và diff check đều đạt. GitHub push run `38219844010` và pull-request run `38219933811` đều xanh `safe`/`integration`/`compose-smoke`; deploy job skip đúng thiết kế trên feature branch. Chưa gọi Render/Aiven, tạo dump hoặc restore thật. PRD-606/607/608/609 và Gate G6 vẫn mở.
- Backend/frontend/MySQL/Mailpit/Prometheus/Grafana đang chạy; backend và frontend healthy. Grafana/Prometheus chỉ bind loopback, 8081 không publish ra host. Đây là cấu hình local, không dùng anonymous Viewer cho Internet/demo.
- Phân công mặc định và ranh giới chỉnh sửa tuân theo `AGENTS.md`; hiện không có ngoại lệ đang hoạt động.

## Nguồn sự thật cần đọc

- Yêu cầu và API contract đang hoạt động: `specs/production-readiness/spec.md`.
- Thiết kế triển khai và rollback: `specs/production-readiness/plan.md`.
- Trạng thái công việc và blocker: `specs/production-readiness/tasks.md`.
- Bằng chứng kiểm tra duy nhất: `specs/production-readiness/verification.md`.
- Quyết định bảo mật lâu dài: `docs/decisions/001-password-reset-token-and-session-revocation.md`.
- Quyết định email nền đã Accepted: `docs/decisions/002-durable-email-outbox.md`.
- Quy tắc project và cổng kiểm tra: `AGENTS.md` và `scripts/verify.sh`.

## Phạm vi thay đổi PR #6

- Backend: `ErrorCode`, `AssignmentServiceImpl` và unit test tương ứng.
- Integration: `ConcurrencyCorrectnessIntegrationTest` mới cho bốn race scenario.
- Tài liệu chung: `spec.md`, `tasks.md`, `verification.md` của production-readiness và file handoff này.
- Không sửa `frontend/`, migration hoặc cấu hình database. Dùng Git diff để lấy danh sách file chính xác.

## Blocker và rủi ro còn lại

- MySQL Testcontainers/Compose, frontend container, Mailpit và browser E2E đã đạt local và GitHub hosted runner sạch.
- Playwright Chromium E2E điều khiển DOM thật và yêu cầu login thành công; PRD-107, các tiêu chí tự động Gate G1 và kiểm tra thủ công PRD-110 đều đã đạt.
- `source-map-js` là dependency bắc cầu qua Vite/PostCSS, đã lên 1.2.2; `npm audit --audit-level=high` ngày 2026-10-09 báo 0 vulnerability.
- Không còn điểm gọi SMTP trực tiếp trong các producer password reset, assignment hoặc extension. Delivery thật đã được xác nhận bằng diễn tập tắt/bật Mailpit PRD-409.
- Timing guard giữ request reset hợp lệ trong cửa sổ mặc định 150-175 ms. Kết quả local đạt nhưng phải đo lại trên hạ tầng deploy nếu latency database thường xuyên vượt 150 ms; shared rate limit không được tắt để dựa riêng vào jitter.
- SMTP thuần không có exactly-once delivery tuyệt đối; worker giữ stable Message-ID và chuyển lease mơ hồ sang `UNKNOWN` thay vì retry mù. PRD-409 chỉ xác nhận kịch bản lỗi kết nối/recovery đã chạy không tạo thư trùng.
- Resend API giảm phụ thuộc SMTP cloud nhưng vẫn không tạo cam kết exactly-once tuyệt đối. Demo bắt buộc recipient allow-list và mặc định chỉ gửi tới đúng email chủ tài khoản đã xác nhận; onboarding email không thay thế kiểm tra email từ ứng dụng sau deploy.

## Việc tiếp theo

1. Review toàn bộ working tree, commit và push source; hiện branch `feature/frontend-prd603` vẫn chưa có commit riêng nên Render chưa thể thấy bất kỳ thay đổi mới nào.
2. Người dùng làm PRD-601/606 theo đúng thứ tự runbook: tạo Render Static Site bằng placeholder để có frontend origin, tạo Render Web Service bằng Docker với secret store, duyệt migration, cập nhật frontend sang backend URL, cấu hình GitHub environment `demo`, rồi mới đặt `DEMO_DEPLOY_ENABLED=true`. Không gửi secret qua chat/repository, không reset Aiven hoặc xóa database cũ.
3. Người dùng làm PRD-607/608: tạo backup, restore vào database rỗng `device_demo_restore_*`, kiểm tra dữ liệu/login; sau đó browser/email cloud smoke. PRD-609 tích hợp bằng chứng deploy/restore/rollback. Gate G6 chỉ đóng sau các lần chạy thật.

## Cảnh báo vận hành

- Demo image dùng C1 JIT ưu tiên startup, có thể giảm peak throughput. Phép thử local hard cap 0,1 CPU cần 425 giây đến readiness; không suy ra cold-start Render từ số này. Khi deploy thật cần grace phù hợp, kiểm tra wake-up/readiness và mở sẵn trước trình diễn.
- Không lưu mật khẩu hoặc reset token dạng rõ trong repository/log/response.
- Rate-limit bucket không lưu email/IP rõ. Deployment sau reverse proxy phải giới hạn backend sau proxy tin cậy; ứng dụng dùng `server.forward-headers-strategy=native` để lấy client IP.
- Password-reset response window mặc định 150 ms + jitter 0-25 ms chỉ che chênh lệch đã đo khi latency nghiệp vụ nằm dưới floor; phải benchmark lại trên môi trường deploy và không tắt shared rate limit.
- Local/Compose/IDE cần thêm `LOCAL_EMAIL_JOB_SECRET_KEY` là đúng 32 byte mã hóa Base64, độc lập với JWT; có thể tạo bằng `openssl rand -base64 32`. Không xóa key version cũ khi còn job dùng nó.
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
- Rerun GitHub thứ hai vẫn fail ở riêng reset khôi phục tùy chọn. Mô phỏng CI local với project/volume tạm và credential ngẫu nhiên đạt; test đã được tách để chỉ khôi phục local DB bền vững, còn `CI=true` bỏ reset lần hai vì database runner là tạm. Cần xác nhận rerun tiếp theo.
- `CI=true bash scripts/verify.sh frontend-e2e` sau thay đổi đạt `1 passed (6.4s)` trên full Compose local.
- GitHub commit `5837be2`: `safe`, `integration` và `compose-smoke` đều pass ở cả push run và PR run. PR #1 đang mở.

## Xác nhận thủ công PRD-110 ngày 2026-10-09

- Người dùng xác nhận full Compose có bốn container healthy và Mailpit nhận email reset.
- Trên trình duyệt, link mở được, token biến mất khỏi address bar, reset thành công, đăng nhập bằng mật khẩu mới thành công và link cũ bị từ chối.
- PRD-110 đã được đánh dấu hoàn thành trong `specs/production-readiness/tasks.md`; bằng chứng được ghi tại `specs/production-readiness/verification.md`. Không lưu mật khẩu hoặc token.

## Khởi động Giai đoạn 2 ngày 2026-10-09

- Tạo branch `feature/pagination-data-scale` từ merge commit `f5eef03`.
- Cập nhật GitHub Actions từ v4 lên v5 theo cảnh báo runtime Node 20/setup-java deprecated và release notes chính thức. Branch CI run `37901442587` đạt cả `safe`, `integration`, `compose-smoke`; cảnh báo Node 20/setup-java v4 đã biến mất.
- Ba job được ghim `ubuntu-24.04` sau khi GitHub cảnh báo `ubuntu-latest` sẽ chuyển sang Ubuntu 26 từ 2026-10-19. Branch CI run `37901920546` xác nhận `safe`, `integration`, `compose-smoke` đều đạt và không còn cảnh báo đổi runner.
- Codebase graph và source review xác nhận tám endpoint collection ở users, assignments, repairs và extension requests đang trả `List`; frontend chưa gọi các endpoint này.
- PRD-201 đã ghi contract proposed trong `specs/production-readiness/spec.md`, gồm page/size, response ổn định, filter/sort allow-list, error contract, tương thích và nguyên tắc query/index.
- Video tham khảo được chuyển lời bằng Groq Whisper sau khi hai nguồn caption trả rỗng. Các ý deep offset, deterministic sort, `EXPLAIN` và page-ID deferred join chỉ được dùng làm giả thuyết cho benchmark, không phải bằng chứng hiệu năng của Device.
- Blocker PRD-202 tại mốc khởi động đã được người dùng gỡ ngày 2026-10-09; trạng thái triển khai mới nhất nằm ở mục kế tiếp.

## Phân trang backend PRD-202 đến PRD-205 ngày 2026-10-09

- Người dùng đã duyệt breaking response cho tám endpoint và xác nhận không tạo màn hình frontend mới; PRD-202 hoàn tất.
- Thêm `PageResult`, validation chung, sort allow-list + `id` tie-breaker và filter theo contract cho users, assignments, repairs và extension requests. Devices giữ response cũ nhưng dùng chung giới hạn page/size và keyword.
- Users phân trang trước rồi fetch roles theo ID để không page trên collection fetch join; các collection to-one dùng specification + entity graph.
- Input sai trả HTTP 400/code 1055. `PaginationApiIntegrationTest` bao phủ page boundary, filter/sort, tám endpoint và input lỗi trên MySQL 8.4.11 Testcontainers.
- `bash scripts/verify.sh integration` đạt 34/34 test, Flyway V1/V2 đạt. `scripts/verify.sh` dùng Byte Buddy javaagent từ Maven cache cho cả targeted và full integration khi có.
- GitHub Device CI run `37904867768` tại commit `ea1de52` đạt đủ `safe`, `integration` và `compose-smoke` trên Ubuntu 24.04.
- Frontend và `.env` không bị sửa. PRD-205 đóng N/A vì frontend hiện không tiêu thụ tám endpoint này.
- Bước tiếp theo: PRD-206 thiết kế import/export lớn và PRD-207 tạo data generator an toàn; chưa thêm migration index trước baseline PRD-208.

## Import/export và data generator PRD-206/207 ngày 2026-10-09

- PR #2 merge vào `main` tại `d34854f`; post-merge run `37906680884` đạt `safe` 37 giây, `integration` 1 phút 01 giây và `compose-smoke` 2 phút 17 giây.
- Tạo nhánh `feature/device-file-scale`. Contract HTTP ba endpoint file được giữ nguyên; export CSV/XLSX chuyển từ `findAll()` + toàn bộ `byte[]` sang response streaming và keyset batch `id,asc`.
- XLSX dùng `SXSSFWorkbook` với row window hữu hạn và cleanup file tạm. Import CSV giới hạn 10 MiB, kiểm tra header/BOM, đọc tuần tự và flush/clear theo batch trong transaction nguyên tử.
- Thêm generator CSV tổng hợp chỉ ghi filesystem, giới hạn 1 đến 1.000.000 row và từ chối ghi đè mặc định; không đọc `.env`, không có code database/mạng. Runbook: `docs/runbooks/device-data-scale.md`.
- `bash scripts/verify.sh safe` đạt: 20 backend test, generator check, 3 frontend test, lint/build 92 module. Full integration cuối đạt 41/41 test trên MySQL 8.4.11 Testcontainers và Flyway V1/V2, gồm rollback toàn import sau khi batch đầu đã flush.
- Device CI run `37909236650` tại commit `cf5d95a` đạt `safe` 44 giây, `integration` 1 phút 05 giây và `compose-smoke` 2 phút 20 giây trên Ubuntu 24.04.
- Chưa sửa `frontend/`, `.env` hoặc migration/index. PRD-208/209 vẫn mở vì chưa chạy dataset lớn, peak heap/RSS/latency hay `EXPLAIN ANALYZE`.

## PRD-208 benchmark ngày 2026-10-09

- PR #3 `feat(device): stream device import and export` đã merge tại `9b9e4fdfb1fd5a76d43178e9b541d3e1555931da`. PR CI run `37910092843` đạt `safe` 41 giây, `integration` 1 phút 07 giây, `compose-smoke` 2 phút; post-merge run `37910483706` cũng xanh toàn bộ như ghi ở đầu file.
- Branch hiện tại: `feature/device-scale-benchmark`. Working tree cố ý chưa commit:
  - modified: `AI-HANDOFF.md`, `docs/runbooks/device-data-scale.md`, `scripts/verify.sh`, `specs/production-readiness/spec.md`, `specs/production-readiness/tasks.md`, `specs/production-readiness/verification.md`;
  - untracked: `docs/benchmarks/device-scale-baseline-2026-10-09.md`, `scripts/benchmark-device-scale.sh`, `src/test/java/com/example/device/DeviceScaleBenchmarkIT.java`.
- Harness chỉ chạy khi có `--confirm-isolated`, script bỏ toàn bộ biến `DB_*`/`SPRING_DATASOURCE_*`, còn test xác nhận URL cấu hình là `jdbc:tc:mysql` và database thật là `device_test`. Không đọc/sửa `.env`, không sửa `frontend/`, `PaginationSupport.java`, production code hoặc migration/index.
- Dataset dùng `scripts/generate-device-csv.sh`; mặc định 1k/10k/100k. JVM benchmark cố định `-Xms128m -Xmx512m`. Read/export warm-up 1 và ghi 3 lần; import warm-up hạ tầng rồi ghi 1 lần/dataset vì chi phí cao. Sampler lấy JVM heap và Linux VmRSS mỗi 10 ms.
- Các operation hiện có: import CSV, page đầu, deep page, keyword cuối dataset, CSV export, XLSX export. Report lưu raw measurement, summary và `EXPLAIN ANALYZE` trước index cho deep-page data/count và keyword data/count.
- Safety/compile đã đạt: `bash -n scripts/benchmark-device-scale.sh scripts/verify.sh`; benchmark từ chối chạy khi thiếu `--confirm-isolated`; `bash ./mvnw -DskipTests test-compile` thành công. `scripts/verify.sh safe` chưa được chạy lại sau thay đổi PRD-208.
- Smoke 100 row/1 repetition trên MySQL 8.4.11 Testcontainers đạt và report ở `/tmp/device-benchmark-smoke.md` (không thuộc repository). Lần đầu dừng đúng safety guard do Hikari che URL; guard đã được sửa dùng Spring Environment rồi rerun thành công.
- Benchmark 1.000 row với cấu hình cuối đạt, report ở `/tmp/device-benchmark-1k.md`: import 6.888 giây, peak heap delta 58 MiB, RSS delta 47,219 MiB; median page đầu 67,065 ms, deep page 55,310 ms, keyword-tail 33,381 ms, CSV export 77,161 ms, XLSX export 257,755 ms. Plan hiện cho thấy deep page và keyword dùng table scan/sort ở 1.000 row. Đây chỉ là smoke/ước lượng, chưa phải full PRD-208.
- Full baseline 1k/10k/100k đã đạt bằng lệnh:

  ```bash
  bash scripts/benchmark-device-scale.sh \
    --confirm-isolated \
    --output target/benchmarks/device-scale-baseline.md
  ```

- Full run exit 0 trong 6 phút 10 giây. Report nguyên vẹn đã được lưu ở `docs/benchmarks/device-scale-baseline-2026-10-09.md`; `tasks.md` và `verification.md` đã cập nhật PRD-208. Tóm tắt 100k: import 225,443 giây; median page đầu/deep/keyword 438,772/616,170/378,796 ms; CSV/XLSX export 1.669,511/6.806,224 ms; không OOM với heap 512 MiB.
- Plan 100k xác nhận deep page scan+sort 100k row (~503 ms), keyword data/count table scan (~332/313 ms). Chưa thêm index; đây là đầu vào cho PRD-209.
- Gate local sau full baseline đều đạt: `bash scripts/verify.sh safe` có 20 backend test, generator/benchmark guard, frontend test/lint/build; `bash scripts/verify.sh integration` đạt 41/41 test trên MySQL 8.4.11 Testcontainers và Flyway V1/V2.
- Commit implementation/report `cd1ccbb`; GitHub push run `37940116019` đạt `safe` 39 giây, `integration` 1 phút 12 giây và `compose-smoke` 5 phút 39 giây.
- Việc còn lại trên branch này: commit bằng chứng CI, push, mở/merge PR PRD-208 sau PR CI. Sau khi merge mới tạo nhánh PRD-209.

## PRD-209 index truy vấn thiết bị ngày 2026-10-10

- Branch `feature/device-query-index` từ merge commit PR #4 `3179a16`; implementation/report commit local `37333db` (`perf(device): add measured name index`).
- Benchmark 100k trước/sau dùng hai chu kỳ, mỗi operation warm-up 2 và ghi 3 lần. Report lưu tại `docs/benchmarks/device-query-index-comparison-2026-10-10.md`.
- Median trang đầu 447,520 -> 50,178 ms, cải thiện 88,79%; `EXPLAIN ANALYZE` đổi từ table scan + sort sang index scan `idx_devices_name`. Deep page giảm 2,71%; keyword cải thiện 9,45%, nên không claim hai operation này được tối ưu.
- Thêm Flyway `V3__device_name_index.sql` với `devices(name)`. Keyword dùng `ORDER BY LOWER(name), id` để tránh regression observed khi optimizer chọn ordered index scan cho leading-wildcard; mixed-case integration test đạt.
- Harness có guard `--confirm-isolated`, bỏ datasource env, xác nhận `jdbc:tc:mysql`/`device_test`, seed multi-row 1.000 row/lô và chỉ drop/recreate index trong database Testcontainers dùng một lần.
- `bash scripts/verify.sh safe` exit 0: 20 backend test, hai benchmark/generator guards, 3 frontend test, lint và build 92 module đạt.
- `bash scripts/verify.sh integration` exit 0: 42/42 test đạt trên MySQL 8.4.11; Flyway V1/V2/V3 và `idx_devices_name` được xác nhận.
- Còn lại: push/CI/PR sau khi khôi phục GitHub auth; PRD-210 chỉ người dùng được đánh dấu sau xác nhận.
