# Công việc: Hoàn thiện hệ thống để vận hành, chịu tải và phục hồi sự cố

## Trạng thái tổng thể

- Spec: Approved ngày 2026-10-08.
- Implementation: Giai đoạn 0/1/2/3/4/5 và phần chuẩn bị source/local của PRD-602/603/604/605 đã hoàn tất. Phương án demo CV 0đ hiện tại là Render Free Web Service + Aiven MySQL Free + Render Static Site + Resend HTTPS API. Aiven đang Running với database riêng `device_demo`; tài khoản Render đã có nhưng chưa tạo service. Người dùng đã xác nhận email onboarding Resend tới đúng một email; PRD-601/606 còn resource, secret, migration và deploy cloud; PRD-607/608 còn restore/browser/email drill thật.
- Verification: Cổng `safe` mới nhất có 45 backend test và logging guard; full MySQL integration 99/99 với Flyway V1-V8; image demo 512 MiB/0,1 CPU đã qua TLS/Resend config/readiness/login/devices/CORS/security smoke; correlation UI → log, health, metrics, dashboard 8 panel và security boundary management endpoint đã đạt local; benchmark timing 50 cặp, benchmark 100k do người dùng xác nhận, bốn race scenario backend, rate-limit concurrency 20 transaction, Playwright Chromium E2E, SMTP failure/recovery không gửi trùng và GitHub CI đến PR #6 đều đạt.
- Feature password reset vẫn ở trạng thái Verifying vì các integration case PR-405/406/407 riêng còn mở; các dependency PR-207/208/209 của Giai đoạn 4 đã hoàn tất.

## Ký hiệu owner và cách làm

- `[Owner: Codex] [AI thực hiện]`: Codex được phép sửa backend/cấu hình/tài liệu trong phạm vi sau khi spec Approved.
- `[Owner: Antigravity] [AI thực hiện]`: Antigravity chỉ sửa `frontend/` sau khi contract/dependency backend sẵn sàng.
- `[Owner: User] [AI hướng dẫn]`: người dùng phải quyết định hoặc thao tác; AI cung cấp checklist/lệnh và kiểm tra kết quả.
- `[Owner: Integration]`: Codex ghép kết quả, chạy cổng kiểm tra và cập nhật tài liệu chung.

## Quy tắc bắt đầu phiên cho cả hai AI

- Đọc `PROJECT.md`, `AGENTS.md`, `AI-HANDOFF.md` và toàn bộ `specs/production-readiness/`.
- Đọc `git status` và diff liên quan; không ghi đè thay đổi có sẵn.
- Chỉ nhận task có đúng owner và mọi dependency đã đạt.
- Không sửa cùng file/cùng working tree khi làm song song; dùng branch/worktree riêng.
- Antigravity không sửa file chung; gửi kết quả để Codex tích hợp hoặc làm trên worktree riêng.
- Chỉ Codex/Integration cập nhật trạng thái task và verification chung sau khi kiểm tra bằng chứng.

## Giai đoạn 0 — Duyệt và baseline

- [x] PRD-001 `[Owner: Codex] [AI thực hiện]` Khảo sát tài liệu, kiến trúc, API collection, email, test, migration và khoảng trống hạ tầng hiện tại.
- [x] PRD-002 `[Owner: Codex] [AI thực hiện]` Tạo `spec.md`, `plan.md`, `tasks.md`, `verification.md` cho feature mức L.
- [x] PRD-003 `[Owner: Antigravity] [AI thực hiện]` Review contract/frontend dependency hiện tại. Phạm vi xác nhận khi integration: `LoginPage`, `ForgotPasswordPage`, `ResetPasswordPage`, route trong `App.tsx`, `AuthContext`, `authService` và cấu hình `http`.
- [x] PRD-004 `[Owner: User] [AI hướng dẫn]` Duyệt phạm vi, ngoài phạm vi, thứ tự phase và contract phân trang mặc định. Người dùng duyệt ngày 2026-10-08: `page=0`, `size=20`, tối đa `100`.
- [x] PRD-005 `[Owner: Integration]` Chuyển spec sang `Approved`, ghi quyết định đã chốt và cập nhật handoff. Người dùng đã duyệt PRD-004; PRD-003 vẫn là điều kiện trước phần frontend.
- [x] PRD-006 `[Owner: Codex] [AI thực hiện]` Ghi baseline `scripts/verify.sh safe`, xác nhận datasource hiện tại và thiết kế fail-fast bảo vệ production. Phụ thuộc: PRD-005. Xem `verification.md`; test integration chưa chạy.

### Gate G0

- [x] G0-1 Spec đã Approved.
- [x] G0-2 Không còn câu hỏi làm thay đổi đáng kể thiết kế Giai đoạn 1 của Codex; provider demo thuộc Giai đoạn 6.
- [x] G0-3 Test datasource được ép sang `jdbc:tc` bằng test profile, Surefire và `@DynamicPropertySource`; lệnh integration bỏ biến DB môi trường và dừng khi thiếu Docker. Đây là bằng chứng cấu hình; runtime MySQL vẫn phải xác nhận tại G1.

## Giai đoạn 1 — Docker, MySQL/Mailpit test, CI và password reset E2E

- [x] PRD-101 `[Owner: Codex] [AI thực hiện]` Viết Dockerfile backend tối thiểu, non-root nếu stack hỗ trợ và không đóng gói secret. Image build thành công; container chạy bằng `10001:10001`.
- [x] PRD-102 `[Owner: Codex] [AI thực hiện]` Tạo Compose cho backend, MySQL và Mailpit với health check/volume áp dụng. Cả ba service healthy; backend/Mailpit chỉ bind loopback.
- [x] PRD-103 `[Owner: Antigravity] [AI thực hiện]` Viết Dockerfile frontend, Nginx SPA/API proxy và cấu hình `VITE_API_BASE_URL`. Image build thành công; service frontend được tích hợp Compose và healthy trên loopback `5173`.
- [x] PRD-104 `[Owner: Codex] [AI thực hiện]` Tách profile dev/test/demo và thêm kiểm tra fail-fast chống datasource production trong test. Full integration đã đạt trên MySQL Testcontainers cô lập.
- [x] PRD-105 `[Owner: Codex] [AI thực hiện]` Thêm MySQL Testcontainers và migration test V1 -> V2 trên database mới. Full suite đạt 31 test trên MySQL 8.4.11 tạm.
- [x] PRD-106 `[Owner: Codex] [AI thực hiện]` Viết backend integration test password reset với database/email test áp dụng. Test DB đạt; Compose smoke đạt request/email/confirm/reuse/login.
- [x] PRD-107 `[Owner: Antigravity] [AI thực hiện]` Hoàn thiện Playwright Chromium E2E điều khiển DOM thật: login thành công, forgot/reset qua Mailpit, xóa token khỏi URL, kiểm tra `Referer`, login bằng mật khẩu mới, từ chối mật khẩu cũ và khôi phục mật khẩu ban đầu. Codex chạy lại `bash scripts/verify.sh frontend-e2e` thành công ngày 2026-10-09.
- [x] PRD-108 `[Owner: Codex] [AI thực hiện]` Tạo GitHub Actions backend/migration/safe checks tại Git root `.github/workflows/device-ci.yml`; `safe` và `integration` đã chạy xanh trên GitHub cho commit `5837be2`.
- [x] PRD-109 `[Owner: Antigravity] [AI thực hiện]` Frontend contract/security tests, lint/build và Playwright Compose E2E đã được tích hợp vào `scripts/verify.sh` và workflow Device CI; `compose-smoke` đã chạy xanh trên GitHub.
- [x] PRD-110 `[Owner: User] [AI hướng dẫn]` Chạy Compose, mở Mailpit và kiểm tra password reset trên trình duyệt. Người dùng xác nhận đạt ngày 2026-10-09: bốn container healthy, nhận email, mở link, token biến mất khỏi URL, reset/login bằng mật khẩu mới thành công và link cũ bị từ chối.
- [x] PRD-111 `[Owner: Integration]` Ghi kết quả Giai đoạn 1 vào verification và cập nhật task password-reset liên quan.

### Gate G1

- [x] G1-1 GitHub hosted runner checkout sạch, tạo volume MySQL mới và chạy đủ bốn service healthy bằng workflow đã ghi.
- [x] G1-2 Migration và integration test chạy trên MySQL cô lập: 31 test đạt, Flyway V1/V2 đạt.
- [x] G1-3 Playwright Chromium password reset E2E đạt login, Mailpit, reset, URL/referrer và mật khẩu cũ/mới.
- [x] G1-4 GitHub `safe`, `integration` và `compose-smoke` đều đạt trên commit `5837be2`.

## Giai đoạn 2 — Phân trang, dữ liệu lớn và import/export

- [x] PRD-201 `[Owner: Codex] [AI thực hiện]` Bổ sung API contract phân trang/filter/sort cho users, assignments, repairs và extension requests. Contract đã được duyệt ngày 2026-10-09.
- [x] PRD-202 `[Owner: User] [AI hướng dẫn]` Người dùng duyệt breaking response cho tám endpoint và xác nhận chưa tạo màn hình frontend mới ngày 2026-10-09. Phụ thuộc: PRD-201.
- [x] PRD-203 `[Owner: Codex] [AI thực hiện]` Implement `PageResult`, validation/allow-list, deterministic sort và filter ở controller-service-repository; devices dùng chung validation `page/size`.
- [x] PRD-204 `[Owner: Codex] [AI thực hiện]` Integration test MySQL cho page boundary, tám endpoint, filter, sort, giới hạn size và input lỗi; full suite đạt 34/34 test.
- [x] PRD-205 `[Owner: Antigravity] [AI thực hiện]` Đóng N/A: audit xác nhận frontend hiện chỉ gọi devices và không tiêu thụ tám endpoint breaking; phạm vi đã duyệt không tạo màn hình mới nên không sửa `frontend/`.
- [x] PRD-206 `[Owner: Codex] [AI thực hiện]` Export CSV/XLSX stream trực tiếp, đọc MySQL theo keyset batch; import CSV có giới hạn 10 MiB và flush/clear theo batch. Unit test và MySQL integration test đạt.
- [x] PRD-207 `[Owner: Codex] [AI thực hiện]` Generator CSV tổng hợp chỉ ghi filesystem, giới hạn row và từ chối ghi đè mặc định; không đọc `.env`, không có kết nối database/mạng.
- [x] PRD-208 `[Owner: Codex] [AI thực hiện]` Đã thu baseline import/query/export, peak heap/RSS và `EXPLAIN ANALYZE` trên 1k/10k/100k với máy/JVM/MySQL được ghi; report lưu tại `docs/benchmarks/device-scale-baseline-2026-10-09.md`.
- [x] PRD-209 `[Owner: Codex] [AI thực hiện]` Đã đo trước/sau 100k và thêm Flyway V3 `devices(name)`. Median trang đầu giảm 447,520 xuống 50,178 ms; keyword control không regression; deep offset không được tuyên bố đã giải quyết.
- [x] PRD-210 `[Owner: User] [AI hướng dẫn]` Người dùng chạy lại benchmark 1k/10k/100k trên máy đã ghi và xác nhận `BUILD SUCCESS`, exit 0 trong 8 phút 39 giây; report local ở `target/benchmarks/prd-210-user-confirmation.md`, không OOM với heap 512 MiB.
- [x] PRD-211 `[Owner: Integration]` Local `safe` và integration 42/42 đạt trên MySQL 8.4.11/Flyway V1-V3; PR #5 đã có đủ sáu check push/PR xanh và merge vào `main` tại `681ab42`.

### Gate G2

- [x] G2-1 Collection lớn trong phạm vi PRD-201 đã phân trang; devices giữ contract hiện tại nhưng có validation chung.
- [x] G2-2 Import/export không bắt buộc nạp toàn bộ dữ liệu lớn vào bộ nhớ; implementation và test xác nhận DB/persistence context/row window đều được giới hạn theo batch.
- [x] G2-3 Index có bằng chứng query plan và đo trước/sau; trang đầu dùng `idx_devices_name`, còn deep offset và keyword vẫn table scan theo giới hạn đã ghi.
- [x] G2-4 Frontend không tiêu thụ tám endpoint breaking và tiếp tục dùng contract devices không đổi.

## Giai đoạn 3 — Request đồng thời và tính đúng đắn dữ liệu

- [x] PRD-301 `[Owner: Codex] [AI thực hiện]` MySQL concurrent integration test gửi hai HTTP request cùng assign một device; đúng một request thành công, chỉ một active assignment được commit.
- [x] PRD-302 `[Owner: Codex] [AI thực hiện]` Test đồng thời cho return, extension approve/reject và repair complete/unrepairable đều đạt trên MySQL thật.
- [x] PRD-303 `[Owner: Codex] [AI thực hiện]` Luồng assign dùng pessimistic write lock đã có ở repository; các luồng còn lại giữ lock hiện hữu. Không thêm constraint/migration vì test dữ liệu không phát hiện invariant bị phá vỡ.
- [x] PRD-304 `[Owner: Codex] [AI thực hiện]` Conflict contract chốt HTTP `409`: codes `1022`, `1029`, `1035`, `1046`; optimistic conflict chung tiếp tục code `1053`. Integration test kiểm tra HTTP và code.
- [x] PRD-305 `[Owner: Antigravity] [AI thực hiện]` Đóng N/A: frontend hiện không có màn hình hoặc API call cho assignment, return, extension và repair; các form hiện có đã disable submit trong lúc request đang chạy nên không cần sửa `frontend/`.
- [x] PRD-306 `[Owner: User] [AI hướng dẫn]` Người dùng chạy `bash scripts/verify.sh integration` ngày 2026-10-10; 46/46 test đạt trong 1 phút 53 giây. Bộ test gồm bốn race scenario gửi hai request đồng thời và assertions đọc lại database.
- [x] PRD-307 `[Owner: Integration]` Focused concurrency suite và full integration đều đạt; assertions đọc lại database xác nhận assignment/state/deadline/final repair state nhất quán.

### Gate G3

- [x] G3-1 Chỉ kết quả hợp lệ được commit trong bốn scenario backend đã duyệt.
- [x] G3-2 Không có active assignment/transition mâu thuẫn do race trong MySQL integration test.
- [x] G3-3 Đóng N/A cho bốn transition concurrency vì frontend chưa cung cấp các thao tác này; các form hiện có đã có double-submit guard.

## Giai đoạn 4 — Email nền và rate limit

- [x] PRD-401 `[Owner: Codex] [AI thực hiện]` ADR-002 mô tả transactional outbox, claim/lease, retry, idempotency, giới hạn exactly-once của SMTP và mã hóa reset token; đã Accepted qua PRD-402.
- [x] PRD-402 `[Owner: User] [AI hướng dẫn]` Người dùng duyệt ADR-002 ngày 2026-10-10, gồm batch 20, lease 60 giây, retry, retention 30/90 ngày, SMTP delivery semantics và quản lý encryption key.
- [x] PRD-403 `[Owner: Codex] [AI thực hiện]` Flyway V4 tạo `email_jobs`; entity/repository và claim service dùng batch, lease, `FOR UPDATE SKIP LOCKED`, status/version guard. MySQL integration test xác nhận hai worker không claim trùng và lượt sau lấy job bị skip.
- [x] PRD-404 `[Owner: Codex] [AI thực hiện]` Worker claim batch rồi gửi song song ngoài transaction; retry/backoff có jitter và giới hạn, `SENT`/`DEAD`/`UNKNOWN`, stable Message-ID, redacted error, cleanup 30/90 ngày và feature flag đã có test MySQL failure/crash/chống gửi lại.
- [x] PRD-405 `[Owner: Codex] [AI thực hiện]` Password reset ghi token hash và job trong cùng transaction; raw token chỉ tồn tại dưới AES-256-GCM ciphertext trong outbox, worker giải mã trong bộ nhớ, giữ response HTTP 202 trung lập và vô hiệu hóa token khi job `DEAD`/`EXPIRED`.
- [x] PRD-406 `[Owner: Codex] [AI thực hiện]` Reminder, overdue, daily report và hai extension notification đã enqueue snapshot/idempotency key trong transaction; Flyway V6 liên kết assignment job, worker chỉ cập nhật marker sau `SENT` và bỏ qua reminder/overdue đã lỗi thời.
- [x] PRD-407 `[Owner: Codex] [AI thực hiện]` Flyway V7 tạo shared fixed-window bucket trong MySQL; login/reset kiểm tra IP và normalized identity, chỉ lưu SHA-256 key, atomic upsert an toàn đồng thời, trả 429/code 1057 + `Retry-After` và cleanup bucket hết hạn.
- [x] PRD-408 `[Owner: Antigravity] [AI thực hiện]` Đóng N/A: frontend hiện có đã chặn double-submit và hiển thị `message` từ mọi Axios error, bao gồm 429 mới; response HTTP 202 trung lập không đổi và không cần sửa `frontend/`.
- [x] PRD-409 `[Owner: User] [AI hướng dẫn]` Người dùng tắt/bật Mailpit ngày 2026-10-11: job được giữ ở `RETRY_WAIT`, sau recovery chuyển `SENT` ở lần thử thứ 3; database chỉ có một job và người dùng xác nhận Mailpit chỉ có một email.
- [x] PRD-410 `[Owner: Integration]` PR-207/208/209 đã đủ bằng chứng. Benchmark timing phát hiện baseline không đạt, sau timing guard đạt với median lệch 5,210 ms; hai verification, report và handoff đã được cập nhật, integration 83/83 và safe đạt.

### Gate G4

- [x] G4-1 SMTP lỗi không làm mất job hoặc giữ lock nghiệp vụ trong lúc gọi mạng; Compose drill xác nhận request tạo job, worker retry ngoài transaction nghiệp vụ và job tồn tại qua lúc Mailpit dừng.
- [x] G4-2 Job retry và hoàn tất đúng một lần theo contract; một job chuyển `RETRY_WAIT` sang `SENT` và Mailpit nhận đúng một email. SMTP thuần vẫn không được tuyên bố exactly-once tuyệt đối.
- [x] G4-3 Rate limit chạy trước account lookup, cùng policy cho email tồn tại/không tồn tại; unit/web/MySQL concurrency test đạt.

## Giai đoạn 5 — Log, health, metrics và chẩn đoán

- [x] PRD-501 `[Owner: Codex] [AI thực hiện]` Source audit xác nhận production Java không còn `System.out`; API exception, email worker và scheduler dùng log `event` + key/value theo allow-list. Test redaction và guard trong `verify.sh safe` ngăn exception message, recipient, payload, mật khẩu, JWT và reset token lọt vào log.
- [x] PRD-502 `[Owner: Codex] [AI thực hiện]` `X-Request-ID` được validate/sinh UUID, trả ở mọi response, thêm vào error body/log và lưu qua Flyway V8 trong email job để worker log cùng `request_id`/`job_id`. Full integration 87/87 xác nhận migration/index và password-reset request → job → worker correlation.
- [x] PRD-503 `[Owner: Codex] [AI thực hiện]` Thêm Actuator với health/liveness/readiness công khai nhưng không lộ detail; chỉ expose health, management endpoint khác cần xác thực. Compose dùng readiness thay OpenAPI; integration và runtime Compose đều đạt.
- [x] PRD-504 `[Owner: Codex] [AI thực hiện]` Actuator/Micrometer cung cấp request, JDBC/Hikari và JVM metrics; scheduler timer cùng email queue/batch/delivery/recovery/cleanup metrics dùng tag hữu hạn, có Prometheus registry nhưng chưa expose scrape endpoint trước PRD-505.
- [x] PRD-505 `[Owner: Codex] [AI thực hiện]` Compose profile monitoring chạy Prometheus/Grafana local; scrape qua management port nội bộ 8081, dashboard 8 panel được provision, cổng API 8080 không phục vụ Actuator ẩn danh và runtime target đạt `up`.
- [x] PRD-506 `[Owner: Antigravity] [AI thực hiện]` Frontend ưu tiên `requestId` trong error body, fallback `X-Request-ID`, thêm “Mã tham chiếu” vào error message và ghi nhận response ID; static contract test, lint và build đạt. Codex chỉ review/tích hợp tài liệu, không sửa `frontend/`.
- [x] PRD-507 `[Owner: Codex] [AI thực hiện]` Runbook chẩn đoán API chậm, DB connection exhaustion và scheduler/email job kẹt đã có PromQL, log correlation, truy vấn metadata không đọc secret và guard chống thao tác phá dữ liệu; các query được chạy thử trên local stack.
- [x] PRD-508 `[Owner: User] [AI hướng dẫn]` Người dùng tạo lỗi đăng nhập test, thấy “Mã tham chiếu”, dùng cùng request ID tìm đúng log 401 và xác nhận đã lưu ảnh dashboard không chứa secret.
- [x] PRD-509 `[Owner: Integration]` Security review source/test/runtime đạt: log và metric không mang dữ liệu nhạy cảm/cardinality cao; management 8081 không publish, API 8080 chặn Actuator, nội bộ chỉ health/Prometheus được phép.

### Gate G5

- [x] G5-1 Từ lỗi UI/API tìm được log/metric liên quan; PRD-508 xác nhận UI support ID khớp hai backend log event.
- [x] G5-2 Dashboard phản ánh health/latency/error/DB/job cơ bản trên local Compose; ảnh bằng chứng do người dùng lưu ở PRD-508.
- [x] G5-3 Không lộ secret/token và management endpoint không public trái phép trong cấu hình/runtime local; môi trường demo phải kiểm tra lại sau deploy.

## Giai đoạn 6 — Deploy demo, backup/restore và rollback

- [ ] PRD-601 `[Owner: User] [AI hướng dẫn]` Đã chốt ngân sách 0đ và chuyển sang Render Free Web Service + Aiven MySQL Free + Render Static Site + Resend HTTPS API ngày 2026-10-12; Koyeb mới và SMTP trên Render Free không còn nằm trong phương án. Aiven MySQL 8.4.8 đang Running với database riêng `device_demo`, các database cũ giữ nguyên. Render Dashboard đã có nhưng chưa tạo service. Người dùng đã nhận email onboarding Resend tại đúng một địa chỉ; ứng dụng demo sẽ chỉ cho phép địa chỉ này. Còn tạo resource, nhập CA/secret, duyệt migration và deploy/kiểm tra email từ ứng dụng; không gửi secret qua chat/repository. Checklist tại `docs/runbooks/free-demo-deployment.md`.
- [x] PRD-602 `[Owner: Codex] [AI thực hiện]` Image/profile demo riêng, MySQL VERIFY_IDENTITY, CORS origin chính xác và transport Resend HTTPS có recipient allow-list/idempotency đã có; local/dev vẫn dùng SMTP. Safe 45/45, full integration 99/99. Image `3b3fbf302cc0` smoke 512 MiB/0,1 CPU đạt TLS/Resend config/Flyway/readiness/login/devices/CORS/security, readiness 425 giây và mẫu RAM 268,3 MiB; chưa deploy cloud, gọi Resend thật từ ứng dụng hoặc chạy migration Aiven.
- [x] PRD-603 `[Owner: Antigravity] [AI thực hiện]` Đã chuẩn bị Render Static Site build/publish, SPA rewrite, security headers, monorepo root, build-time `VITE_API_BASE_URL` và quy trình placeholder → frontend origin → backend URL. Codex review tích hợp đã sửa Blueprint theo schema Render thành `type: web` + `runtime: static`; `bash scripts/verify.sh frontend` đạt 3/3 test, Oxlint và Vite build 92 module. Service URL/secret/deploy thật vẫn thuộc PRD-601/606.
- [x] PRD-604 `[Owner: Codex] [AI thực hiện]` GitHub deploy job chỉ chạy ref `main` sau `safe`/`integration`/`compose-smoke` và biến enable, từ push hoặc manual có xác nhận; deploy hook chọn đúng SHA, Render API chờ `live`, post-deploy script kiểm tra readiness/frontend/auth/management/CORS. Rollback runbook giữ Flyway forward-only và có bước tắt auto-deploy/worker. Local guard, YAML parse, mock HTTP end-to-end và cổng `safe` đạt; chưa gọi Render thật.
- [x] PRD-605 `[Owner: Codex] [AI thực hiện]` Backup/restore helper dùng MySQL option file ngoài Git, TLS `VERIFY_IDENTITY`, logical dump + checksum, từ chối ghi đè và chỉ restore vào database rỗng `device_demo_restore_*`; runbook cấm restore trực tiếp `device_demo`. Shell/guard test đạt; restore thật vẫn thuộc PRD-607.
- [ ] PRD-606 `[Owner: User] [AI hướng dẫn]` Nhập secret vào provider, duyệt migration và deploy demo.
- [ ] PRD-607 `[Owner: User] [AI hướng dẫn]` Thực hành backup và restore vào database cô lập; kiểm tra bản ghi và login.
- [ ] PRD-608 `[Owner: User] [AI hướng dẫn]` Kiểm tra demo bằng trình duyệt và email test/demo.
- [ ] PRD-609 `[Owner: Integration]` Ghi kết quả deploy/restore/rollback và giới hạn còn lại.

### Gate G6

- [ ] G6-1 Demo chạy qua quy trình kiểm tra tự động.
- [ ] G6-2 Backup đã được restore thành công, không chỉ được tạo.
- [ ] G6-3 Rollback code/config có kịch bản và bằng chứng áp dụng.

## Giai đoạn 7 — Portfolio và hoàn tất

- [ ] PRD-701 `[Owner: Codex] [AI thực hiện]` Cập nhật kiến trúc, README, runbook, ADR và số liệu thật.
- [ ] PRD-702 `[Owner: Antigravity] [AI thực hiện]` Cung cấp ảnh/giao diện demo đã kiểm tra; không sửa tài liệu chung trực tiếp khi làm song song.
- [ ] PRD-703 `[Owner: User] [AI hướng dẫn]` Quay demo và duyệt claim CV.
- [ ] PRD-704 `[Owner: Codex] [AI hướng dẫn]` Luyện giải thích với người dùng về DB lớn, concurrency, email failure và restore.
- [ ] PRD-705 `[Owner: Integration]` Review diff cuối, chạy toàn bộ cổng áp dụng và cập nhật handoff.
- [ ] PRD-706 `[Owner: User]` Duyệt feature `Done` và cho phép commit/merge/phát hành theo quy trình.

## Blocker hiện tại

- PRD-003, PRD-103, PRD-107 và phần tích hợp CI PRD-109 đã được kiểm tra; Playwright Chromium E2E local đã đạt.
- Docker Engine/Compose và quyền user đã hoạt động; blocker Docker được gỡ. Full stack đạt bốn service healthy trong lần kiểm tra PRD-110.
- Password reset đã có MySQL Testcontainers, full Compose, Playwright E2E, kiểm tra trình duyệt thủ công và GitHub CI xanh; PR-207/208/209 đều đã đóng.
- Giai đoạn 2 đã đóng: PRD-210 được người dùng xác nhận; PR #5 đã merge vào `main` tại `681ab42` với sáu check xanh.
- PRD-305 đã đóng N/A sau review frontend; người dùng đã xác nhận PRD-306 và Giai đoạn 3 hoàn tất.
- Giai đoạn 4 và 5 cùng chuẩn bị backend/frontend/deploy/backup PRD-602 đến PRD-605 đã hoàn tất; ngày 2026-10-12 người dùng chọn demo CV miễn phí. Chưa được tuyên bố production-ready trước kiểm tra deploy/restore/rollback thật.
- Aiven Free và database rỗng riêng `device_demo` đã sẵn sàng; email onboarding Resend đã đến đúng một địa chỉ. Chưa tạo Render Web/Static services, nhập secret, duyệt cloud migration, restore backup cô lập hoặc xác minh email do ứng dụng cloud tạo. Các phần còn lại chặn PRD-606/607/608 và Gate G6.

## Quy tắc cập nhật

- Chỉ đánh dấu `[x]` khi có implementation hoặc bằng chứng tương ứng.
- Task `[Owner: User]` không được AI tự đánh dấu hoàn thành nếu người dùng chưa xác nhận kết quả.
- Agent chỉ sửa code thuộc owner; ngoại lệ cần yêu cầu rõ từ người dùng.
- Dependency chưa đạt thì ghi blocker, không tự sửa phần của agent khác.
- Task phát sinh ngoài phạm vi phải quay lại review spec.
- Trạng thái thật chỉ nằm ở file này; bằng chứng thật chỉ nằm ở `verification.md`.
