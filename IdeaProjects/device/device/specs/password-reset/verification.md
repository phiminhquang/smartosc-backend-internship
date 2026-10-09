# Kiểm chứng: Khôi phục mật khẩu

## Trạng thái

Tính năng đã có implementation backend/frontend; backend đã được kiểm chứng trên MySQL cô lập và Mailpit local. Browser E2E, rate limit, email nền và failure/recovery vẫn chưa hoàn tất.

## Bằng chứng hiện có

Đây là nguồn bằng chứng kiểm tra của feature. Các kết quả dưới đây được ghi từ những lần chạy thực tế được mô tả ở cuối file; chưa phải toàn bộ test suite end-to-end:

| Kiểm tra | Kết quả | Phạm vi |
|---|---|---|
| `bash ./mvnw -DskipTests compile` | Thành công | Backend compile |
| 28 unit/web-security test backend cô lập | 28 thành công, 0 failure/error | Password reset, authentication và security liên quan |
| 15 test mục tiêu chạy lại ngày 2026-09-29 | 15 thành công, 0 failure/error | Password reset service, authentication, JWT version và controller security |
| `bash scripts/verify.sh safe` ngày 2026-10-02 | Thành công | Tài liệu/diff, backend compile, 15 test mục tiêu, frontend lint/build |
| `git diff --check` | Thành công | Lỗi whitespace trong diff |
| Full backend suite với `DeviceApplicationTests` | 31 thành công, 0 failure/error/skipped | MySQL 8.4.11 Testcontainers; Flyway V1/V2 và password reset integration |
| `npm run lint` | Thành công | Frontend, oxlint |
| `npm run build` | Thành công | TypeScript và Vite production build |

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
| V-09 | Mật khẩu cũ/mới | Cũ thất bại, mới đăng nhập thành công | Chưa chạy E2E |
| V-10 | Link email mở frontend | Token được điền và form submit thành công | Chưa chạy trình duyệt |
| V-11 | Gửi email thất bại | Token vừa tạo không dùng được, không lộ raw token | Có unit test; cần staging failure test |
| V-12 | Migration V2 trên schema V1 | Migration thành công, constraint/index đúng | Đạt trên MySQL 8.4.11 tạm; 2 migration được xác nhận |
| V-13 | Mở link reset có query token | Token được nạp vào form rồi biến mất khỏi URL; referrer không chứa token | Chưa chạy trình duyệt |
| V-14 | So sánh request email tồn tại/không tồn tại | Không có timing side-channel rõ ràng và cả hai chịu cùng rate limit | Chưa triển khai follow-up production |

## Kết quả review tĩnh 2026-09-30

- Reset token có entropy cao và database chỉ lưu SHA-256 hash.
- Transaction và pessimistic locking bảo vệ việc phát/dùng token đồng thời.
- JWT cũ bị thu hồi bằng `tokenVersion` ở resource server và introspect.
- Đã thêm `no-referrer` và xóa query token khỏi URL sau khi frontend nạp token.
- Còn rủi ro trước production: SMTP đang chạy đồng bộ bên trong request transaction, tạo thời gian phản hồi khác đáng kể cho email tồn tại và giữ database lock trong lúc gọi mạng.
- Còn rủi ro trước production: cooldown theo user không thay thế rate limit theo IP/identity và không bảo vệ nhánh email không tồn tại khỏi abuse.

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
