# Kiểm chứng: Hoàn thiện hệ thống để vận hành, chịu tải và phục hồi sự cố

## Trạng thái

- Feature đã `Approved`; các tiêu chí tự động của Gate G1 đã đạt trên local và GitHub, và người dùng đã xác nhận kiểm tra thủ công PRD-110 đạt ngày 2026-10-09.
- File này chỉ ghi kết quả đã thực sự chạy. Không suy ra thành công từ kế hoạch hoặc lịch sử chat.
- Bằng chứng password reset hiện có tiếp tục nằm trong `specs/password-reset/verification.md`; không sao chép hoặc nâng trạng thái tại đây.

## Môi trường baseline

- Ngày giờ: 2026-10-08, Asia/Ho_Chi_Minh.
- Commit/working tree: Nhánh `main`, ahead `origin/main` 1 commit; working tree có thay đổi chưa commit thuộc password reset, production-readiness, cấu hình/backend test, Docker/Compose, CI và tài liệu. Không ghi đè hoặc quy kết các thay đổi có sẵn cho lần tiếp quản này.
- OS/runtime: Linux, Java 17; frontend Vite 8.3.1 theo output lần chạy.
- Database/dịch vụ ngoài: MySQL 8.4.11 Testcontainers/Compose và Mailpit 1.31.4 local đã được kiểm chứng; không dùng Aiven/production.

## Cổng chất lượng

| Kiểm tra | Lệnh/kịch bản | Kết quả | Bằng chứng/Ghi chú |
|---|---|---|---|
| Tài liệu/diff sau khi tạo spec | `git diff --check` | Thành công | Không có lỗi whitespace trong tracked diff |
| Cổng an toàn hiện tại | `bash scripts/verify.sh safe` | Thành công | 2026-10-12 PRD-604/605: compile, logging guard, 45 backend test, generator/ba benchmark guard, deploy/backup guard, 3 frontend test, lint/build 92 module |
| Backend compile | Chạy bởi `bash scripts/verify.sh safe` | Thành công | Maven `BUILD SUCCESS` |
| MySQL migration/integration | `bash scripts/verify.sh integration` | Thành công | 2026-10-12 PRD-602 Resend: 99 test; MySQL 8.4.11 tạm; Flyway V1-V8 |
| Backend test mục tiêu | Chạy bởi `bash scripts/verify.sh safe` | 45 thành công | 0 failure, 0 error, 0 skipped |
| Frontend contract/security tests | `node --test` qua `scripts/verify.sh` | Thành công | 3 file static/contract; không thay thế browser test |
| Frontend lint | Chạy bởi `bash scripts/verify.sh safe` | Thành công | Oxlint không báo lỗi |
| Frontend build | Chạy bởi `bash scripts/verify.sh safe` | Thành công | TypeScript/Vite, 92 module transformed |
| Compose local smoke | Build/start + frontend/API/Mailpit | Thành công | 4 service healthy; SPA route/header và API smoke qua Nginx proxy đạt |
| Browser E2E | `bash scripts/verify.sh frontend-e2e` | Thành công | Playwright Chromium, 1 test pass trong 1.1 phút; điều khiển DOM thật qua full Compose |
| Browser manual PRD-110 | Người dùng chạy full Compose và kiểm tra qua trình duyệt/Mailpit | Thành công | 4 container healthy; email/link/reset/login đạt; token biến mất khỏi URL; link cũ bị từ chối |
| GitHub Device CI | `safe`, `integration`, `compose-smoke` | Thành công | PR #6 có đủ sáu check push/PR xanh; post-merge run `38040530264` trên `main` cũng xanh |
| Data-scale benchmark | Dataset/máy/lệnh phải được ghi | Thành công | Baseline 1k/10k/100k, raw latency, heap/RSS và plan lưu tại `docs/benchmarks/device-scale-baseline-2026-10-09.md` |
| Device query index benchmark | `bash scripts/benchmark-device-query-index.sh --confirm-isolated` | Thành công | 100k trước/sau, 2 chu kỳ x 3 lần đo; report tại `docs/benchmarks/device-query-index-comparison-2026-10-10.md` |
| Concurrent requests | MySQL integration test thật | Thành công | 4/4 race test và full suite 46/46 đạt; mỗi scenario đúng một HTTP 200 và một HTTP 409 |
| SMTP failure/recovery | Tắt/bật Mailpit, gửi đúng một request và quan sát job/MySQL/Mailpit | Thành công | 2026-10-11: `RETRY_WAIT` → `SENT`, một job, một email, không thấy raw token/payload trong log đã kiểm tra |
| Backup/restore | Restore vào database cô lập và kiểm tra | Chưa chạy | PRD-605 helper/guard đã đạt local; máy hiện không có MySQL client và PRD-607 vẫn cần user chạy restore thật trên database cô lập |

## Lần triển khai và kiểm tra Giai đoạn 0/1 ngày 2026-10-08

- `bash scripts/verify.sh safe`: exit 0; backend compile thành công, 15 test mục tiêu thành công (0 failure/error/skipped), frontend lint/build thành công, 92 module Vite.
- `bash ./mvnw -DskipTests test-compile`: exit 0; integration test mới compile được, nhưng điều này **không** chứng minh test pass.
- Parse YAML bằng PyYAML cho `compose.yaml`, `application.yaml`, `application-dev.yaml`, `application-demo.yaml`, `application-test.yaml`: 5 file hợp lệ về cú pháp YAML. Chưa xác thực Compose bằng Docker.
- `bash scripts/verify.sh integration`: exit 1 có chủ ý, thông báo Docker daemon phải được cài và truy cập được bởi user hiện tại; không khởi chạy Maven hoặc kết nối database ngoài.
- `command -v docker`: không tìm thấy. `sudo -n true` trong môi trường được nâng quyền trả `sudo: a password is required`; chưa cài Docker. Không dùng database Aiven/production.
- `git diff --check`: exit 0. CI workflow đã tạo nhưng chưa có run trên GitHub; Dockerfile/Compose chưa build hoặc start.
- Safety design: `application-test.yaml` dùng `jdbc:tc:mysql:8.4:///device_test`; Surefire ép `test`; `DeviceApplicationTests` dùng `@ActiveProfiles("test")` và `@DynamicPropertySource` ép datasource URL/driver; lệnh integration bỏ các biến DB môi trường trước khi chạy. Đây là kiểm chứng mã/cấu hình, không thay thế chạy runtime.
- Sau khi người dùng cài Docker: `docker compose version` báo 2.40.3; `docker info` bằng user `quang` thất bại `permission denied` tại `/var/run/docker.sock`. `docker compose config --quiet` với ba placeholder local-only qua môi trường: exit 0; chưa khởi động container. `getent group docker` cho thấy user chưa thuộc nhóm này. Không có file `.env` local.
- Sau khi người dùng cấp quyền Docker: Engine 29.1.3 và Compose 2.40.3 hoạt động. `.env` local mode 600 bị Git ignore; không ghi giá trị vào tài liệu/Git.
- `bash scripts/verify.sh integration`: exit 0; 31 test, 0 failure/error/skipped; MySQL 8.4.11 tạm, Flyway V1/V2 và password reset integration đạt.
- `docker compose up --build -d`: lần đầu phát hiện lỗi JDBC `Public Key Retrieval is not allowed`; sửa URL chỉ trong Compose local rồi build lại. Backend/MySQL/Mailpit sau đó đều healthy.
- Backend image chạy user `10001:10001`; backend và Mailpit bind `127.0.0.1`. OpenAPI/Mailpit HTTP 200; login HTTP 200; reset request HTTP 202; Mailpit nhận email; confirm HTTP 200; token dùng lại HTTP 400; email không tồn tại HTTP 202 và không tạo email.
- `bash scripts/verify.sh safe` sau sửa: exit 0; backend compile, 15 test mục tiêu, frontend lint/build (92 module) đạt.

## Lần tiếp quản và kiểm tra lại 2026-10-08 13:55-13:58 +07

- `docker version`: client/server 29.1.3; `docker compose version`: 2.40.3.
- `docker compose config --quiet`: exit 0. `docker compose ps`: backend, MySQL và Mailpit đều `healthy`.
- `docker inspect`: backend chạy bằng user `10001:10001`; health của cả ba container là `healthy`.
- OpenAPI backend và Mailpit UI trên loopback đều trả HTTP 200. Không chạy lại reset token smoke nên không thay đổi mật khẩu hoặc dữ liệu nghiệp vụ local.
- Truy vấn chỉ đọc `flyway_schema_history` trong MySQL Compose xác nhận migration version 1 và 2 đều có `success=1`.
- `.env` local tiếp tục bị Git ignore và có mode `600`; review `application-dev.yaml` xác nhận chỉ chứa override SMTP local và địa chỉ gửi local, không chứa credential.
- `bash scripts/verify.sh integration`: exit 0; 31 test, 0 failure/error/skipped; Testcontainers tạo MySQL 8.4.11 tạm và Flyway áp dụng V1/V2 từ schema trống.
- `bash scripts/verify.sh safe`: exit 0; diff/docs check, backend compile, 15 test mục tiêu, frontend lint và build Vite 92 module đều đạt. Frontend source không bị sửa.
- Review bảo mật/migration password reset không phát hiện secret production hoặc raw reset token trong log/config; rủi ro SMTP đồng bộ, rate limit và timing vẫn là blocker PR-207/208/209, không được coi là đã xử lý.

## Lần tích hợp frontend 2026-10-08 16:19 +07

- Diff frontend do Antigravity bàn giao gồm Dockerfile multi-stage, Nginx SPA/API proxy, `VITE_API_BASE_URL`, bốn file Node test và script npm. Codex không sửa source/frontend của Antigravity.
- `npm test`, `npm run lint`, `npm run build`: exit 0; 4 file test đạt, Oxlint đạt, Vite build 92 module.
- `docker compose up --build -d --wait`: exit 0; frontend image build thành công và backend, frontend, MySQL, Mailpit đều `healthy`. Frontend chỉ bind `127.0.0.1:5173`.
- `bash scripts/verify.sh frontend-e2e`: exit 0; live smoke không skip và gọi API qua Nginx proxy. Frontend `/` và `/reset-password` trả HTTP 200; response có `Referrer-Policy: no-referrer`, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`.
- `npm audit --omit=dev --audit-level=high`: không thấy vulnerability trong production dependency. Full audit còn 1 high ở dev dependency `source-map-js`; chưa tự sửa dependency thuộc owner frontend.
- Workflow Device CI đã có job `compose-smoke`, sinh credential tạm trong runner, build đủ stack và chạy live smoke; chưa có GitHub run nên không tuyên bố CI xanh.
- `bash scripts/verify.sh safe` sau tích hợp: exit 0; docs/diff, backend compile, 15 test backend mục tiêu, 3 frontend contract/security test, lint và build 92 module đều đạt.
- `bash scripts/verify.sh integration` sau tích hợp: exit 0; 31 test, 0 failure/error/skipped; MySQL 8.4.11 Testcontainers và Flyway V1/V2 đạt.
- Giới hạn: file `e2e-compose-smoke.test.mjs` là API smoke bằng Node `fetch`, cho phép login 401 và không dùng browser/DOM. Vì vậy PRD-107, PRD-110, G1-1, G1-3 và G1-4 vẫn mở.

## Cấu hình phát triển backend bằng IntelliJ 2026-10-09

- Bổ sung `compose.ide.yaml` chỉ publish MySQL ở loopback `3307` và Mailpit SMTP ở loopback `1025`; full Compose hiện tại không đổi.
- Bổ sung profile `ide`, script `scripts/dev-ide.sh` và shared run configuration `Device Backend (IDE)`. Profile đọc ba secret local từ `.env`; không có credential trong file được theo dõi.
- Cấu hình `Unnamed` cá nhân trước đó có key ` DB_USERNAME` thừa khoảng trắng và thiếu `DB_URL`; file `.idea/workspace.xml` bị Git ignore và không được sửa.
- `bash scripts/dev-ide.sh up`: exit 0; container backend/frontend được dừng để tránh tranh chấp cổng, MySQL và Mailpit được tạo lại rồi đạt trạng thái healthy.
- `SPRING_PROFILES_ACTIVE=dev,ide bash ./mvnw spring-boot:run -Dspring-boot.run.arguments=--spring.main.banner-mode=off`: backend khởi động thành công trên cổng 8080; log xác nhận hai profile `dev`, `ide`, kết nối `jdbc:mysql://127.0.0.1:3307/device_db`, Flyway validate 2 migration và schema đang ở version 2.
- `curl --fail --silent --show-error --output /tmp/device-api-docs.json --write-out '%{http_code}' http://127.0.0.1:8080/v3/api-docs`: HTTP 200. Sau kiểm tra, tiến trình Maven được dừng graceful để cổng 8080 sẵn sàng cho IntelliJ; MySQL/Mailpit tiếp tục chạy.
- `bash scripts/verify.sh safe` sau thay đổi hybrid: exit 0; kiểm tra tài liệu/diff, backend compile, 15 test backend, 3 frontend test, lint và build 92 module đều đạt.

## Tích hợp Playwright browser E2E ngày 2026-10-09

- Review `frontend/e2e/auth-password-reset.spec.ts` và `frontend/playwright.config.ts`: test dùng Chromium thật, thao tác form/DOM để login, gửi forgot password, lấy link qua Mailpit, reset, login bằng mật khẩu mới, từ chối mật khẩu cũ và khôi phục mật khẩu ban đầu để chạy lặp lại.
- Test xác nhận query token biến mất khỏi `page.url()` sau khi trang reset nạp, token vẫn có trong form và raw token không xuất hiện trong header `Referer` của các request tiếp theo.
- Full Compose cuối cùng đạt bốn service `healthy`. Hai lần start trung gian bị chặn do backend IntelliJ giữ cổng `8080` và Vite giữ `5173`; sau khi dừng đúng hai tiến trình phát triển, container được tạo lại mà không xóa MySQL volume.
- `bash scripts/verify.sh frontend-e2e`: exit 0; `1 passed (1.1m)` trên Chromium. Login phải thành công; test không chấp nhận HTTP 401 làm kết quả pass.
- `bash scripts/verify.sh safe` sau tích hợp: exit 0; kiểm tra tài liệu/diff, backend compile, 15 backend test, 3 frontend contract/security test, lint và build 92 module đều đạt.
- `bash scripts/verify.sh integration` sau tích hợp: exit 0; 31 test, 0 failure/error/skipped; MySQL 8.4.11 Testcontainers và Flyway V1/V2 đạt.
- `npm ls source-map-js`: dependency bắc cầu `vite@8.3.1 -> postcss@8.5.28 -> source-map-js@1.2.2`. `npm audit --audit-level=high`: exit 0, `found 0 vulnerabilities`.
- Workflow `compose-smoke` đã được bổ sung bước `npx playwright install --with-deps chromium` trước khi dựng Compose; bước cài Chromium và dựng đủ stack đã chạy thành công trên GitHub.
- GitHub push/PR run đầu tiên: `safe` và `integration` đều pass; `compose-smoke` fail ở bước self-healing do test chưa đợi React Router chuyển tới `/forgot-password` trước khi dùng selector chung. Đây là race condition của test, không phải failure nghiệp vụ reset chính.
- Đã thêm assertion chờ đúng URL trước lần reset khôi phục và mask hai credential tạm trước khi ghi `GITHUB_ENV`. `bash scripts/verify.sh frontend-e2e` sau sửa tiếp tục exit 0, `1 passed (1.1m)`; GitHub rerun phải được kiểm tra trước khi đóng CI gate.
- Rerun GitHub thứ hai vẫn fail ở riêng lần reset khôi phục tùy chọn, dù full luồng bắt buộc đã qua tới bước từ chối mật khẩu cũ. Mô phỏng CI local bằng Compose project/volume riêng, mật khẩu ngẫu nhiên 64 ký tự và cleanup volume tạm đạt `1 passed (1.1m)`.
- Test tiếp tục khôi phục mật khẩu cho local DB bền vững, nhưng bỏ reset lần hai khi `CI=true` vì runner dùng database/volume tạm và bị hủy sau job. Luồng CI bắt buộc vẫn giữ login, Mailpit, reset, URL/referrer, mật khẩu mới và từ chối mật khẩu cũ.
- `CI=true bash scripts/verify.sh frontend-e2e` trên full Compose local sau thay đổi: exit 0; `1 passed (6.4s)`. GitHub run mới vẫn là bằng chứng cuối cho `compose-smoke`.
- GitHub commit `5837be2`: cả push run và PR run đều đạt. `safe` mất 22/34 giây, `integration` 41/50 giây và `compose-smoke` 2 phút 17 giây/1 phút 59 giây. Bước Compose cài Chromium, tạo credential tạm đã mask, dựng bốn service và chạy Playwright thành công.

## Kiểm tra thủ công PRD-110 ngày 2026-10-09

- Người dùng xác nhận cả bốn container đều healthy và Mailpit nhận được email reset.
- Link trong email mở thành công; reset token biến mất khỏi address bar sau khi trang nạp.
- Đổi mật khẩu thành công và đăng nhập bằng mật khẩu mới thành công.
- Link reset đã dùng bị từ chối khi mở lại.
- Không ghi mật khẩu hoặc reset token vào tài liệu. Đây là bằng chứng do người dùng thực hiện và xác nhận cho task `[Owner: User]` PRD-110.

## Khởi động Giai đoạn 2 và PRD-201 ngày 2026-10-09

- Sau khi PR #1 merge, local `main` được fast-forward tới merge commit `f5eef03`; post-merge Device CI đạt `safe` 1m01s, `integration` 1m03s và `compose-smoke` 2m38s.
- GitHub Actions báo runtime Node 20/setup-java v4 deprecated. Release notes chính thức xác nhận `actions/checkout@v5`, `actions/setup-java@v5` và `actions/setup-node@v5` chạy Node 24 và yêu cầu runner `v2.327.1`; workflow đã được cập nhật sang v5. Branch CI run `37901442587` đạt `safe` 30s, `integration` 54s và `compose-smoke` 2m07s, không còn cảnh báo Node 20/setup-java v4.
- Cùng CI run trên báo `ubuntu-latest` sẽ chuyển sang Ubuntu 26 từ 2026-10-19. Ba job được ghim `ubuntu-24.04`; branch CI run `37901920546` xác nhận `safe` 30s, `integration` 51s và `compose-smoke` 2m22s đều đạt, không còn cảnh báo đổi runner.
- Codebase graph generation tại commit `f5eef03` xác nhận bốn controller danh sách gọi các service/repository đang trả `List`; kiểm tra coverage không ghi nhận khoảng trống ở các controller/service/repository liên quan. Frontend hiện chỉ có `deviceService`, không có consumer cho bốn nhóm API mới.
- Video YouTube người dùng cung cấp được lấy metadata qua `yt-dlp`; hai backend caption trả rỗng nên dùng Groq Whisper fallback và lưu transcript tạm ngoài repository. Nội dung tham khảo nhấn mạnh deep `OFFSET`, deterministic sort, `EXPLAIN`, index theo bằng chứng và deferred join bằng page ID.
- Contract proposed cho PRD-201 đã ghi trong `spec.md`: page zero-based, mặc định 20/tối đa 100, response page ổn định, filter/sort allow-list và tám endpoint bị breaking response. Tại mốc này PRD-202 còn mở và chưa sửa backend hoặc frontend.
- `bash scripts/verify.sh safe` sau cập nhật workflow/contract: exit 0; documentation/diff check, backend compile, 15 backend test mục tiêu, 3 frontend contract/security test, lint và build 92 module đều đạt.

## PRD-202 đến PRD-205 — API phân trang ngày 2026-10-09

- Người dùng duyệt breaking response cho tám endpoint và phạm vi không tạo màn hình frontend mới sau khi xác nhận devices đã có phân trang, phần còn thiếu là users/assignments/repairs/extension requests.
- Thêm `PageResult<T>` ổn định, `PaginationSupport` giới hạn `page >= 0`, `1 <= size <= 100`, sort allow-list và `id` tie-breaker. Input phân trang/filter/sort/enum/UUID sai trả HTTP `400`, code `1055`.
- Users dùng query trang entity/ID trước rồi fetch roles theo tập ID, không page trên collection fetch join. Assignments, repairs và extensions dùng specification cùng entity graph chỉ chứa quan hệ to-one.
- Tám endpoint collection đã trả page shape; `/api/devices` giữ response hiện tại nhưng dùng chung kiểm tra page/size, keyword tối đa 100 và sort xác định `name,id`.
- `PaginationApiIntegrationTest` chạy qua MockMvc trên MySQL Testcontainers 8.4.11: 3 test đạt, bao phủ trang đầu/cuối, keyword/role/status/user/device filter, sort, tám endpoint, max size và input sai.
- `bash scripts/verify.sh integration`: exit 0, Flyway V1/V2 áp dụng trên database tạm; 34 test đạt, 0 failure/error/skip. Script integration dùng Byte Buddy javaagent từ Maven cache khi có để Mockito không phụ thuộc cơ chế self-attach của máy chạy.
- `bash scripts/verify.sh safe`: exit 0 sau implementation; documentation/diff check, backend compile, 15 backend test mục tiêu, 3 frontend contract/security test, lint và build 92 module đều đạt.
- GitHub Device CI run `37904867768` cho commit implementation `ea1de52` đạt `safe` 32s, `integration` 1m09s và `compose-smoke` 2m00s trên runner `ubuntu-24.04`.
- Không sửa file trong `frontend/`, không sửa `.env`, không thêm migration/index trước khi có dataset và benchmark PRD-207/208/209.

## Ma trận tiêu chí chấp nhận

| ID | Tình huống | Kết quả mong đợi | Kết quả thực tế | Trạng thái |
|---|---|---|---|---|
| V-01 | Máy sạch khởi động stack | Frontend, backend, MySQL, Mailpit healthy bằng quy trình tài liệu hóa | Cả 4 service healthy trên máy local; chưa có clean-machine run | Một phần |
| V-02 | Full test/migration | Chỉ dùng MySQL cô lập, không thể chạm Aiven ngoài ý muốn | 83 test đạt trên MySQL Testcontainers, Flyway V1-V7; biến DB môi trường bị bỏ | Đạt |
| V-03 | Password reset E2E | Email/link/reset/login/token revocation đúng contract | Playwright Chromium đạt email/link/reset/login, URL/referrer và mật khẩu cũ/mới; JWT cũ có backend test nhưng chưa kiểm tra trên browser/Compose | Một phần |
| V-04 | API list với dữ liệu lớn | Trả page có giới hạn/filter/sort đúng contract | MySQL 8.4.11 đạt 100k row; so sánh PRD-209: page đầu 447,520 -> 50,178 ms, deep page 608,277 -> 624,786 ms | Đạt |
| V-05 | Request size quá giới hạn | Bị từ chối hoặc giới hạn theo contract | `size=101` trên users và devices trả HTTP 400/code 1055 trong integration test | Đạt |
| V-06 | Export dữ liệu lớn | Không bắt buộc nạp toàn bảng vào heap; file đúng | 100k: CSV median 1.669,511 ms/peak heap delta 123,444 MiB; XLSX median 6.806,224 ms/117 MiB; output đã có unit/integration test đúng định dạng | Đạt |
| V-07 | Import file lớn/lỗi dòng | Xử lý theo giới hạn và báo lỗi xác định | 100k CSV 7.100.032 byte import đủ trong 225,443 giây; peak heap delta 117,122 MiB; rollback/header/BOM/size đã có integration/unit test | Đạt |
| V-08 | Query trước/sau index | Có query plan, dataset và số đo lặp lại được | 100k, hai chu kỳ x ba lần: page đầu cải thiện 88,79% và plan dùng `idx_devices_name`; keyword +9,45%, deep page -2,71%; report đầy đủ đã lưu | Đạt |
| V-09 | Hai request assign cùng device | Chỉ một kết quả hợp lệ; không có hai assignment mở | Một HTTP 200, một HTTP 409/code 1022; database có đúng một active assignment và device ở `ASSIGNED` | Đạt |
| V-10 | Hai request return/review/repair | State transition không bị lặp hoặc mâu thuẫn | Mỗi race có một HTTP 200 và một HTTP 409; codes 1029/1035/1046; dữ liệu cuối nhất quán | Đạt |
| V-11 | SMTP tắt khi tạo email | Nghiệp vụ/job theo contract; job không mất | PRD-409: job giữ ở `RETRY_WAIT`, request vẫn hoàn tất | Đạt |
| V-12 | SMTP bật lại | Job retry và email hoàn tất đúng một lần | PRD-409: cùng job `SENT`, database một job và Mailpit một email | Đạt |
| V-13 | Gọi reset/login quá mức | Rate limit hoạt động giống nhau, không lộ tài khoản | Unit/web/MySQL concurrency đạt; PR-209 timing sau guard đạt | Đạt |
| V-14 | Lỗi API có request ID | Tìm được log liên quan, response không lộ nội bộ | PRD-508: mã hỗ trợ trên UI khớp log rejection/completion 401; redaction test đạt | Đạt |
| V-15 | Dashboard vận hành | Thấy health, latency/error, DB và email job cơ bản | PRD-505/508: dashboard 8 panel chạy; target up và người dùng xác nhận đã lưu ảnh | Đạt |
| V-16 | Management endpoint từ public | Không truy cập trái phép | PRD-509 local: host 8081 connection refused; API Actuator 401; nội bộ health/Prometheus 200, info 401 | Đạt |
| V-17 | Backup/restore | Restore vào DB cô lập, dữ liệu và login được xác nhận | Chưa chạy | Chưa chạy |
| V-18 | Deploy lỗi/rollback | Quay về artifact/config trước theo runbook | Chưa chạy | Chưa chạy |
| V-19 | Frontend với contract mới | Pagination/conflict/support ID hiển thị đúng | PRD-305/408 N/A cho màn hình chưa có; PRD-506 test và PRD-508 browser support-ID đạt | Đạt |
| V-20 | Portfolio claim | Mỗi số liệu/claim liên kết được tới code hoặc bằng chứng | Chưa chạy | Chưa chạy |

## Mẫu ghi một lần chạy

```text
Ngày giờ:
Commit hoặc working tree:
Giai đoạn/task:
Môi trường và cấu hình máy:
Dataset/dịch vụ ngoài:
Lệnh hoặc kịch bản:
Kết quả:
Bằng chứng lưu ở đâu:
Lỗi hoặc giới hạn còn lại:
```

## Quy tắc benchmark

- Ghi cấu hình CPU/RAM, Java/MySQL version và trạng thái Docker.
- Ghi số bản ghi từng bảng và cách sinh dữ liệu.
- Chạy warm-up và nhiều lần; không chọn riêng kết quả đẹp nhất.
- So sánh cùng môi trường trước/sau.
- Không suy rộng kết quả local thành số người dùng production.

## Lỗi và giới hạn hiện biết

- MySQL Testcontainers, Mailpit và Playwright Chromium đã chạy local; GitHub hosted runner đã checkout sạch, tạo volume mới và chạy đủ ba job CI thành công.
- `DeviceApplicationTests` đã chạy runtime với Testcontainers và không dùng datasource ngoài.
- Tất cả producer email hiện dùng transactional outbox; SMTP chỉ được worker gọi ngoài transaction claim/nghiệp vụ.
- Tám collection PRD-201 đã có phân trang. Export thiết bị không còn dùng `findAll()`/`byte[]`; CSV/XLSX stream theo keyset batch và đã có số đo 100k.
- Index `devices(name)` tối ưu trang đầu. Deep offset vẫn scan/sort toàn bảng và keyword leading-wildcard vẫn table scan; deferred join/full-text chưa được thêm vì ngoài bằng chứng và phạm vi hiện tại.
- Concurrent integration test đã đạt; backup/restore và deploy demo vẫn chưa có kết quả.

## PRD-206/207 — Import/export giới hạn tài nguyên và data generator ngày 2026-10-09

- PR #2 merge vào `main` tại `d34854f`; post-merge Device CI run `37906680884` đạt `safe` 37 giây, `integration` 1 phút 01 giây và `compose-smoke` 2 phút 17 giây.
- Contract mới giữ nguyên ba endpoint/payload hiện có. CSV/XLSX export ghi trực tiếp vào response stream, đọc `devices` theo keyset `id,asc` batch 500; XLSX dùng `SXSSFWorkbook` row window 100 và dọn file tạm.
- Import CSV giới hạn 10 MiB, kiểm tra extension/header, bỏ UTF-8 BOM, đọc tuần tự và flush/clear persistence context mỗi 100 row. File lỗi vẫn rollback toàn bộ transaction; file quá lớn dùng HTTP 400/code 1056.
- `DeviceFileServiceImplTest`: 5/5 test đạt với Byte Buddy javaagent; bao phủ hai batch CSV, workbook XLSX đọc lại được, BOM, batch flush/clear, header thiếu và file quá giới hạn.
- `PaginationApiIntegrationTest`: 5/5 test đạt trên MySQL 8.4.11 Testcontainers; test mới buộc export đi qua nhiều keyset batch, import ba row thật, và xác nhận lỗi ở row thứ ba rollback cả hai row đã flush trước đó.
- `bash scripts/verify.sh safe`: exit 0; backend compile, 20 backend test, generator check, 3 frontend test, lint và build 92 module đạt.
- `bash scripts/verify.sh integration` sau toàn bộ test MySQL mới: exit 0; 41 test đạt, 0 failure/error/skip; MySQL 8.4.11 Testcontainers và Flyway V1/V2 đạt.
- GitHub Device CI run `37909236650` tại commit `cf5d95a` đạt `safe` 44 giây, `integration` 1 phút 05 giây và `compose-smoke` 2 phút 20 giây trên runner `ubuntu-24.04`.
- `scripts/generate-device-csv.sh` tạo thử 7 row thành file 8 dòng gồm header và lần chạy lại không `--force` trả exit 2. Generator chỉ dùng `awk`/filesystem; hướng dẫn nằm tại `docs/runbooks/device-data-scale.md`.
- Tại thời điểm PRD-206/207, dataset lớn/heap/RSS/latency/query plan chưa chạy; khoảng trống này đã được PRD-208 bên dưới đóng. PRD-209 vẫn mở và chưa có migration/index.

## PRD-208 — Baseline dữ liệu lớn ngày 2026-10-09

- Lệnh: `bash scripts/benchmark-device-scale.sh --confirm-isolated --output target/benchmarks/device-scale-baseline.md`; exit 0, `DeviceScaleBenchmarkIT` 1/1 test đạt, Maven total 6 phút 10 giây.
- Môi trường: Linux `7.0.0-34-generic` amd64, Intel Core i5-1345U, 12 processor, RAM 15.633,9 MiB, Java 17.0.20.1, JVM heap tối đa 512 MiB, MySQL 8.4.11 Testcontainers, commit nền `9b9e4fd`.
- Dataset tổng hợp xác định gồm 1k/10k/100k device; read/export warm-up 1 và ghi 3 lần, import warm-up bằng dataset nhỏ nhất rồi ghi 1 lần/dataset. Full raw measurements và toàn bộ plan nằm tại `docs/benchmarks/device-scale-baseline-2026-10-09.md`.
- Median latency 100k: page đầu 438,772 ms; deep page 616,170 ms; keyword cuối bảng 378,796 ms; CSV export 1.669,511 ms; XLSX export 6.806,224 ms. Import 100k là một sample 225.442,547 ms (225,443 giây).
- Peak heap delta 100k: import 117,122 MiB; page đầu 1 MiB; deep page 2 MiB; keyword 1 MiB; CSV export tối đa 123,444 MiB; XLSX export 117 MiB. JVM không OOM dưới heap 512 MiB.
- `EXPLAIN ANALYZE` 100k trước index: deep page table scan 100k row + sort 100k row, actual khoảng 503 ms; keyword data table scan 100k row khoảng 332 ms; keyword count table scan khoảng 313 ms. Đây là baseline cho PRD-209, chưa phải bằng chứng sau tối ưu.
- Giới hạn: import chỉ có một sample mỗi mức; RSS là JVM process và không gồm MySQL container; export dùng counting/null output nên không đo network/browser/disk download; dataset chỉ là device tổng hợp, không mô phỏng assignment/repair hoặc tải đồng thời.
- Không đọc/sửa `.env`, không dùng database ngoài, không sửa frontend hoặc migration/index trong PRD-208.
- Sau khi thêm harness/report: `bash scripts/verify.sh safe` exit 0; documentation/diff, compile, 20 backend test, generator guard, benchmark isolation guard, 3 frontend test, lint và build 92 module đều đạt.
- `bash scripts/verify.sh integration` exit 0; 41/41 test đạt, 0 failure/error/skip trên MySQL 8.4.11 Testcontainers; Flyway V1/V2 đạt. `DeviceScaleBenchmarkIT` không thuộc suite mặc định và chỉ chạy khi được gọi rõ qua script có safety guard.
- GitHub Device CI push run `37940116019` tại commit `cd1ccbb` đạt `safe` 39 giây, `integration` 1 phút 12 giây và `compose-smoke` 5 phút 39 giây trên runner `ubuntu-24.04`.

## PRD-209 — Index truy vấn thiết bị ngày 2026-10-10

- Lệnh cuối: `bash scripts/benchmark-device-query-index.sh --confirm-isolated --output target/benchmarks/device-query-index-comparison.md --force`; exit 0, `DeviceQueryIndexBenchmarkIT` 1/1 test đạt, Maven total 1 phút 59 giây.
- Môi trường: Linux `7.0.0-34-generic` amd64, Java 17.0.20.1, MySQL 8.4.11 Testcontainers, database dùng một lần `device_test`, 100.000 device tổng hợp; warm-up 2, ghi 3 lần, 2 chu kỳ baseline/indexed.
- Candidate tối thiểu `CREATE INDEX idx_devices_name ON devices (name)` được triển khai bằng `V3__device_name_index.sql`. Trên bảng 100k, tạo index mất 916,747 và 869,184 ms ở hai chu kỳ local.
- Median application `Page`: trang đầu 447,520 -> 50,178 ms (cải thiện 88,79%); deep page 608,277 -> 624,786 ms (giảm 2,71%); keyword cuối bảng 454,039 -> 411,118 ms (cải thiện 9,45%).
- `EXPLAIN ANALYZE`: trang đầu đổi từ table scan 100k + sort sang index scan `idx_devices_name` và trả 100 row trong khoảng 6-9 ms ở hai plan. Deep page vẫn table scan + sort; index không được tuyên bố giải quyết deep offset.
- Lần thử đầu cho thấy `ORDER BY name` khiến keyword leading-wildcard chọn ordered index scan rất chậm. Production query keyword được giữ thứ tự case-insensitive xác định bằng `ORDER BY LOWER(name), id`, buộc plan phù hợp quay lại table scan + sort; integration test xác nhận keyword mixed-case và count đúng.
- Harness seed dùng multi-row JDBC insert 1.000 row/lô để phần chuẩn bị không chi phối thời gian chạy; harness chỉ drop/recreate candidate index sau khi tự xác nhận datasource Testcontainers.
- Report nguyên vẹn: `docs/benchmarks/device-query-index-comparison-2026-10-10.md`. Đây là số đo local, không phải SLA/capacity production.
- `bash scripts/verify.sh safe`: exit 0; compile, 20 backend test, generator/benchmark guards, 3 frontend test, lint và build 92 module đạt.
- `bash scripts/verify.sh integration`: exit 0; 42/42 test đạt trên MySQL 8.4.11; Flyway áp dụng V1/V2/V3 và test xác nhận `idx_devices_name` trong `information_schema`.
- Không sửa `frontend/` hoặc `.env`. Rollback code trước merge là bỏ V3 cùng logic keyword; sau khi V3 đã áp dụng, rollback schema thủ công tương ứng là `DROP INDEX idx_devices_name ON devices`, chỉ thực hiện theo quy trình migration/rollback đã duyệt.

## PRD-210/211 — Xác nhận người dùng và tích hợp Giai đoạn 2 ngày 2026-10-10

- Người dùng chạy `DeviceScaleBenchmarkIT` trên commit `681ab42` với Testcontainers MySQL 8.4.11, dataset 1k/10k/100k và heap tối đa 512 MiB; terminal xác nhận `BUILD SUCCESS`, 1 test đạt, exit 0 trong 8 phút 39 giây.
- Report local: `target/benchmarks/prd-210-user-confirmation.md`. Ở 100k: import 322.918,738 ms; median page đầu 439,314 ms, deep page 888,687 ms, keyword 426,418 ms, CSV 2.072,262 ms và XLSX 8.453,611 ms; không OOM.
- PR #5 merge branch `feature/device-query-index` vào `main` tại `681ab42dcbd39e7e8153e26b78ef2e9a4caad49e`; ba check `safe`, `integration`, `compose-smoke` đều xanh trên cả push và pull request (sáu check).

## PRD-301 đến PRD-304/307 — Tính đúng đắn khi request đồng thời ngày 2026-10-10

- Test-first focused run ban đầu chứng minh contract chưa thống nhất: assign thua bằng optimistic conflict code 1053, còn return và repair thua bằng HTTP 400. Sau review, contract được chốt ở HTTP 409 với mã nghiệp vụ riêng cho bốn transition.
- Thay đổi production tối thiểu: `assignDevice` lấy device bằng pessimistic write lock `findByIdForUpdate`; return, extension review và repair transition tiếp tục dùng các lock hiện hữu. Không thêm migration hoặc constraint vì không có invariant database nào bị phá vỡ trong race test.
- `ConcurrencyCorrectnessIntegrationTest` gửi hai HTTP request thật đồng thời qua `MockMvc`, dùng MySQL 8.4.11 Testcontainers riêng. Bốn scenario assign, return, approve/reject extension và complete/unrepairable repair đều có đúng một HTTP 200 và một HTTP 409; database được đọc lại để kiểm tra state/deadline/active assignment.
- Focused suite cuối: 4/4 test đạt, 0 failure/error/skip. Full `bash scripts/verify.sh integration`: exit 0; 46/46 test đạt, 0 failure/error/skip; Flyway V1/V2/V3 áp dụng trên database cô lập; tổng thời gian 2 phút 27 giây.
- Cổng cuối `bash scripts/verify.sh safe`: exit 0; kiểm tra tài liệu/diff, backend compile, 20 backend test, generator/benchmark isolation guards, 3 frontend test, lint và build 92 module đều đạt.
- Error contract: assign code 1022, return 1029, extension 1035, repair 1046 đều HTTP 409; optimistic conflict ngoài bốn trạng thái trên tiếp tục code 1053/HTTP 409. Response không lộ exception/database internals.
- PR #6 merge commit `bf2add0e6776b8b9504af505f2ffe98dba85048c`; sáu check push/PR đều đạt. Post-merge Device CI run `38040530264` đạt `safe` trong 47 giây, `integration` trong 1 phút 18 giây và `compose-smoke` trong 2 phút 12 giây.
- Review PRD-305 xác nhận `App.tsx` chỉ có dashboard thiết bị và các route xác thực; `deviceService.ts` chỉ gọi API `/devices`. Không có màn hình, type hoặc API call frontend cho assignment, return, extension hay repair, nên không có submit flow để ánh xạ bốn conflict code hoặc refresh.
- Các form hiện có đã chống submit lặp bằng state và thuộc tính `disabled`: login, forgot password và reset password dùng `submitting`; tạo device dùng `creating`. PRD-305 và G3-3 được đóng N/A, không sửa `frontend/` và không tạo màn hình ngoài phạm vi.

## PRD-306 — Demo concurrency do người dùng xác nhận ngày 2026-10-10

- Người dùng chạy `bash scripts/verify.sh integration` tại thư mục gốc project với Docker hoạt động và cung cấp ảnh terminal kết quả thật.
- Kết quả: `Tests run: 46, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS`; tổng thời gian 1 phút 53 giây, hoàn tất lúc 18:58:30 GMT+07:00.
- Full suite bao gồm 4/4 test trong `ConcurrencyCorrectnessIntegrationTest`. Mỗi scenario khởi động hai request đồng thời, yêu cầu đúng một HTTP 200 và một HTTP 409, sau đó đọc lại MySQL Testcontainers để xác nhận active assignment, return state, extension deadline và final repair state nhất quán.
- Bằng chứng này hoàn tất PRD-306 và đóng Giai đoạn 3; không sử dụng datasource thật hoặc secret.
- Sau khi tích hợp tài liệu, `bash scripts/verify.sh safe` đạt: backend compile thành công, 20/20 backend test đạt, generator/benchmark guards đạt, 3/3 frontend test đạt, lint và build 92 module thành công.

## PRD-401 — ADR email outbox ngày 2026-10-10

- Review source xác nhận sáu điểm gọi `EmailService` trực tiếp: một password reset, ba assignment reminder/overdue/report và hai extension notification. Password reset, reminder/overdue và extension notification có thể gọi SMTP trong transaction; daily report chặn scheduler trong lúc gọi mạng.
- Tạo `docs/decisions/002-durable-email-outbox.md`, ban đầu ở trạng thái Proposed. ADR chọn MySQL transactional outbox, claim ngắn bằng lease/`SKIP LOCKED`, unique idempotency key, retry có giới hạn, terminal states và không giữ database lock khi gọi SMTP.
- ADR giữ nguyên nguyên tắc ADR-001: raw reset token không lưu dạng rõ. Secret payload đề xuất AES-256-GCM với key tách JWT, key version/nonce và xóa ciphertext khi job terminal.
- ADR ghi rõ SMTP thuần không thể bảo đảm exactly-once trong cửa sổ send/ack; job mơ hồ chuyển `UNKNOWN` thay vì tự retry. Production chỉ được tuyên bố exactly-once khi provider có idempotency/đối soát tương đương.
- Tại checkpoint PRD-401 chưa tạo migration hoặc sửa production code; batch 20, lease 60 giây, retry và retention 30/90 ngày còn là giá trị đề xuất. Việc duyệt và implementation tiếp theo được ghi ở mục PRD-402/403 bên dưới.
- `bash scripts/verify.sh safe` sau khi tích hợp ADR: exit 0; documentation/diff check, backend compile, 20/20 backend test, generator/benchmark guards, 3/3 frontend test, lint và build 92 module đều đạt.

## PRD-402/403 — Duyệt ADR và nền tảng email job ngày 2026-10-10

- Người dùng duyệt ADR-002 cùng toàn bộ giá trị đề xuất. ADR chuyển sang Accepted; PRD-402 hoàn tất trước khi tạo migration.
- Flyway V4 tạo `email_jobs` với JSON payload, vùng secret ciphertext/nonce/key-version, unique SHA-256 idempotency key, status/attempt/lease/error/timestamps/version, ba index claim/lease/retention và các check constraint.
- Thêm `EmailJob`, `EmailJobStatus`, `EmailJobType`, `EmailJobRepository` và `EmailJobClaimService`. Claim chạy trong transaction ngắn bằng `FOR UPDATE SKIP LOCKED`, chuyển job sang `PROCESSING`, tăng attempt, gán owner/lease và flush trước khi trả ID.
- `DeviceApplicationTests` xác nhận đủ 4 migration, index claim ba cột và unique idempotency index trên MySQL Testcontainers.
- `EmailJobClaimIntegrationTest` chạy hai worker đồng thời, xác nhận ID không giao nhau, mỗi batch không vượt giới hạn, job bị skip được lượt kế tiếp claim, job tương lai và terminal không bị nhận.
- Lần full integration đầu có 1 failure do test giả định mỗi worker luôn lấy đủ batch; `SKIP LOCKED` hợp lệ khi trả batch nhỏ hơn trong lúc row/gap đang khóa. Assertion được sửa theo contract không trùng + eventual next claim, không thay đổi production claim code.
- Focused test sau sửa: 1/1 đạt, 0 failure/error/skip, `BUILD SUCCESS` trong 1 phút 24 giây.
- Full `bash scripts/verify.sh integration` cuối: 47/47 test đạt, 0 failure/error/skip, `BUILD SUCCESS` trong 3 phút 16 giây; MySQL 8.4.11 và Flyway V1-V4 đều đạt.
- `bash scripts/verify.sh safe` cuối: exit 0; documentation/diff check, backend compile, 20/20 backend test, generator/benchmark guards, 3/3 frontend test, lint và build 92 module đều đạt.
- Giới hạn: PRD-403 mới cung cấp schema và claim primitive. Chưa có enqueue producer, worker gửi SMTP, retry/backoff, cleanup, recovery `UNKNOWN` hoặc mã hóa secret payload; các phần này thuộc PRD-404/405/406.

## PRD-404 — Worker, retry và terminal state ngày 2026-10-10

- Thêm worker có feature flag `EMAIL_JOB_WORKER_ENABLED` mặc định `false`. Worker claim tối đa 20 job trong transaction ngắn, gửi song song bằng pool giới hạn 20 thread và chỉ cập nhật trạng thái trong transaction riêng; SMTP connect timeout 5 giây, read/write timeout 10 giây, ngắn hơn lease 60 giây.
- Adapter render snapshot JSON/template và dùng `Message-ID` ổn định theo job UUID. Hai lần gọi worker không gửi lại job đã `SENT`; unique idempotency key của V4 tiếp tục chặn enqueue trùng ở tầng database.
- Retry job thường dùng base delay 1 phút, 5 phút, 30 phút, 2 giờ; password reset dùng 30 giây, 2 phút, 5 phút. Mỗi delay có jitter dương tối đa 10%; `max_attempts` chặn retry vô hạn.
- Lỗi payload/template/address vĩnh viễn đi `DEAD`; lỗi runtime/SMTP retry được đi `RETRY_WAIT` hoặc `DEAD` khi hết attempt. Bảng chỉ lưu error code và summary cố định đã redaction, không lưu exception message/stack trace/recipient/payload.
- Job `PROCESSING` hết lease đi `UNKNOWN`, không tự retry vì SMTP outcome có thể mơ hồ. Secret ciphertext/nonce/key version được xóa khi vào `SENT`, `DEAD` hoặc `UNKNOWN`; cleanup xóa `SENT`/`EXPIRED` sau 30 ngày và `DEAD`/`UNKNOWN` sau 90 ngày.
- Unit test policy/Message-ID: 3/3 đạt. Lần gọi Maven trực tiếp đầu tiên lỗi Mockito self-attach; chạy lại với Byte Buddy javaagent chuẩn của project đạt. Đây là lỗi môi trường test, không phải assertion production.
- Focused `EmailJobClaimIntegrationTest`: 7/7 đạt trong 1 phút 16 giây trên MySQL 8.4.11/Flyway V1-V4; bao phủ claim đồng thời, gửi một lần, retry/backoff, max attempts, permanent failure, secret wipe, crash/lease recovery và retention cleanup.
- Lần full suite đầu phát hiện sender inject Jackson 2 `com.fasterxml` trong khi Spring Boot 4 auto-config Jackson 3 `tools.jackson`; các context không mock sender không khởi động. Sender được đổi sang mapper Jackson 3 của project, không thay đổi contract job.
- Full `bash scripts/verify.sh integration` sau sửa: 56/56 test đạt, 0 failure/error/skip, `BUILD SUCCESS` trong 3 phút 03 giây; MySQL 8.4.11 và Flyway V1-V4 đạt.
- `bash scripts/verify.sh safe`: exit 0; documentation/diff, backend compile, 20/20 backend test, generator/benchmark guards, 3/3 frontend test, lint và Vite build 92 module đều đạt. Không sửa source frontend.
- PRD-408 đóng N/A: HTTP contract không đổi, password reset tiếp tục response trung lập và frontend chưa có màn hình assignment/extension trong phạm vi; không có source frontend cần sửa.
- Giới hạn: chưa có producer enqueue và worker mặc định tắt, nên chưa có SMTP sandbox tắt/bật end-to-end. Password reset/mã hóa thuộc PRD-405; các producer còn lại thuộc PRD-406; bằng chứng vận hành SMTP thuộc PRD-409.

## PRD-405 — Password reset transactional outbox ngày 2026-10-11

- Flyway V5 liên kết job `PASSWORD_RESET` với reset token, thêm hạn dùng, index, foreign key `ON DELETE CASCADE` và check constraint bắt buộc metadata đúng loại job. `DeviceApplicationTests` xác nhận V1-V5 cùng constraint trên MySQL 8.4.11.
- `requestReset` tiếp tục khóa user và giữ HTTP 202 trung lập, nhưng không gọi SMTP. Token hash và email job được ghi trong cùng transaction; test gây lỗi cipher xác nhận cả token lẫn job rollback về số lượng ban đầu.
- Raw token được mã hóa AES-256-GCM bằng khóa 32 byte tách JWT, nonce ngẫu nhiên 12 byte, authentication tag 128 bit, AAD theo job type và key version để xoay khóa. Payload JSON chỉ chứa snapshot tên/thời hạn; test round-trip, ciphertext không chứa plaintext, tamper và unknown key đều đạt.
- Worker chỉ giải mã trong bộ nhớ ngay trước khi dựng reset link. Integration test chạy chuỗi enqueue → decrypt/render → JavaMailSender → `SENT`, kiểm tra secret ciphertext/nonce/key-version bị xóa và token dùng đúng một lần.
- Job quá hạn hoặc token đã dùng chuyển `EXPIRED`, không gửi và vô hiệu hóa token còn mở. Job hết retry hoặc lỗi vĩnh viễn chuyển `DEAD` và vô hiệu hóa token; lease mơ hồ chuyển `UNKNOWN` nhưng giữ token để tránh phủ nhận một email có thể đã được SMTP nhận.
- Focused unit suite `PasswordResetServiceImplTest,EmailJobSecretCipherImplTest,EmailJobSenderImplTest`: 12/12 đạt. Focused MySQL suite ban đầu đạt 13/13; sau khi thêm rollback test, `DeviceApplicationTests` đạt 4/4.
- Một lần chạy rollback test đầu thất bại do Mockito spy service có `Propagation.MANDATORY` kích hoạt interceptor ngay lúc stub. Test được sửa để gây lỗi tại cipher không transactional; production code không đổi và lần chạy lại đạt 4/4.
- Full `bash scripts/verify.sh integration`: exit 0; 64/64 test đạt, 0 failure/error/skip, `BUILD SUCCESS` trong 2 phút 42 giây; MySQL 8.4.11 và Flyway V1-V5 đều đạt.
- `bash scripts/verify.sh safe`: exit 0; documentation/diff, backend compile, 20/20 backend test, generator/benchmark guards, 3/3 frontend test, lint và Vite build 92 module đều đạt.
- `docker compose config --quiet` với bốn giá trị local-only tạm truyền qua environment: exit 0; không ghi giá trị kiểm tra vào `.env` hoặc repository.
- Cấu hình local/CI yêu cầu `LOCAL_EMAIL_JOB_SECRET_KEY` sinh bằng `openssl rand -base64 32`; Compose và profile dev bật worker, cấu hình mặc định vẫn tắt. Không sửa source frontend và contract HTTP không đổi, nên chưa phát sinh thay đổi UX PRD-408.
- Giới hạn: chưa chạy thao tác tắt/bật SMTP thật PRD-409; producer ngoài password reset còn đồng bộ tới PRD-406; rate limit/timing PRD-407 và password-reset PR-207/209 còn mở.

## PRD-406 — Chuyển các producer email còn lại sang outbox ngày 2026-10-11

- Graph/source review xác nhận năm điểm SMTP trực tiếp còn lại: reminder, overdue, daily report, extension requested và extension reviewed. Sau implementation, `rg` không còn `EmailService`, `sendEmail`, `sendHtmlEmail` hoặc `System.out` trong production Java; adapter đồng bộ cũ đã được xóa.
- Thêm enqueue service dùng `Propagation.MANDATORY`, snapshot JSON, tối đa 5 lần thử và SHA-256 idempotency key theo event/domain/version/recipient. Scheduler lặp chỉ nhận `false` khi logical job đã tồn tại thay vì tạo bản sao.
- Flyway V6 thêm assignment ID/deadline snapshot, index, foreign key `ON DELETE CASCADE` và check constraint chỉ cho phép metadata này ở `ASSIGNMENT_REMINDER`/`ASSIGNMENT_OVERDUE`. `DeviceApplicationTests` xác nhận đủ V1-V6 và constraint.
- Reminder/overdue không đánh dấu đã gửi khi enqueue. Worker khóa assignment, chỉ cập nhật `reminderNotifiedAt`/`overdueNotifiedAt` trong transaction chuyển job sang `SENT`; retry giữ marker rỗng. Job không còn khớp status/deadline hiện tại chuyển `EXPIRED` mà không gửi, tránh email cũ sau khi gia hạn.
- Daily report snapshot danh sách quá hạn một lần cho từng recipient/ngày. Extension request/review dùng template HTML chung có `th:text` để escape nội dung plain-text và enqueue cùng transaction tạo/review extension.
- Focused producer unit suite: 15/15 đạt (`AssignmentServiceImplTest` 8, `AssignmentExtensionServiceImplTest` 4, `EmailJobEnqueueServiceImplTest` 3).
- Focused MySQL suite: 17/17 đạt (`DeviceApplicationTests` 4, `EmailJobClaimIntegrationTest` 13), bao phủ migration V6, hai marker khi gửi thành công, retry không đặt marker và stale reminder `EXPIRED` không gọi sender.
- Full `bash scripts/verify.sh integration`: exit 0; 74/74 test đạt, 0 failure/error/skip, `BUILD SUCCESS` trong 3 phút 43 giây; MySQL 8.4.11 và Flyway V1-V6 đều đạt.
- `bash scripts/verify.sh safe`: exit 0; documentation/diff, backend compile, 20/20 backend test, generator/benchmark guards, 3/3 frontend test, lint và Vite build 92 module đều đạt. Không sửa source frontend.
- Giới hạn: PRD-409 tắt/bật SMTP sandbox thật và PRD-407 rate limit vẫn mở; SMTP thuần vẫn có cửa sổ outcome `UNKNOWN` như ADR-002.

## PRD-407 — Shared rate limit login/password reset ngày 2026-10-11

- Quyết định implementation dùng MySQL hiện có làm shared storage, không thêm Redis. Flyway V7 tạo `rate_limit_buckets` với primary key nhị phân 32 byte, expiry index và check count dương; migration V1-V7 chạy sạch trên MySQL 8.4.11.
- Login và password-reset request tiêu thụ bucket riêng theo IP và normalized identity trước khi authenticate/account lookup. Mặc định login `20/IP`, `5/identity` mỗi 5 phút; reset `10/IP`, `3/identity` mỗi 15 phút; ngưỡng/window có environment override.
- Bucket key là SHA-256 của action/scope/discriminator/window, vì vậy bảng không giữ email hoặc IP dạng rõ. Atomic MySQL upsert nằm trong transaction `REQUIRES_NEW`, nên request downstream thất bại hoặc trả 429 vẫn không rollback counter đã tiêu thụ.
- Vượt ngưỡng trả HTTP 429, code `1057`, message chung và `Retry-After`; web test xác nhận cả login lẫn reset dừng trước service nghiệp vụ. Password reset email tồn tại/không tồn tại dùng cùng đường kiểm tra; frontend hiện có đã render message Axios và chặn double-submit nên PRD-408 giữ N/A, không sửa source frontend.
- Fixed-window bucket cũ được xóa theo batch bằng scheduler. `server.forward-headers-strategy=native` cho phép container lấy client address qua trusted reverse proxy; deployment phải không public backend trực tiếp phía sau proxy không tin cậy.
- `bash scripts/verify.sh password-reset`: 20/20 test đạt, 0 failure/error/skip; gồm 3 rate-limit unit test và 5 controller security test.
- `DeviceApplicationTests`: 5/5 đạt; ngoài migration/index còn có 20 transaction đồng thời cùng increment một bucket và quan sát đúng đủ count 1..20, không mất update.
- Full `bash scripts/verify.sh integration`: exit 0; 80/80 test đạt, 0 failure/error/skip, `BUILD SUCCESS` trong 2 phút 54 giây; MySQL 8.4.11 và Flyway V1-V7 đều đạt.
- `bash scripts/verify.sh safe`: exit 0; documentation/diff, backend compile, 25/25 backend test, generator/benchmark guards, 3/3 frontend test, lint và Vite build 92 module đều đạt. Không sửa source frontend.
- Giới hạn: PR-209 timing comparison vẫn chưa chạy. Fixed window cho phép burst ở ranh giới window và tạo hai database increment cho mỗi request trong phạm vi.

## PRD-409 — SMTP failure/recovery do người dùng xác nhận ngày 2026-10-11

- Full Compose chạy backend, frontend, MySQL và Mailpit local. Người dùng dừng Mailpit rồi gửi đúng một yêu cầu password reset bằng tài khoản test; API vẫn chấp nhận yêu cầu và worker không giữ transaction nghiệp vụ trong lúc SMTP lỗi.
- Khi Mailpit dừng, job vẫn tồn tại ở `RETRY_WAIT`, `attempt_count=2`, `max_attempts=4`, `last_error_code=EMAIL_SEND_FAILED`; log backend thể hiện lỗi SMTP retryable theo job ID và không thấy raw reset token hoặc payload.
- Sau khi bật lại Mailpit, cùng job chuyển `SENT` ở `attempt_count=3`, có `sent_at` và xóa `last_error_code`. Truy vấn tổng hợp xác nhận đúng một password-reset job và đúng một job `SENT`.
- Mailpit API trả đúng một message; người dùng mở giao diện `http://localhost:8025` và xác nhận trực quan đúng một email. Không bấm reset link và không chép recipient/token vào tài liệu.
- Kết quả đóng PRD-409, G4-1 và G4-2 theo contract outbox. Đây không phải cam kết exactly-once tuyệt đối của SMTP: cửa sổ send/ack mơ hồ vẫn được xử lý bằng trạng thái `UNKNOWN` như ADR-002.
- Runbook diễn tập nằm tại `docs/runbooks/local-stack.md`; `bash scripts/verify.sh docs` đạt sau khi cập nhật hướng dẫn.

## PRD-410 — Tích hợp PR-207/208/209 ngày 2026-10-11

- Benchmark mới bắt buộc `--confirm-isolated`, bỏ mọi datasource environment bên ngoài và chỉ chạy `PasswordResetTimingBenchmarkIT` trên MySQL 8.4.11 Testcontainers/Flyway V1-V7. Mỗi nhánh dùng email test riêng; 10 cặp warm-up, 50 cặp ghi nhận và thứ tự xen kẽ.
- Baseline đầu không đạt: existing median 86,052 ms, missing 48,330 ms, lệch 37,722 ms và dominance 0,990. Việc đo đã phát hiện timing side-channel rõ ràng thay vì suy ra an toàn từ code.
- Thêm response timing guard mặc định 150 ms + jitter bảo mật 0-25 ms cho request reset hợp lệ. Giá trị cấu hình được, giới hạn 0-5 giây; 429 vẫn chạy trước lookup và không bị delay.
- Lần đo lại đạt: existing median 175,697 ms, missing 170,486 ms, lệch 5,210 ms, ratio 1,031 và dominance 0,560; toàn bộ response có cùng HTTP 202/body. Báo cáo: `docs/benchmarks/password-reset-timing-comparison-2026-10-11.md`.
- `bash scripts/verify.sh password-reset`: 23/23 đạt. `bash scripts/verify.sh integration`: 83/83 đạt trong 2 phút 53 giây. `bash scripts/verify.sh safe`: compile, 28/28 backend test, generator/benchmark guards, 3/3 frontend test, lint/build 92 module đều đạt.
- Rebuild riêng backend Compose bằng source mới thành công; backend, frontend, MySQL và Mailpit đều healthy. Hai smoke request cho email test không tồn tại sau restart đều HTTP 202, lần cold 341 ms và lần warm 187 ms; đây chỉ xác nhận image/config runtime, không thay thế benchmark hai nhánh.
- PR-207/208/209 có đủ bằng chứng trong hai bộ verification; PRD-410 và Giai đoạn 4 được đóng. Phải đo lại timing trên hạ tầng deploy nếu database thường xuyên vượt cửa sổ 150 ms.

## PRD-501 — Structured logging và redaction ngày 2026-10-11

- Graph/source audit và `rg -n "System\\.(out|err)|printStackTrace\\(" src/main/java` xác nhận production Java không còn console output trực tiếp. Các điểm cũ đã bị loại cùng adapter email đồng bộ ở PRD-406; PRD-501 thêm guard tự động để ngăn tái xuất hiện.
- Chuẩn hóa log API exception, email worker, assignment scheduler và rate-limit cleanup theo dạng `event=<tên ổn định> key=value`. Trường ghi log chỉ lấy từ allow-list gồm error/status code, exception type, UUID job, enum job type, attempt, failure kind và các bộ đếm; không ghi exception message, request/body/header, recipient, subject hoặc payload.
- `GlobalExceptionHandlerLoggingTest` đưa mật khẩu/JWT/reset token giả vào exception message; `EmailJobWorkerLoggingTest` đưa recipient, payload và exception message nhạy cảm vào failure path. Cả hai xác nhận metadata cần thiết có mặt và dữ liệu nhạy cảm không xuất hiện trong captured log.
- Focused command `bash ./mvnw -Dtest=GlobalExceptionHandlerLoggingTest,EmailJobWorkerLoggingTest test`: 2/2 test đạt, 0 failure/error/skip, `BUILD SUCCESS`.
- `bash scripts/verify.sh safe`: exit 0; documentation/diff và backend compile đạt, logging guard đạt, 30/30 backend test đạt, generator/benchmark guards đạt, 3/3 frontend test đạt, lint và Vite build 92 module thành công. Không sửa source frontend.
- `bash scripts/verify.sh integration` được thử nhưng dừng trước khi chạy test vì Docker daemon không truy cập được. PRD-501 không thay schema/database; bằng chứng của task là focused redaction tests và cổng `safe`. Full integration cần chạy lại khi Docker hoạt động ở một task cần Testcontainers.
- Phạm vi chưa bao gồm request/job correlation ID; phần này thuộc PRD-502. G5-1/G5-3 vẫn mở đến khi correlation, management endpoint và security review hoàn tất.

## PRD-502 — Request/job correlation ID ngày 2026-10-11

- Chốt contract `X-Request-ID`: backend giữ ID client khi khớp `[A-Za-z0-9][A-Za-z0-9._-]{0,63}`, nếu thiếu/sai thì sinh UUID; mọi response trả header này. Error `ApiResponse` thêm `requestId`, success body không đổi cấu trúc ngoài trường null bị bỏ qua.
- `RequestCorrelationFilter` đặt/khôi phục MDC, log một event hoàn tất request với ID, method, path không có query string, status và duration. Test xác nhận header chứa newline bị thay bằng UUID, nội dung log injection không xuất hiện và MDC được dọn sau request.
- Global exception logs và error body dùng cùng request ID. Web test rate-limit gửi ID cố định và xác nhận đồng thời response header, JSON `requestId` và log có cùng giá trị; response vẫn giữ HTTP 429/code 1057/`Retry-After`.
- Flyway V8 thêm `email_jobs.request_id` nullable kiểu ASCII 64 ký tự và index `idx_email_jobs_request_id`. Hai enqueue service lưu ID hiện tại; delivery record chuyển ID sang worker, worker khôi phục context và log đồng thời `request_id`, `job_id`, job type và attempt. Job scheduler ngoài HTTP giữ `request_id = NULL` và log `none`, truy vết bằng `job_id`.
- `DeviceApplicationTests` xác nhận đủ 8 migration và request-ID index trên MySQL 8.4.11. Password reset integration gắn `request-password-reset-integration`, đọc lại đúng ID từ row và log enqueue/delivery thành công thể hiện cùng request ID + job UUID.
- Lần full integration đầu: 86/87 đạt; test max-attempt nhận batch 2 thay vì 1 vì `DeviceAssignmentScheduler` thật chạy cron trong profile test và enqueue reminder giữa test. Production worker không gửi trùng. Scheduler nghiệp vụ/rate-limit được thêm property `app.scheduler.enabled` mặc định bật và profile test đặt `false` để cô lập suite.
- Full `bash scripts/verify.sh integration` sau sửa: 87/87 test đạt, 0 failure/error/skip, `BUILD SUCCESS` trong 3 phút 13 giây; Flyway V1-V8 chạy sạch trên các MySQL Testcontainers.
- Focused correlation/redaction suite đạt 4/4. `bash scripts/verify.sh safe` đạt 32/32 backend test, logging/generator/benchmark guards, 3/3 frontend test, lint và Vite build 92 module. Không sửa source frontend.
- PRD-506 đã hết blocker tại thời điểm PRD-502 đóng; frontend sau đó đã triển khai body `requestId` với fallback `X-Request-ID`. G5-1 vẫn cần manual drill PRD-508.

## PRD-503 — Actuator health và bảo vệ management endpoints ngày 2026-10-11

- Thêm Spring Boot Actuator; HTTP exposure chỉ gồm `health`. Ba probe công khai là
  `/actuator/health`, `/actuator/health/liveness` và `/actuator/health/readiness`;
  `show-components`/`show-details` đều `never`.
- Readiness gồm application readiness state và database. Mail health indicator bị tắt
  có chủ đích vì SMTP không nằm trên critical request path: email đã được lưu bền vững
  và worker retry; SMTP hỏng không được làm backend mất readiness.
- Security chỉ `permitAll` ba health path. Integration test xác nhận ba probe trả HTTP
  200/`UP`, body không có `components`, health response có `X-Request-ID`; actuator
  index và `/actuator/prometheus` không cho truy cập ẩn danh, trả HTTP 401. Metrics
  chưa được expose trước PRD-504/505.
- Compose backend healthcheck chuyển từ OpenAPI sang readiness. `docker compose config
  --quiet` đạt; `docker compose up --build --wait -d backend` rebuild image và kết thúc
  với backend healthy. Runtime curl xác nhận health/liveness/readiness đều HTTP 200,
  chỉ trả status (health root thêm tên group), còn Prometheus trả 401.
- Focused `DeviceApplicationTests`: 6/6 đạt trên MySQL 8.4.11 Testcontainers/Flyway
  V1-V8. `bash scripts/verify.sh integration`: 88/88 đạt trong 3 phút 18 giây.
  `bash scripts/verify.sh safe`: 32/32 backend test, logging/generator/benchmark guards,
  3/3 frontend test, lint và Vite build 92 module đều đạt. Không sửa source frontend.
- PRD-503 đóng. V-15/V-16 vẫn giữ một phần cho đến khi PRD-505 thêm dashboard và
  kiểm tra lại scrape/network boundary ở môi trường demo.

## PRD-504 — Metrics vận hành ngày 2026-10-11

- Thêm Prometheus Micrometer registry nhưng giữ HTTP exposure chỉ gồm `health`; scrape
  endpoint chưa được mở trước khi PRD-505 thiết lập network boundary. HTTP request
  histogram bật và mọi meter có common tag `application=device`.
- Actuator auto-config cung cấp `http.server.requests`, JDBC/Hikari connection pool và
  JVM metrics. Integration test thực hiện request thật rồi xác nhận request timer và
  `jdbc.connections.active` gauge tồn tại trong application registry.
- `OperationalMetrics` thêm scheduler duration theo tên task + success/failure; email
  queue gauge cho đủ 7 status; worker batch size, delivery theo enum type/outcome,
  recovered lease và retention cleanup. Không có request ID, job ID, email, exception
  message hoặc giá trị người dùng trong metric tags.
- Assignment notification, daily report, rate-limit cleanup, email poll/recovery/cleanup
  đều được instrument. Worker ghi sent/retryable/permanent/unknown/skipped và giữ nguyên
  luồng transaction/delivery hiện có.
- `OperationalMetricsTest` xác nhận gauge/counter/summary/timer và cả scheduler failure
  path. Focused `DeviceApplicationTests` đạt 6/6 trên MySQL 8.4.11/Flyway V1-V8, gồm
  request, datasource và 7 email status gauges.
- Lần Maven focused đầu không dùng helper của project nên Mockito không self-attach
  Byte Buddy; không có assertion chạy. Cổng chuẩn `bash scripts/verify.sh safe` tự gắn
  javaagent và đạt 33/33 backend test, 3 frontend test file, lint/build 92 module.
- `bash scripts/verify.sh integration` đạt 89/89, 0 failure/error/skip, `BUILD SUCCESS`
  trong 2 phút 08 giây. PRD-504 đóng; PRD-505 sẽ expose/scrape registry qua Compose
  profile và dựng dashboard.

## PRD-506 — Frontend support/request ID ngày 2026-10-11

- Review thay đổi do Antigravity thực hiện trong `frontend/`: `ApiResponse` có
  `requestId?`; Axios error ưu tiên body `requestId`, fallback header `X-Request-ID`;
  error message thêm `Mã tham chiếu` và không lặp ID nếu message đã chứa ID.
- Response interceptor ghi nhận request ID gần nhất cho cả success/error. Không hiển
  thị stack trace, token hoặc response nội bộ; request ID chỉ là mã hỗ trợ.
- Static/behavioral test bao phủ body priority, header fallback, Axios headers `.get`,
  fallback message và duplicate guard. `bash scripts/verify.sh safe` xác nhận frontend
  static tests, Oxlint và Vite production build 92 module đều đạt.
- Codex không sửa source frontend; chỉ review bằng diff/kết quả cổng và tích hợp trạng
  thái vào tài liệu chung. PRD-506 đóng; G5-1 còn cần user drill PRD-508.

## PRD-505 — Prometheus/Grafana local ngày 2026-10-11

- Thêm profile Compose `monitoring` với Prometheus `v3.5.0` và Grafana `12.1.0`;
  cả hai chỉ bind `127.0.0.1`. Backend dùng profile `dev,monitoring`, đặt management
  server ở cổng `8081` chỉ expose nội bộ Compose và healthcheck chuyển sang readiness
  trên cổng này.
- Security dùng `EndpointRequest` theo đúng management application context: health và
  Prometheus được phép ở 8081; endpoint ngoài allow-list trả 401. Trên cổng API 8080,
  cả `/actuator/health` và `/actuator/prometheus` ẩn danh đều trả 401. `docker compose
  ps` chỉ publish backend 8080, không publish 8081.
- `promtool check config /etc/prometheus/prometheus.yml` đạt. Prometheus target
  `device-backend` scrape `backend:8081/actuator/prometheus`, `health=up`, không có
  `lastError`; query `up{job="device-backend"}` trả 1 và `device_email_jobs` trả đủ
  7 chuỗi status với common tag `application=device`.
- Grafana health API trả HTTP 200/database `ok`; dashboard provisioned UID
  `device-operations` trả HTTP 200 và có 8 panel: scrape health, HTTP p95/error,
  JDBC pool, email jobs/delivery outcome, scheduler duration và JVM heap. Anonymous
  chỉ là Viewer, không tạo admin; runbook cảnh báo không dùng cấu hình này cho Internet.
- `docker compose --profile monitoring config --quiet`, backend compile, dashboard JSON
  parse và Compose build đều đạt. `bash scripts/verify.sh safe` đạt 33/33 backend test,
  logging/generator/benchmark guards, 3 frontend test file, Oxlint và Vite build 92
  module. Focused `DeviceApplicationTests` sau khi tách matcher management đạt 6/6 trên
  MySQL 8.4.11/Flyway V1-V8, xác nhận profile mặc định vẫn công khai ba health probe và
  chặn actuator index/Prometheus bằng 401. Full integration 89/89 của PRD-504 không chạy
  lại vì PRD-505 không đổi schema hoặc nghiệp vụ; runtime Compose thật đã kiểm tra riêng
  security/scrape/provisioning.
- PRD-505 và G5-2 đóng. V-16/G5-3 vẫn một phần đến security review PRD-509 và kiểm tra
  ranh giới ở môi trường demo; PRD-508 vẫn cần người dùng lưu ảnh dashboard.

## PRD-507 — Runbook chẩn đoán vận hành ngày 2026-10-11

- Thêm `docs/runbooks/operations-troubleshooting.md` với luồng chỉ đọc cho API chậm,
  DB connection pool cạn và scheduler/email job kẹt. Mỗi luồng bắt đầu từ time window,
  request/job ID và metrics rồi mới sang log/database metadata; restart, kill connection,
  sửa job hoặc tăng pool không được coi là bước chẩn đoán mặc định.
- Runbook dùng đúng event hiện có (`http_request_completed`, API rejection/failure,
  scheduler batch, email delivery/recovery), PromQL của dashboard và trạng thái outbox.
  Truy vấn database cố ý không chọn recipient, subject, payload, secret columns hoặc SQL
  text; `DEAD`/`UNKNOWN` không được sửa thủ công về `PENDING`.
- Chạy thử trên local stack: năm PromQL latency/pool/scheduler/delivery/job đều trả
  `status=success`; ba truy vấn MySQL process/job/global-status chạy thành công; worker
  flag đọc riêng trả `true`. Latency có thể là `NaN` và delivery rỗng khi cửa sổ không
  có traffic, đúng giới hạn đã ghi trong runbook.
- `bash scripts/verify.sh docs`, `docker compose --profile monitoring config --quiet`,
  dashboard JSON parse và `git diff --check` đều đạt. PRD-507 đóng; G5-1 vẫn chờ người
  dùng thực hiện PRD-508 bằng một lỗi test và lưu ảnh dashboard.

## PRD-508/509 — User drill và security review ngày 2026-10-11

- Người dùng cố ý đăng nhập sai trên frontend local, thấy “Mã tham chiếu” và dùng cùng
  request ID tìm log. Runtime log có đúng `api_request_rejected` code 1001/HTTP 401 và
  `http_request_completed` cho `POST /api/auth/token`; không có password/JWT/body.
  Người dùng xác nhận đã lưu ảnh dashboard. PRD-508, G5-1 và bằng chứng thủ công của
  V-14/V-15 đạt.
- Codebase graph/source review bao phủ `SecurityConfig`, request correlation, global
  exception handler, operational metrics và email worker. Source search xác nhận không
  có `System.out`/`System.err`/`printStackTrace`; metric tags chỉ gồm status, scheduler,
  outcome và enum job type. Redaction tests kiểm tra exception message, recipient,
  payload, password, JWT và reset token không xuất hiện trong log.
- Runtime security boundary: Compose chỉ publish backend `127.0.0.1:8080`; host kết nối
  `127.0.0.1:8081` thất bại. Trên API 8080, health và Prometheus đều 401. Từ nội bộ
  backend, readiness và Prometheus trả 200, body health chỉ `status=UP`; `/actuator/info`
  trả 401. Prometheus target `backend:8081` vẫn `up`; Grafana health và dashboard UID
  `device-operations` trả 200.
- `bash scripts/verify.sh safe` cuối đạt 33/33 backend test, production logging guard,
  generator/benchmark guards, 3 frontend test file, Oxlint và Vite build 92 module.
  Codebase-memory coverage không ghi nhận gap trên 14 file bằng chứng và bốn scope đã
  review; kết luận vẫn dựa thêm trên source search/test/runtime vì graph là best-effort.
- PRD-509, V-16 và G5-3 đóng cho local Compose. Giai đoạn 5/Gate G5 hoàn tất; security
  boundary phải được kiểm tra lại sau khi deploy demo, và Grafana anonymous Viewer
  tuyệt đối không dùng cho Internet.

## PRD-601/602 — Chuẩn bị ban đầu ngày 2026-10-12 (provider đã thay đổi)

- Người dùng chọn demo CV 0đ và cho phép tiếp tục cấu hình. Phương án ban đầu là
  Koyeb Free backend + Aiven MySQL Free + Render Static Site; phương án này sau đó
  bị thay bằng Render backend + Resend như mục cập nhật ngay bên dưới. Không gọi API
  cloud, nhập secret, push source hoặc chạy migration ngoài local/Testcontainers
  trong bước chuẩn bị ban đầu này.
- `ApiCorsConfiguration` allow-list origin chính xác, không credential/wildcard,
  chỉ `/api/**`. Khi không có origin cấu hình ở local, không đăng ký mapping CORS
  để giữ luồng same-origin/proxy. Web tests mới kiểm tra preflight không JWT,
  protected API vẫn 401, origin giả 403 trước service và error 429 expose cùng
  X-Request-ID/Retry-After. HTTP/JSON contract không đổi.
- `bash scripts/verify.sh safe` sau source cuối đạt 37/37 backend test, compile,
  logging/generator/benchmark guards, 3 frontend test file, Oxlint, Vite build 92 module.
  `bash scripts/verify.sh integration` đạt 93/93, 0 failure/error/skip, BUILD SUCCESS
  trong 2 phút 42 giây trên MySQL 8.4.11/Flyway V1-V8; không dùng database Aiven.
- Image `device-demo:prd602` build bằng `docker/Dockerfile.demo` riêng: UID 10001,
  profile demo, heap 256 MiB/metaspace 128 MiB/direct 32 MiB, SerialGC, C1 JIT và
  timezone Việt Nam. Entry point import CA vào JKS runtime, dùng store password env
  sinh riêng; Hikari VERIFY_IDENTITY, tối đa 5 connections; email batch/threads 2,
  Tomcat 16 threads; SMTP STARTTLS bắt buộc. Không sửa Dockerfile/Compose local.
- Build lần đầu dừng tại `COPY --chmod` vì local Docker dùng legacy builder;
  đổi thành COPY + RUN chmod và build lại đạt. Hai smoke thử nghiệm với JVM tiered
  mặc định được chủ động dừng trước readiness vì startup rất chậm dưới hard CPU cap;
  không coi đó là assertion failure hoặc OOM. Image cuối với C1 JIT là `33f2c124218b`.
- `bash scripts/smoke-demo.sh --confirm-isolated` trên image cuối exit 0. MySQL TLS
  bắt buộc, VERIFY_IDENTITY với CA runtime, Flyway V1-V8, readiness UP không detail,
  login admin và GET devices phân trang đều đạt. CORS origin đúng được phép, origin
  giả 403, protected API/Prometheus không JWT 401. Backend giới hạn RAM/swap 512 MiB
  và CPU 0,1 đạt readiness sau 417 giây; mẫu memory sau request là 261,7 MiB, OOMKilled
  false. Đây là một phép smoke trên Linux/Docker local với CPU hard cap, không phải
  số chịu tải, peak RSS hay dự báo cold start Koyeb. Các container/anonymous volumes,
  network và credential/cert tạm đã được tự dọn, xác nhận không còn container smoke.
- Guard đã chạy: smoke từ chối thiếu `--confirm-isolated`; image từ chối thiếu CA
  và profile `demo,monitoring` trước khi Java startup. Shell syntax và diff đạt.
- Runbook `docs/runbooks/free-demo-deployment.md` có workdir monorepo, healthcheck,
  runtime env/secret checklist và bàn giao PRD-603 cho Antigravity. PRD-606/608/609
  còn kiểm tra provider thật, SMTP TLS, client IP/rate limit sau proxy và timing
  neutral response trên Aiven. PRD-602 đóng cho phần chuẩn bị/kiểm chứng local;
  backup/restore/rollback và Gate G6 vẫn mở.

### Cập nhật provider và Resend HTTPS API

- Phương án hiện tại thay Koyeb/SMTP bằng Render Free Web Service và Resend HTTPS
  API. Koyeb không còn được dùng vì luồng đăng ký mới yêu cầu gói trả phí; Render
  Free chặn outbound SMTP phổ biến nên backend demo không phụ thuộc SMTP. Local/dev
  vẫn dùng `JavaMailSender` và Mailpit, không làm thay đổi diễn tập PRD-409.
- Aiven MySQL 8.4.8 Free đang Running; người dùng đã tạo riêng database
  `device_demo` và giữ nguyên các database cũ. Render Dashboard đã có nhưng chưa tạo
  Web Service hoặc Static Site. Người dùng đã xác nhận nhận được email onboarding
  Resend tại đúng một địa chỉ; đây chỉ là bằng chứng tài khoản/API hoạt động, chưa
  phải email do ứng dụng Device trên cloud gửi.
- `EmailJobSenderImpl` tiếp tục dựng template và giải mã password-reset token trong
  memory rồi giao payload đã render cho `EmailTransport`. SMTP là transport mặc
  định; profile `demo` chọn Resend. Resend dùng HTTPS timeout hữu hạn, stable
  `Idempotency-Key` theo `email_jobs.id`, phân loại 408/425/429/5xx là retryable,
  lỗi 4xx còn lại là permanent và không đưa recipient/key/body vào exception.
- Demo bắt buộc `RESEND_ALLOWED_RECIPIENTS`; recipient ngoài allow-list trở thành
  permanent failure trước khi gọi mạng. Với sender onboarding mặc định, phạm vi demo
  chỉ là đúng email chủ tài khoản đã xác nhận, không tuyên bố gửi tùy ý cho người dùng
  công khai và không tuyên bố exactly-once tuyệt đối.
- Unit tests mới bao phủ render/delegation, Message-ID SMTP, JSON/idempotency/
  allow-list và phân loại response Resend, cùng request HTTPS/timeout/header của JDK
  client. `bash scripts/verify.sh safe` cuối exit 0: 45 backend test, 0 failure/error/
  skip; 3 frontend test, Oxlint và Vite build 92 module đều đạt.
- `bash scripts/verify.sh integration` exit 0: 99 test, 0 failure/error/skip, BUILD
  SUCCESS trong 2 phút 17 giây trên MySQL 8.4.11 Testcontainers và Flyway V1-V8;
  không dùng Aiven hoặc gọi Resend thật.
- Smoke đầu của image Resend phát hiện Spring không chọn được constructor có
  dependency của `ResendEmailTransport`. Constructor production đã được đánh dấu
  `@Autowired`; smoke script cũng được bổ sung diagnostic lọc theo startup event và
  che JDBC URL. Sau sửa, cổng `safe` chạy lại đạt và image cuối
  `device-demo:prd602` có ID `3b3fbf302cc0`.
- `bash scripts/smoke-demo.sh --confirm-isolated` trên image cuối exit 0. Backend đạt
  readiness sau 425 giây với giới hạn 512 MiB/0,1 CPU; mẫu memory cuối là
  268,3 MiB/512 MiB. MySQL VERIFY_IDENTITY TLS, Resend demo configuration, Flyway
  V1-V8, login, devices phân trang, CORS và management boundary đều đạt. Script dùng
  API key giả, không tạo email job và không gọi Resend.
- PRD-602 tiếp tục đóng ở phạm vi chuẩn bị backend/local verification. PRD-601/606/
  608 vẫn mở cho Render resources, secret store, migration Aiven, deploy thật,
  browser smoke và email do ứng dụng cloud gửi. Backup/restore/rollback và Gate G6
  vẫn mở.

## PRD-603 — Chuẩn bị frontend Render Static Site ngày 2026-10-12

- Antigravity bàn giao cấu hình frontend cho quy trình ba bước: deploy Static Site
  với `https://placeholder.invalid/api` để lấy frontend origin, dùng origin đó cấu
  hình backend, sau đó thay `VITE_API_BASE_URL` bằng backend HTTPS URL và redeploy.
- `frontend/render.yaml` khai báo build `npm ci && npm run build`, publish `./dist`,
  SPA rewrite `/*` sang `/index.html`, ba security header, placeholder build-time env
  và monorepo root `IdeaProjects/device/device/frontend`.
- Review tích hợp phát hiện bản bàn giao ban đầu dùng `type: static`, không khớp
  Blueprint schema chính thức. Cấu hình đã được sửa thành `type: web` kết hợp
  `runtime: static`; README ghi rõ Blueprint Path vì file nằm dưới `frontend/`.
- `frontend/.env.example` và `frontend/README.md` ghi rõ cấu hình local, placeholder
  và backend URL thật; không chứa secret hoặc hostname cloud chưa tồn tại.
- `bash scripts/verify.sh frontend` exit 0 ngày 2026-10-12: 3/3 test file đạt,
  Oxlint đạt, TypeScript/Vite production build đạt với 92 module.
- PRD-603 đóng cho phạm vi chuẩn bị source/local verification. Chưa tạo Render Static
  Site, chưa có frontend/backend hostname thật và chưa chạy browser cloud smoke;
  các bằng chứng đó tiếp tục thuộc PRD-601/606/608 và Gate G6.

## PRD-604/605 — CI/CD, rollback và backup/restore guard ngày 2026-10-12

- Workflow Device CI thêm `deploy-demo`, chỉ chạy trên ref `main` từ push hoặc manual
  có `deploy_demo=true`, sau `safe`, `integration`, `compose-smoke` và khi
  `DEMO_DEPLOY_ENABLED=true`. Job dùng GitHub
  environment `demo`; deploy hook/API token ở secret, service ID/public origin ở
  variable. Backend được deploy và chờ `live` trước frontend; mỗi hook nhận đúng
  `${github.sha}`.
- `scripts/render-deploy-and-wait.sh` bắt buộc `--confirm-deploy`, chỉ chấp nhận endpoint
  Render HTTPS, giữ hook/API token trong curl config tạm mode 600, yêu cầu deploy ID,
  poll API đến `live`, kiểm tra commit nếu API trả commit và fail khi queued không ID,
  timeout hoặc terminal failure. Không gọi Render trong local verification.
- `scripts/post-deploy-check.sh` poll readiness rồi kiểm tra frontend root/deep-link,
  nosniff/DENY/no-referrer, bundle không còn placeholder và chứa backend `/api`, API
  thiếu JWT 401, Prometheus public 401, exact-origin CORS/no credentials và origin giả
  403. Mock backend/frontend loopback end-to-end cuối đạt toàn bộ assertions; lần chạy
  đầu trong sandbox không mở được socket và test harness lần hai thoát sớm, sau khi
  chạy ngoài sandbox với harness sửa thì script exit 0.
- Rollback runbook yêu cầu tắt deploy gate/auto-deploy, chọn cặp deploy known-good,
  tắt worker nếu cần, giữ migration forward-only và chạy lại smoke. Không có down
  migration hoặc restore thẳng backup vào database demo chính.
- `scripts/demo-db-backup-restore.sh` giới hạn backup đúng `device_demo`, option file
  mode 600 và output ở ngoài Git, TLS `VERIFY_IDENTITY` + CA hiện hữu, dump nhất quán,
  không overwrite và tạo SHA-256. Restore xác minh checksum, cấm `CREATE/DROP DATABASE`
  và `USE`, chỉ nhận database rỗng `device_demo_restore_*`, rồi in count không nhạy cảm.
- `bash scripts/verify.sh safe` exit 0: 45/45 backend test, production logging,
  generator/ba benchmark guard, deploy/backup guard, 3/3 frontend test, Oxlint và
  Vite build 92 module đạt. Workflow/render YAML parse và `git diff --check` đạt;
  `bash scripts/verify.sh frontend` chạy lại sau quote enum `"off"` cũng đạt.
- Trước lượt commit tích hợp, `bash scripts/verify.sh integration` được chạy lại bằng
  Docker/Testcontainers: 99/99 test đạt, 0 failure/error/skipped, MySQL 8.4.11 áp dụng
  thành công Flyway V1-V8; Maven `BUILD SUCCESS` trong 4 phút 06 giây.
- MySQL client không có trên máy hiện tại và không có cloud mutation trong PRD-605,
  nên logical dump/restore thật chưa chạy. PRD-604/605 đóng cho implementation/local
  guard; PRD-606/607/608/609 và cả ba Gate G6 tiếp tục mở.

## Lần chạy baseline 2026-10-08

- Phạm vi: kiểm tra tài liệu/diff, backend compile, nhóm test password reset/JWT/security và frontend lint/build.
- Lệnh: `bash scripts/verify.sh safe` tại thư mục gốc project.
- Kết quả: thành công, exit code 0.
- Backend: compile thành công; 15 test thành công, không có failure/error/skipped.
- Frontend: Oxlint thành công; TypeScript/Vite production build thành công với 92 module.
- Giới hạn: script không chạy MySQL integration, Flyway trên database test, SMTP, browser E2E, concurrency, benchmark hoặc backup/restore. Tất cả các mục tương ứng tiếp tục là `Chưa chạy`.

## Kết luận

- Gate G1 đến G5 đã đạt. PRD-501 đến PRD-509 đã đóng với implementation, test/runtime local, frontend review, user drill và security review tương ứng.
- Hệ thống vẫn chưa được tuyên bố production-ready vì deploy/backup/restore/rollback của Giai đoạn 6 và portfolio/final review của Giai đoạn 7 còn mở.
