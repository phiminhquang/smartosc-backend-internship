# Kiểm chứng: Khôi phục mật khẩu

## Trạng thái

Tính năng đã có implementation backend và frontend nhưng chưa hoàn tất kiểm thử tích hợp trên database/email test hoặc staging.

## Bằng chứng hiện có

Các kết quả dưới đây được chuyển từ `AI-HANDOFF.md`; chưa phải toàn bộ test suite end-to-end:

| Kiểm tra | Kết quả | Phạm vi |
|---|---|---|
| `bash ./mvnw -DskipTests compile` | Thành công | Backend compile |
| 28 unit/web-security test backend cô lập | 28 thành công, 0 failure/error | Password reset, authentication và security liên quan |
| 15 test mục tiêu chạy lại ngày 2026-09-29 | 15 thành công, 0 failure/error | Password reset service, authentication, JWT version và controller security |
| `bash scripts/verify.sh safe` ngày 2026-09-30 | Thành công | Tài liệu/diff, backend compile, 15 test mục tiêu, frontend lint/build |
| `git diff --check` | Thành công | Lỗi whitespace trong diff |
| `DeviceApplicationTests.contextLoads` | Chưa chạy | Có nguy cơ dùng datasource thật và áp dụng Flyway lên Aiven |
| `npm run lint` | Thành công | Frontend, oxlint |
| `npm run build` | Thành công | TypeScript và Vite production build |

## Ma trận cần kiểm tra

| ID | Tình huống | Kết quả mong đợi | Trạng thái |
|---|---|---|---|
| V-01 | Request với email tồn tại | HTTP 202, email reset được gửi | Chưa chạy E2E |
| V-02 | Request với email không tồn tại | HTTP 202 và thông điệp giống V-01 | Chưa chạy E2E |
| V-03 | Request lại trong cooldown | Không tạo token/email sử dụng được thứ hai | Có unit test; cần E2E |
| V-04 | Confirm bằng token hợp lệ | Đổi mật khẩu và đánh dấu token đã dùng | Có unit test; cần E2E |
| V-05 | Dùng lại token | Bị từ chối | Có unit test; cần E2E |
| V-06 | Token hết hạn | Bị từ chối | Có unit test; cần E2E |
| V-07 | Token không tồn tại | Bị từ chối với lỗi chung | Có unit test; cần E2E |
| V-08 | JWT phát trước reset | Resource server và introspect từ chối | Có test cô lập; cần E2E |
| V-09 | Mật khẩu cũ/mới | Cũ thất bại, mới đăng nhập thành công | Chưa chạy E2E |
| V-10 | Link email mở frontend | Token được điền và form submit thành công | Chưa chạy trình duyệt |
| V-11 | Gửi email thất bại | Token vừa tạo không dùng được, không lộ raw token | Có unit test; cần staging failure test |
| V-12 | Migration V2 trên schema V1 | Migration thành công, constraint/index đúng | Chưa chạy staging |
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
- Script không chạy `DeviceApplicationTests.contextLoads` và không kết nối database thật.

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
