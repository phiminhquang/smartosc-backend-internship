# AI Handoff

## Mục tiêu hiện tại

- Thực hiện Giai đoạn 2 production-readiness: phân trang, dữ liệu lớn, import/export và index có bằng chứng.
- Chốt contract trước khi thay đổi backend và không sửa frontend thuộc owner Antigravity.

## Trạng thái hiện tại

- Backend, migration, email template và frontend đã được triển khai.
- `bash scripts/verify.sh safe` và `bash scripts/verify.sh integration` được chạy lại ngày 2026-10-08 lúc 13:55-13:58 +07: safe đạt toàn bộ, integration đạt 31/31 test trên MySQL Testcontainers.
- MySQL integration, Flyway V1/V2, full Compose frontend/backend/MySQL/Mailpit và Playwright Chromium browser E2E đã đạt local.
- PR #3 đã merge PRD-206/207 vào `main` tại `9b9e4fd`; post-merge Device CI run `37910483706` đạt `safe` 35 giây, `integration` 1 phút 18 giây và `compose-smoke` 2 phút 34 giây.
- Branch hiện tại là `feature/device-scale-benchmark`, tách từ `main` sau PR #3. PRD-208 đã có full baseline 1k/10k/100k và đang chờ chạy gate/review/commit; xem mục cuối file trước khi sửa.
- Chưa sẵn sàng public production vì PRD-209 và các giai đoạn production-readiness sau đó còn mở.
- Phân công mặc định và ranh giới chỉnh sửa tuân theo `AGENTS.md`; hiện không có ngoại lệ đang hoạt động.

## Nguồn sự thật cần đọc

- Yêu cầu và API contract đang hoạt động: `specs/production-readiness/spec.md`.
- Thiết kế triển khai và rollback: `specs/production-readiness/plan.md`.
- Trạng thái công việc và blocker: `specs/production-readiness/tasks.md`.
- Bằng chứng kiểm tra duy nhất: `specs/production-readiness/verification.md`.
- Quyết định bảo mật lâu dài: `docs/decisions/001-password-reset-token-and-session-revocation.md`.
- Quy tắc project và cổng kiểm tra: `AGENTS.md` và `scripts/verify.sh`.

## Phạm vi file đã thay đổi

- Backend: controller, security/JWT validator, authentication/password-reset service, user/reset-token model, repository, DTO và test liên quan trong `src/main/` và `src/test/`.
- Database/email: `src/main/resources/db/migration/V2__password_reset.sql` và `src/main/resources/templates/email/password-reset.html`.
- Frontend: các trang forgot/reset password, `authService`, route trong `App.tsx` và chính sách referrer trong `frontend/`.
- Quy trình/tài liệu: `PROJECT.md`, các adapter AI, `specs/password-reset/`, ADR liên quan và `scripts/verify.sh`.
- Dùng Git diff/history để lấy danh sách file chính xác; handoff chỉ giữ phạm vi để tránh lặp dữ liệu.

## Blocker và rủi ro còn lại

- MySQL Testcontainers/Compose, frontend container, Mailpit và browser E2E đã đạt local và GitHub hosted runner sạch.
- Playwright Chromium E2E điều khiển DOM thật và yêu cầu login thành công; PRD-107, các tiêu chí tự động Gate G1 và kiểm tra thủ công PRD-110 đều đã đạt.
- `source-map-js` là dependency bắc cầu qua Vite/PostCSS, đã lên 1.2.2; `npm audit --audit-level=high` ngày 2026-10-09 báo 0 vulnerability.
- SMTP hiện đồng bộ trong request transaction, có thể tạo timing side-channel và giữ database lock khi gọi mạng.
- Cooldown theo user chưa thay thế rate limit theo IP/identity cho endpoint public.

## Việc tiếp theo

1. Chạy `bash scripts/verify.sh safe` và `bash scripts/verify.sh integration`, review diff PRD-208 rồi commit/push/CI/PR riêng.
2. Sau khi PRD-208 merge, thực hiện PRD-209 trên nhánh mới bằng cách review plan đã lưu và đo trước/sau cùng dataset/môi trường.
3. Không thêm index hoặc deferred join nếu phép đo sau thay đổi không chứng minh lợi ích đủ rõ.
4. Trước public production, hoàn tất PR-207/208/209 trong password-reset `tasks.md`.
5. Không tuyên bố production-ready chỉ từ Gate G1/G2; các giai đoạn và follow-up bảo mật còn lại vẫn áp dụng.

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
- Việc còn lại trên branch này: review diff, commit/push/CI/PR PRD-208. Sau khi merge mới tạo nhánh PRD-209.
