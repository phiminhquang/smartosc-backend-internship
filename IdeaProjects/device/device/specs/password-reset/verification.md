# Kiểm chứng: Khôi phục mật khẩu

## Trạng thái

Tính năng đã có implementation backend/frontend; MySQL cô lập, Mailpit và Playwright Chromium browser E2E đã đạt local/GitHub. Password reset đã dùng transactional outbox có retry/AES-256-GCM, shared MySQL rate limit và response timing guard; diễn tập SMTP failure/recovery cùng follow-up PR-207/208/209 đều đã đạt.

## Bằng chứng hiện có

Đây là nguồn bằng chứng kiểm tra của feature. Các kết quả dưới đây được ghi từ những lần chạy thực tế được mô tả ở cuối file; chưa phải toàn bộ test suite end-to-end:

| Kiểm tra | Kết quả | Phạm vi |
|---|---|---|
| `bash ./mvnw -DskipTests compile` | Thành công | Backend compile |
| 28 unit/web-security test backend cô lập | 28 thành công, 0 failure/error | Password reset, authentication và security liên quan |
| 15 test mục tiêu chạy lại ngày 2026-09-29 | 15 thành công, 0 failure/error | Password reset service, authentication, JWT version và controller security |
| `bash scripts/verify.sh safe` ngày 2026-10-02 | Thành công | Tài liệu/diff, backend compile, 15 test mục tiêu, frontend lint/build |
| `git diff --check` | Thành công | Lỗi whitespace trong diff |
| Full backend suite mới nhất | 83 thành công, 0 failure/error/skipped | MySQL 8.4.11 Testcontainers; Flyway V1-V7, outbox/worker, shared rate limit và password reset integration |
| `bash scripts/verify.sh safe` mới nhất | Thành công | 28 backend test, benchmark guards, 3 frontend test, lint/build 92 module |
| `npm run lint` | Thành công | Frontend, oxlint |
| `npm run build` | Thành công | TypeScript và Vite production build |
| `bash scripts/verify.sh frontend-e2e` | Thành công | Playwright Chromium điều khiển DOM thật qua frontend/backend/MySQL/Mailpit |
| SMTP failure/recovery thủ công ngày 2026-10-11 | Thành công | Mailpit dừng: job `RETRY_WAIT`; bật lại: cùng job `SENT`, database một job và Mailpit đúng một email |
| GitHub Device CI | Thành công | `safe`, `integration`, `compose-smoke` đều xanh trên commit `5837be2` |

## Ma trận cần kiểm tra

| ID | Tình huống | Kết quả mong đợi | Trạng thái |
|---|---|---|---|
| V-01 | Request với email tồn tại | HTTP 202, email reset được gửi | Đạt trên Compose API + Mailpit |
| V-02 | Request với email không tồn tại | HTTP 202 và thông điệp giống V-01 | Đạt HTTP 202, không thêm email Mailpit |
| V-03 | Request lại trong cooldown | Không tạo token/email sử dụng được thứ hai | Có unit test; cần E2E |
| V-04 | Confirm bằng token hợp lệ | Đổi mật khẩu và đánh dấu token đã dùng | Đạt integration test và Compose API |
| V-05 | Dùng lại token | Bị từ chối | Đạt Compose API, HTTP 400 lần hai |
| V-06 | Token hết hạn | Bị từ chối | Có unit test; cần E2E |
| V-07 | Token không tồn tại | Bị từ chối với lỗi chung | Có unit test; cần E2E |
| V-08 | JWT phát trước reset | Resource server và introspect từ chối | Có test cô lập; cần E2E |
| V-09 | Mật khẩu cũ/mới | Cũ thất bại, mới đăng nhập thành công | Đạt bằng Playwright Chromium |
| V-10 | Link email mở frontend | Token được điền và form submit thành công | Đạt bằng Playwright + Mailpit |
| V-11 | Gửi email thất bại | Token vừa tạo không dùng được khi job terminal, không lộ raw token; lỗi tạm thời được retry | Đạt bằng test tự động `DEAD`/`EXPIRED` và diễn tập SMTP failure/recovery thật; log đã kiểm tra không lộ raw token/payload |
| V-12 | Migration V2/V4/V5/V7 trên schema mới | Migration thành công, constraint/index đúng | Đạt trên MySQL 8.4.11 tạm; Flyway V1-V7 được xác nhận |
| V-13 | Mở link reset có query token | Token được nạp vào form rồi biến mất khỏi URL; referrer không chứa token | Đạt bằng Playwright Chromium |
| V-14 | So sánh request email tồn tại/không tồn tại | Không có timing side-channel rõ ràng và cả hai chịu cùng rate limit | Đạt PR-209: 50 cặp, median lệch 5,210 ms, ratio 1,031, dominance 0,560 sau timing guard |

## Kết quả review tĩnh 2026-09-30

- Reset token có entropy cao và database chỉ lưu SHA-256 hash.
- Transaction và pessimistic locking bảo vệ việc phát/dùng token đồng thời.
- JWT cũ bị thu hồi bằng `tokenVersion` ở resource server và introspect.
- Đã thêm `no-referrer` và xóa query token khỏi URL sau khi frontend nạp token.
- Password reset không còn gọi SMTP trong request transaction: token hash và outbox job được ghi nguyên tử, worker giải mã raw token trong bộ nhớ rồi gửi ngoài transaction claim. Các producer email khác vẫn đồng bộ cho tới PRD-406.
- Shared rate limit chạy trước account lookup cho cả email tồn tại/không tồn tại. Timing guard mặc định 150 ms + jitter 0-25 ms đã đạt PR-209; cần đo lại khi latency database ở môi trường deploy vượt cửa sổ này.

## Môi trường kiểm thử tích hợp yêu cầu

- MySQL test/staging tách khỏi production/Aiven đang dùng thật.
- SMTP sandbox hoặc tài khoản email test.
- `PASSWORD_RESET_BASE_URL` trỏ tới frontend test.
- Không lưu credential của database/email vào repository.

## Cách ghi kết quả mới

Mỗi lần chạy, thêm một mục có:

```text
Ngày giờ:
Commit hoặc working tree:
Môi trường:
Lệnh/kịch bản:
Kết quả:
Lỗi còn lại:
```

Không thay `Chưa chạy` bằng `Thành công` nếu chưa có bằng chứng từ lần chạy thực tế.

## Lệnh chuẩn hóa

- Toàn bộ cổng an toàn hiện tại: `bash scripts/verify.sh safe`.
- Chỉ backend mục tiêu: `bash scripts/verify.sh password-reset`.
- `bash scripts/verify.sh integration` chạy full backend suite trên MySQL Testcontainers; script bỏ biến DB môi trường và dừng nếu Docker không dùng được.

## Lần chạy tích hợp local 2026-10-08

- Môi trường: Docker Engine 29.1.3, Compose 2.40.3, MySQL 8.4.11 Testcontainers/Compose, Mailpit 1.31.4; không dùng Aiven/production.
- `bash scripts/verify.sh integration`: thành công; 31 test, 0 failure/error/skipped. Flyway validate và áp dụng V1/V2 từ schema trống tới version 2.
- `DeviceApplicationTests`: 3 test thành công, gồm số migration và password reset trên MySQL thật tạm thời; email service được mock trong integration test để không gọi mạng ngoài.
- `docker compose up --build -d`: image backend build thành công. Lần khởi động đầu phát hiện `Public Key Retrieval is not allowed`; JDBC URL local được sửa bằng `allowPublicKeyRetrieval=true`, sau đó Flyway V1/V2 và backend khởi động thành công.
- Trạng thái sau sửa: backend, MySQL và Mailpit đều `healthy`; backend chạy non-root `10001:10001`; backend và Mailpit chỉ mở trên `127.0.0.1`.
- Smoke API: OpenAPI HTTP 200, Mailpit UI HTTP 200, admin login HTTP 200, request reset email tồn tại HTTP 202 và Mailpit nhận 1 email.
- Smoke reset token không in secret ra output: email không tồn tại HTTP 202 và không thêm email; confirm lần đầu HTTP 200; dùng lại cùng token HTTP 400; đăng nhập sau reset HTTP 200.
- `bash scripts/verify.sh safe` sau các thay đổi: thành công; backend compile, 15 test mục tiêu, frontend lint và Vite build 92 module đều đạt.
- Còn thiếu: trình duyệt E2E, mật khẩu cũ/mới khác nhau trên Compose, JWT cũ sau reset, cooldown/concurrency E2E, SMTP failure/recovery và các follow-up PR-207/208/209.

## Lần tiếp quản và kiểm tra lại 2026-10-08 13:55-13:58 +07

- Docker Engine client/server 29.1.3 và Compose 2.40.3 hoạt động; backend, MySQL và Mailpit đều `healthy`.
- Backend chạy non-root `10001:10001`; OpenAPI và Mailpit UI trên loopback đều trả HTTP 200.
- MySQL Compose xác nhận Flyway version 1 và 2 đều `success=1`; lệnh chỉ đọc, không đổi dữ liệu.
- `bash scripts/verify.sh integration`: exit 0; 31 test, 0 failure/error/skipped; MySQL 8.4.11 Testcontainers và V1/V2 đạt.
- `bash scripts/verify.sh safe`: exit 0; backend compile, 15 test mục tiêu, frontend lint/build với 92 module đạt; không sửa frontend source.
- Review bảo mật/migration: token sinh từ 32 byte ngẫu nhiên, database lưu SHA-256, thao tác phát/dùng có pessimistic lock, JWT/resource server kiểm tra `tokenVersion`, endpoint public chỉ mở đúng các API auth áp dụng và V2 giữ constraint/index đã đặc tả. Không thấy secret production hoặc raw token được ghi log.
- Giới hạn giữ nguyên: SMTP còn đồng bộ trong transaction/request path, chưa có rate limit dùng chung, chưa đo timing và chưa chạy browser E2E/JWT cũ/mật khẩu cũ trên Compose.

## Lần tích hợp frontend Compose 2026-10-08 16:19 +07

- Frontend Dockerfile/Nginx và cấu hình API theo môi trường đã build thành công; full Compose có backend, frontend, MySQL và Mailpit đều `healthy`.
- Ba file Node contract/security test đạt; frontend lint và build đạt với 92 module. Live Compose smoke qua Nginx proxy đạt và không bị skip.
- Frontend `/reset-password` trả HTTP 200 theo SPA fallback; Nginx trả `Referrer-Policy: no-referrer`, `X-Content-Type-Options: nosniff` và `X-Frame-Options: DENY`.
- Bằng chứng này xác nhận container/proxy/header và API smoke, nhưng **không** xác nhận PR-409/PR-410: test không chạy browser, không bấm link Mailpit, không quan sát address bar/referrer thực và không hoàn tất reset bằng token hợp lệ.
- Production dependencies không có vulnerability ở mức npm audit hiện tại; dev toolchain còn cảnh báo high ở `source-map-js`, cần frontend owner review thay đổi lockfile/dependency.

## Lần chạy Playwright/GitHub CI 2026-10-09

- Playwright Chromium điều khiển DOM thật qua full Compose: login thành công, forgot password, lấy link Mailpit, reset, xóa token khỏi URL, kiểm tra `Referer`, login mật khẩu mới và từ chối mật khẩu cũ.
- Local persistent DB có bước khôi phục mật khẩu; `CI=true` dùng volume tạm nên bỏ reset khôi phục dư thừa. Local CI-mode đạt `1 passed (6.4s)`.
- `source-map-js` là dependency bắc cầu `vite -> postcss`, đã lên 1.2.2; `npm audit --audit-level=high` báo 0 vulnerability.
- PR #1, commit `5837be2`: GitHub `safe`, `integration` và `compose-smoke` đều đạt ở cả push run lẫn PR run. Credential CI là giá trị ngẫu nhiên tạm và được mask trước khi ghi `GITHUB_ENV`.

## PRD-405 — Chuyển password reset sang outbox ngày 2026-10-11

- `requestReset` không còn gọi SMTP trong request transaction. Reset token hash và job `PASSWORD_RESET` được ghi nguyên tử; lỗi mã hóa/enqueue rollback cả hai.
- Raw token chỉ được lưu dưới AES-256-GCM ciphertext với nonce 12 byte, tag 128 bit, AAD job type và key version. Payload JSON không chứa token; worker giải mã trong bộ nhớ ngay trước khi render reset link và xóa secret khi terminal.
- Test MySQL xác nhận job quá hạn/token không hoạt động chuyển `EXPIRED` mà không gửi; `DEAD` vô hiệu hóa token; `UNKNOWN` giữ token dùng được vì SMTP outcome có thể mơ hồ.
- `DeviceApplicationTests` chạy thật chuỗi enqueue → decrypt/render → mock JavaMailSender → `SENT`, sau đó confirm token một lần; test rollback riêng xác nhận không để token/job mồ côi khi cipher lỗi.
- `bash scripts/verify.sh integration`: 64/64 test đạt, 0 failure/error/skipped; MySQL 8.4.11 Testcontainers và Flyway V1-V5 đạt trong 2 phút 42 giây.
- `bash scripts/verify.sh safe`: backend compile, 20/20 test mục tiêu, generator/benchmark guards, 3/3 frontend test, lint và build 92 module đều đạt.
- PR-208 được đóng bằng implementation và test tự động. PR-207 rate limit, PR-209 timing và PRD-409 tắt/bật SMTP sandbox vẫn mở; chưa dùng kết quả tự động để thay thế thao tác SMTP failure/recovery thật.

## PRD-407 / PR-207 — Shared rate limit ngày 2026-10-11

- Password-reset request áp dụng hai fixed-window bucket trong MySQL trước account lookup: `10/IP` và `3/normalized identity` trong 15 phút theo mặc định. Email tồn tại và không tồn tại đi qua cùng policy; raw email/IP không được lưu trong bảng vì bucket key là SHA-256.
- Counter increment dùng transaction mới và atomic upsert; MySQL integration test với 20 caller đồng thời quan sát đủ count 1..20. Flyway V7 và expiry index được xác nhận trên MySQL 8.4.11.
- Khi vượt ngưỡng, API trả HTTP 429/code `1057` cùng message chung và `Retry-After`; web test xác nhận password-reset service không được gọi. Login dùng cơ chế tương tự với ngưỡng cấu hình riêng.
- `bash scripts/verify.sh password-reset`: 20/20 test đạt. Full `bash scripts/verify.sh integration`: 80/80 test đạt trong 2 phút 54 giây, Flyway V1-V7. `bash scripts/verify.sh safe`: 25/25 backend test, 3/3 frontend test, lint/build 92 module đều đạt.
- PR-207 được đóng; PR-209 timing vẫn mở. PRD-409 được đóng bằng diễn tập SMTP sandbox thật ghi bên dưới.

## PRD-409 — SMTP failure/recovery thủ công ngày 2026-10-11

- Người dùng dừng Mailpit rồi gửi đúng một yêu cầu reset cho tài khoản test trong full Compose. Khi SMTP không khả dụng, job vẫn tồn tại ở `RETRY_WAIT`, đã thử hai lần và có mã lỗi redacted `EMAIL_SEND_FAILED`.
- Sau khi người dùng bật lại Mailpit, cùng job được retry lần thứ 3 và chuyển `SENT`; truy vấn tổng hợp xác nhận database chỉ có một password-reset job và một job `SENT`.
- Mailpit API và xác nhận trực quan của người dùng đều cho kết quả đúng một email. Không bấm link reset, không đưa recipient/token vào tài liệu và không thấy raw token/payload trong log backend đã kiểm tra.
- Bằng chứng này xác nhận failure tạm thời không làm mất job và recovery không tạo email trùng trong kịch bản đã chạy. Giới hạn SMTP send/ack mơ hồ và trạng thái `UNKNOWN` trong ADR-002 vẫn giữ nguyên; không tuyên bố exactly-once tuyệt đối.
- `bash scripts/verify.sh docs` đạt sau khi bổ sung runbook và kết quả kiểm chứng.

## PR-209 / PRD-410 — Timing comparison và tích hợp Giai đoạn 4 ngày 2026-10-11

- Thêm benchmark có guard `--confirm-isolated`, chạy controller → shared rate limit → service/outbox trên MySQL 8.4.11 Testcontainers `device_test`; mỗi lần dùng 10 cặp warm-up và 50 cặp email test riêng, thứ tự xen kẽ. Không đọc/ghi datasource Compose hoặc production.
- Baseline đầu tiên không đạt: existing median 86,052 ms, missing median 48,330 ms, lệch 37,722 ms và dominance 0,990 dù HTTP 202/body giống nhau. PR-209 chưa được đóng tại checkpoint này.
- Thêm `PasswordResetTimingGuard`: request hợp lệ chờ tới thời gian tối thiểu 150 ms cộng jitter mật mã 0-25 ms sau khi transaction hoàn tất. Response 429 vẫn xảy ra trước account lookup và không bị trì hoãn; duration cấu hình được nhưng fail-fast ngoài khoảng 0-5 giây.
- Cùng benchmark sau sửa đạt: existing median 175,697 ms, missing 170,486 ms, lệch 5,210 ms, tỷ lệ 1,031 và dominance 0,560; 100/100 response ghi nhận cùng HTTP status/body. Báo cáo trước/sau: `docs/benchmarks/password-reset-timing-comparison-2026-10-11.md`.
- `bash scripts/verify.sh password-reset`: 23/23 test đạt sau implementation. Full `bash scripts/verify.sh integration`: 83/83 test đạt, 0 failure/error/skip, `BUILD SUCCESS` trong 2 phút 53 giây trên MySQL Testcontainers/Flyway V1-V7.
- `bash scripts/verify.sh safe`: exit 0; compile, 28/28 backend test, generator và ba benchmark isolation guard, 3/3 frontend test, lint và Vite build 92 module đều đạt.
- Backend Compose được rebuild từ source mới; cả bốn service healthy. Smoke email test không tồn tại trả HTTP 202 ở 341 ms ngay sau restart và 187 ms khi đã ấm; phép smoke chỉ xác nhận runtime mới, không dùng thay benchmark existing/missing.
- PR-207/208/209 và PRD-410 được đóng. Giới hạn: kết quả loại bỏ chênh lệch rõ ràng trong môi trường local ít nhiễu, không chứng minh hai phân phối giống nhau về mặt mật mã; phải đo lại trên hạ tầng deploy nếu latency DB vượt 150 ms.

## Lần chạy 2026-09-29

### Frontend lint

- Lệnh: `npm run lint` trong `frontend/`.
- Kết quả: Thành công, không có lỗi oxlint.

### Frontend build

- Lệnh: `npm run build` trong `frontend/`.
- Kết quả: Thành công; TypeScript build và Vite build hoàn tất với 91 module.

### Backend test mục tiêu

- Phạm vi: `PasswordResetServiceImplTest`, `AuthenticationimplTest`, `JwtTokenVersionValidatorTest`, `AuthenticationControllerSecurityTest`.
- Kết quả cuối: 15 test thành công, 0 failure, 0 error, 0 skipped.
- Ghi chú môi trường: Mockito inline mock maker không thể tự attach Byte Buddy agent trong sandbox. Lần chạy thành công truyền Byte Buddy agent cho JVM fork của Surefire bằng `-DargLine=-javaagent:<byte-buddy-agent.jar>`. Đây là hạn chế của môi trường chạy, không phải failure assertion của test.

## Lần chạy 2026-09-30

### Cổng kiểm tra an toàn dùng chung

- Working tree: chưa commit, bao gồm implementation password reset, tài liệu quy trình và security fix frontend.
- Lệnh: `bash scripts/verify.sh safe` tại thư mục gốc project.
- Kết quả tài liệu/diff: thành công.
- Kết quả backend compile: thành công.
- Kết quả backend mục tiêu: 15 test thành công, 0 failure, 0 error, 0 skipped.
- Kết quả frontend: oxlint thành công; TypeScript/Vite production build thành công với 91 module.
- Script tự truyền Byte Buddy javaagent khi tìm thấy trong Maven cache và không chạy test cần datasource thật.
- Phần còn lại: E2E database/email/trình duyệt vẫn chưa chạy vì chưa có MySQL và SMTP test tách biệt.

### Frontend sau security review

- Thay đổi: đặt `Referrer-Policy` thành `no-referrer`, lấy token từ query một lần rồi thay URL và che token trong input.
- `npm run lint`: Thành công, không có lỗi oxlint.
- `npm run build`: Thành công; TypeScript và Vite production build hoàn tất với 91 module.
- Kiểm tra trình duyệt cho hành vi URL/referrer vẫn thuộc V-13 và chưa được đánh dấu hoàn thành.

## Lần chạy 2026-10-02

### Cổng kiểm tra sau khi chuẩn hóa tài liệu

- Working tree: chỉ thay đổi tài liệu bàn giao, trạng thái, adapter Gemini và README frontend.
- Lệnh: `bash scripts/verify.sh safe` tại thư mục gốc project.
- Kết quả tài liệu/diff: thành công.
- Kết quả backend compile: thành công.
- Kết quả backend mục tiêu: 15 test thành công, 0 failure, 0 error, 0 skipped.
- Kết quả frontend: Oxlint thành công; TypeScript/Vite production build thành công với 91 module.
- Phần E2E database/email/trình duyệt vẫn chưa chạy và giữ nguyên trạng thái.
