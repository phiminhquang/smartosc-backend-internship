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
| Cổng an toàn hiện tại | `bash scripts/verify.sh safe` | Thành công | 2026-10-10: compile, 20 backend test, generator/benchmark guards, 3 frontend test, lint/build 92 module |
| Backend compile | Chạy bởi `bash scripts/verify.sh safe` | Thành công | Maven `BUILD SUCCESS` |
| MySQL migration/integration | `bash scripts/verify.sh integration` | Thành công | 2026-10-10: 42 test; MySQL 8.4.11 tạm; Flyway V1/V2/V3 |
| Backend test mục tiêu | Chạy bởi `bash scripts/verify.sh safe` | 20 thành công | 0 failure, 0 error, 0 skipped |
| Frontend contract/security tests | `node --test` qua `scripts/verify.sh` | Thành công | 3 file static/contract; không thay thế browser test |
| Frontend lint | Chạy bởi `bash scripts/verify.sh safe` | Thành công | Oxlint không báo lỗi |
| Frontend build | Chạy bởi `bash scripts/verify.sh safe` | Thành công | TypeScript/Vite, 92 module transformed |
| Compose local smoke | Build/start + frontend/API/Mailpit | Thành công | 4 service healthy; SPA route/header và API smoke qua Nginx proxy đạt |
| Browser E2E | `bash scripts/verify.sh frontend-e2e` | Thành công | Playwright Chromium, 1 test pass trong 1.1 phút; điều khiển DOM thật qua full Compose |
| Browser manual PRD-110 | Người dùng chạy full Compose và kiểm tra qua trình duyệt/Mailpit | Thành công | 4 container healthy; email/link/reset/login đạt; token biến mất khỏi URL; link cũ bị từ chối |
| GitHub Device CI | `safe`, `integration`, `compose-smoke` | Thành công | Push run và PR run của commit `5837be2` đều xanh |
| Data-scale benchmark | Dataset/máy/lệnh phải được ghi | Thành công | Baseline 1k/10k/100k, raw latency, heap/RSS và plan lưu tại `docs/benchmarks/device-scale-baseline-2026-10-09.md` |
| Device query index benchmark | `bash scripts/benchmark-device-query-index.sh --confirm-isolated` | Thành công | 100k trước/sau, 2 chu kỳ x 3 lần đo; report tại `docs/benchmarks/device-query-index-comparison-2026-10-10.md` |
| Concurrent requests | MySQL integration test thật | Chưa chạy | |
| SMTP failure/recovery | Tắt/bật SMTP test | Chưa chạy | |
| Backup/restore | Restore vào database cô lập và kiểm tra | Chưa chạy | |

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
| V-02 | Full test/migration | Chỉ dùng MySQL cô lập, không thể chạm Aiven ngoài ý muốn | 42 test đạt trên MySQL Testcontainers, Flyway V1/V2/V3; biến DB môi trường bị bỏ | Đạt |
| V-03 | Password reset E2E | Email/link/reset/login/token revocation đúng contract | Playwright Chromium đạt email/link/reset/login, URL/referrer và mật khẩu cũ/mới; JWT cũ có backend test nhưng chưa kiểm tra trên browser/Compose | Một phần |
| V-04 | API list với dữ liệu lớn | Trả page có giới hạn/filter/sort đúng contract | MySQL 8.4.11 đạt 100k row; so sánh PRD-209: page đầu 447,520 -> 50,178 ms, deep page 608,277 -> 624,786 ms | Đạt |
| V-05 | Request size quá giới hạn | Bị từ chối hoặc giới hạn theo contract | `size=101` trên users và devices trả HTTP 400/code 1055 trong integration test | Đạt |
| V-06 | Export dữ liệu lớn | Không bắt buộc nạp toàn bảng vào heap; file đúng | 100k: CSV median 1.669,511 ms/peak heap delta 123,444 MiB; XLSX median 6.806,224 ms/117 MiB; output đã có unit/integration test đúng định dạng | Đạt |
| V-07 | Import file lớn/lỗi dòng | Xử lý theo giới hạn và báo lỗi xác định | 100k CSV 7.100.032 byte import đủ trong 225,443 giây; peak heap delta 117,122 MiB; rollback/header/BOM/size đã có integration/unit test | Đạt |
| V-08 | Query trước/sau index | Có query plan, dataset và số đo lặp lại được | 100k, hai chu kỳ x ba lần: page đầu cải thiện 88,79% và plan dùng `idx_devices_name`; keyword +9,45%, deep page -2,71%; report đầy đủ đã lưu | Đạt |
| V-09 | Hai request assign cùng device | Chỉ một kết quả hợp lệ; không có hai assignment mở | Chưa chạy | Chưa chạy |
| V-10 | Hai request return/review/repair | State transition không bị lặp hoặc mâu thuẫn | Chưa chạy | Chưa chạy |
| V-11 | SMTP tắt khi tạo email | Nghiệp vụ/job theo contract; job không mất | Chưa chạy | Chưa chạy |
| V-12 | SMTP bật lại | Job retry và email hoàn tất đúng một lần | Chưa chạy | Chưa chạy |
| V-13 | Gọi reset/login quá mức | Rate limit hoạt động giống nhau, không lộ tài khoản | Chưa chạy | Chưa chạy |
| V-14 | Lỗi API có request ID | Tìm được log liên quan, response không lộ nội bộ | Chưa chạy | Chưa chạy |
| V-15 | Dashboard vận hành | Thấy health, latency/error, DB và email job cơ bản | Chưa chạy | Chưa chạy |
| V-16 | Management endpoint từ public | Không truy cập trái phép | Chưa chạy | Chưa chạy |
| V-17 | Backup/restore | Restore vào DB cô lập, dữ liệu và login được xác nhận | Chưa chạy | Chưa chạy |
| V-18 | Deploy lỗi/rollback | Quay về artifact/config trước theo runbook | Chưa chạy | Chưa chạy |
| V-19 | Frontend với contract mới | Pagination/conflict/support ID hiển thị đúng | Chưa chạy | Chưa chạy |
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
- Email hiện còn đồng bộ trong request/transaction ở các luồng quan trọng.
- Tám collection PRD-201 đã có phân trang. Export thiết bị không còn dùng `findAll()`/`byte[]`; CSV/XLSX stream theo keyset batch và đã có số đo 100k.
- Index `devices(name)` tối ưu trang đầu. Deep offset vẫn scan/sort toàn bảng và keyword leading-wildcard vẫn table scan; deferred join/full-text chưa được thêm vì ngoài bằng chứng và phạm vi hiện tại.
- Chưa có kết quả concurrent integration test, backup/restore hoặc deploy demo.

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

## Lần chạy baseline 2026-10-08

- Phạm vi: kiểm tra tài liệu/diff, backend compile, nhóm test password reset/JWT/security và frontend lint/build.
- Lệnh: `bash scripts/verify.sh safe` tại thư mục gốc project.
- Kết quả: thành công, exit code 0.
- Backend: compile thành công; 15 test thành công, không có failure/error/skipped.
- Frontend: Oxlint thành công; TypeScript/Vite production build thành công với 92 module.
- Giới hạn: script không chạy MySQL integration, Flyway trên database test, SMTP, browser E2E, concurrency, benchmark hoặc backup/restore. Tất cả các mục tương ứng tiếp tục là `Chưa chạy`.

## Kết luận

- Gate G1 và kiểm tra thủ công PRD-110 đã đạt, nhưng hệ thống chưa được tuyên bố production-ready vì các giai đoạn và follow-up bảo mật sau G1 còn mở.
- Phần kỹ thuật PRD-201 đến PRD-209 và local integration PRD-211 đã có bằng chứng. Bước tiếp theo của Giai đoạn 2 là PRD-210 do người dùng xác nhận, rồi commit/push/CI/PR branch PRD-209 trước khi bắt đầu Giai đoạn 3.
