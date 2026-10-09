# Đặc tả: Hoàn thiện hệ thống để vận hành, chịu tải và phục hồi sự cố

## Trạng thái

- Giai đoạn: Approved (2026-10-08, người dùng duyệt trong cuộc trò chuyện).
- Mức: L.
- Người duyệt phạm vi: Người dùng/chủ project.
- Được triển khai theo từng giai đoạn và owner trong `tasks.md`; kết quả chỉ được đánh dấu hoàn thành khi có bằng chứng.

## Cách các AI tiếp quản công việc

Mỗi AI bắt đầu phiên liên quan đến kế hoạch này phải đọc theo thứ tự:

1. `PROJECT.md` và `AGENTS.md`.
2. `AI-HANDOFF.md`.
3. File này.
4. `plan.md`.
5. `tasks.md` để nhận đúng task và owner.
6. `verification.md` để biết bằng chứng thật và phần chưa kiểm tra.
7. Spec của feature phụ thuộc, trước mắt là `specs/password-reset/`.

Không dùng lịch sử chat làm nguồn trạng thái. Không đánh dấu hoàn thành nếu chưa có code hoặc bằng chứng trong repository.

## Hiện trạng đã xác nhận

- Backend là Spring Boot, Spring Security/JWT, JPA, MySQL và Flyway; frontend là React/TypeScript.
- Hệ thống đã có quản lý thiết bị, người dùng, vai trò, cấp phát, gia hạn, hoàn trả, sửa chữa, dashboard, email và import/export.
- API thiết bị đã hỗ trợ tìm kiếm và phân trang; nhiều API người dùng, cấp phát, sửa chữa và gia hạn vẫn trả danh sách đầy đủ.
- Export thiết bị đang đọc toàn bộ dữ liệu bằng `findAll()`.
- Scheduler và password reset còn gửi email đồng bộ; lỗi email ở một số luồng đang ghi bằng `System.out`.
- `Device` đã có trường `version` và một số thao tác đã khóa bản ghi, nhưng chưa có integration test MySQL thật cho các request đồng thời.
- Test hiện tại chủ yếu là unit/web-security test cô lập; chưa có MySQL test/staging an toàn để chạy full integration test và Flyway.
- Repository chưa có Docker/Compose, CI, metrics dashboard hoặc quy trình backup/restore được kiểm chứng.
- Password reset vẫn còn các task production và E2E mở trong `specs/password-reset/tasks.md`.

## Vấn đề

Project có đủ nghiệp vụ để trình diễn nhưng chưa chứng minh được rằng nó:

- chạy lặp lại trên máy mới mà không phụ thuộc cấu hình cá nhân;
- giữ đúng dữ liệu khi nhiều request cùng sửa một tài nguyên;
- phản hồi ổn khi dữ liệu tăng lớn;
- không giữ request/database transaction trong lúc chờ email;
- cung cấp đủ log và số liệu để tìm nguyên nhân khi chậm hoặc lỗi;
- có thể sao lưu, khôi phục và quay lui sau một lần phát hành lỗi.

## Mục tiêu

- Tạo môi trường local/test/demo cô lập, có thể khởi động lặp lại và không chạm database thật ngoài ý muốn.
- Hoàn tất kiểm chứng tích hợp password reset trước khi public demo.
- Phân trang các danh sách có thể tăng lớn và xử lý import/export theo từng nhóm dữ liệu.
- Có dữ liệu thử và phép đo trước/sau cho các truy vấn quan trọng; chỉ thêm index dựa trên bằng chứng.
- Chứng minh các luồng cấp phát, hoàn trả, gia hạn và sửa chữa giữ đúng dữ liệu khi có request đồng thời.
- Chuyển email sang công việc nền có trạng thái, thử lại và chống gửi trùng.
- Có log theo request, health check và dashboard theo dõi API, database, scheduler và email nền.
- Có quy trình deploy demo, backup, restore và rollback đã được thực hành trên môi trường không phải production.
- Tạo bằng chứng kỹ thuật trung thực để dùng trong README, CV và phỏng vấn.

## Ngoài phạm vi

- Không thêm chatbot, AI/OCR, QR hoặc nghiệp vụ mới không cần cho production readiness.
- Không tách microservice, không thêm Kafka và không dùng Kubernetes trong kế hoạch cơ sở này.
- Không đổi MySQL sang database khác chỉ để bổ sung từ khóa CV.
- Không tối ưu dựa trên phỏng đoán hoặc ghi số chịu tải chưa được đo.
- Không tự tạo tài khoản cloud, phát sinh chi phí, nhập secret hoặc chạy migration/restore trên database thật thay người dùng.
- Không tự sửa frontend bằng Codex hoặc backend bằng Antigravity nếu chưa có ngoại lệ được người dùng ghi rõ.

## Vai trò và quyền quyết định

### Codex/GPT

- Sở hữu backend, database migration, backend test, cấu hình backend, Compose/root infrastructure và tài liệu chung.
- Chốt API contract trong spec trước khi giao task frontend phụ thuộc.
- Tích hợp kết quả, chạy cổng kiểm tra và cập nhật verification/handoff.

### Antigravity/Gemini

- Sở hữu toàn bộ `frontend/`.
- Chỉ triển khai theo API contract đã chốt; không tự đoán endpoint hoặc response.
- Báo blocker và kết quả để Codex tích hợp vào tài liệu chung.

### Người dùng/chủ project

- Duyệt phạm vi, API thay đổi và điểm chuyển giai đoạn.
- Chọn nhà cung cấp deploy, tạo tài khoản, duyệt chi phí và nhập secret ngoài Git.
- Thực hiện kiểm tra trình duyệt/email, backup/restore và diễn tập sự cố với hướng dẫn của Codex.
- Duyệt bằng chứng trước khi cho phép ghi kết quả vào CV.

## Yêu cầu chức năng

### FR-1: Môi trường cô lập và lặp lại được

- Given một máy có Docker và source code,
- When chạy lệnh khởi động đã tài liệu hóa,
- Then frontend, backend, MySQL và hộp thư test phải chạy mà không cần dùng credential production.

### FR-2: Kiểm thử database thật nhưng an toàn

- Given integration test hoặc migration test,
- When test chạy,
- Then MySQL tạm/cô lập phải được tạo và hủy độc lập với Aiven hoặc production.

### FR-3: Danh sách và file lớn

- Given dữ liệu người dùng/cấp phát/sửa chữa/gia hạn tăng lớn,
- When client truy vấn danh sách,
- Then API chỉ trả một trang có giới hạn và bộ lọc/sắp xếp được xác định trong contract.
- Given export/import lớn,
- When xử lý file,
- Then backend không được bắt buộc giữ toàn bộ bảng hoặc toàn bộ file trong bộ nhớ.

## Hợp đồng API phân trang Giai đoạn 2 (PRD-201)

Trạng thái: **Approved — người dùng duyệt PRD-202 ngày 2026-10-09 sau khi xác nhận đây là phần mở rộng phân trang từ devices sang users/assignments/repairs/extension requests và không tạo màn hình frontend mới**.

### Quy ước chung

- Các endpoint danh sách trong phạm vi trả `ApiResponse<PageResult<T>>`, không trả mảng trực tiếp.
- Request dùng `page` zero-based, mặc định `0`; `size` mặc định `20`, hợp lệ từ `1` đến `100`.
- `sort` có dạng `field,direction`, ví dụ `sort=assignedAt,desc`; chỉ chấp nhận một field trong allow-list của endpoint và `asc` hoặc `desc`.
- Backend luôn thêm `id` làm khóa sắp xếp phụ để kết quả ổn định khi field chính trùng nhau. Khóa phụ không cần truyền từ client.
- Chuỗi tìm kiếm được trim, bỏ qua hoa/thường và dài tối đa 100 ký tự. Enum dùng đúng giá trị đã công bố; UUID phải hợp lệ.
- `page < 0`, `size < 1`, `size > 100`, sort/filter/UUID không hợp lệ trả HTTP `400`, code ứng dụng `1055` với thông điệp trung lập `Tham số phân trang, lọc hoặc sắp xếp không hợp lệ`.
- Response chỉ cam kết các field ổn định dưới đây; không lộ metadata nội bộ của Spring `Pageable`/`Sort`:

```json
{
  "code": 1000,
  "result": {
    "content": [],
    "number": 0,
    "size": 20,
    "totalElements": 0,
    "totalPages": 0,
    "first": true,
    "last": true,
    "empty": true
  }
}
```

### Endpoint và allow-list

| Endpoint | Filter | Sort cho phép | Sort mặc định |
|---|---|---|---|
| `GET /api/users` | `keyword` trên `name/email`; `role=ADMIN\|IT_STAFF\|EMPLOYEE` | `name`, `email` | `name,asc` + `id,asc` |
| `GET /api/assignments` | `status`, `userId`, `deviceId` | `assignedAt`, `expectedReturnAt`, `returnedAt`, `status` | `assignedAt,desc` + `id,desc` |
| `GET /api/assignments/me` | Không thêm filter; user lấy từ JWT | như assignments | `assignedAt,desc` + `id,desc` |
| `GET /api/assignments/user/{userId}` | `userId` từ path | như assignments | `assignedAt,desc` + `id,desc` |
| `GET /api/repairs` | `status`, `deviceId` | `createdAt`, `startedAt`, `finishedAt`, `status`, `cost` | `createdAt,desc` + `id,desc` |
| `GET /api/repairs/device/{deviceId}` | `deviceId` từ path | như repairs | `createdAt,desc` + `id,desc` |
| `GET /api/extension-requests/me` | `status` tùy chọn; user lấy từ JWT | `requestedAt`, `requestedReturnAt`, `reviewedAt`, `status` | `requestedAt,desc` + `id,desc` |
| `GET /api/extension-requests/pending` | Luôn `status=PENDING` | `requestedAt`, `requestedReturnAt` | `requestedAt,asc` + `id,asc` |

`GET /api/devices` giữ filter/response hiện tại nhưng phải dùng validation chung cho `page/size` và giới hạn `size=100`; frontend hiện truyền `size=10` nên không đổi hành vi màn hình hiện tại.

### Tương thích và phạm vi frontend

- Đây là breaking change có chủ ý cho tám endpoint hiện trả `List<T>`; endpoint chi tiết và endpoint ghi dữ liệu không đổi.
- Frontend hiện chỉ gọi API thiết bị, chưa có service/page cho users, assignments, repairs hoặc extension requests; vì vậy branch backend không sửa `frontend/`.
- Phạm vi PRD-202 đã duyệt: không tạo màn hình quản trị mới trong Giai đoạn 2. Khi có màn hình sản phẩm được duyệt, Antigravity dùng shape trang ổn định ở trên.

### Nguyên tắc truy vấn và bằng chứng

- Mọi query phải chọn thứ tự xác định với `id` làm tie-breaker; không dựa vào thứ tự tự nhiên của database.
- Dùng `EXPLAIN ANALYZE`, dataset và phép đo lặp lại trước khi thêm index. Không thêm index chỉ vì một cột xuất hiện trong filter hoặc có selectivity thấp.
- Offset pagination là contract đã duyệt. Deep page có thể chậm; nếu benchmark chứng minh bottleneck, cân nhắc truy vấn hai bước lấy page ID trên covering index rồi join/fetch DTO chi tiết. Không áp dụng mẹo này trước PRD-207/208/209.
- Với quan hệ `User.roles` dạng to-many, không page trực tiếp trên collection fetch join; dùng page ID/two-step fetch hoặc chiến lược tương đương đã có integration test để tránh in-memory pagination và sai `totalElements`.
- Video tham khảo do người dùng cung cấp: [Tối ưu phân trang MySQL trên bảng lớn](https://www.youtube.com/watch?v=tjT4O5HGIEU&t=870s). Con số trong video là ví dụ bên ngoài, không phải benchmark của Device.

## Hợp đồng import/export thiết bị Giai đoạn 2 (PRD-206/207)

Trạng thái: **Approved by implementation scope — giữ nguyên endpoint, quyền truy cập và payload HTTP hiện có; chỉ thay cách xử lý tài nguyên ở backend.**

### Export

- `GET /api/devices/export/csv` tiếp tục trả `text/csv;charset=UTF-8`, tên file `devices.csv`, UTF-8 BOM và các cột hiện có.
- `GET /api/devices/export/excel` tiếp tục trả XLSX với tên file `devices.xlsx`, sheet `Devices` và các cột hiện có.
- Cả hai endpoint ghi trực tiếp vào response stream và đọc database theo batch có thứ tự `id,asc`; không gọi `findAll()` và không tạo toàn bộ file trong một `byte[]`.
- CSV giữ bộ nhớ theo batch. XLSX dùng streaming workbook với cửa sổ row hữu hạn và file tạm do Apache POI quản lý; file tạm phải được dọn khi kết thúc hoặc lỗi.
- Batch export mặc định là `500` bản ghi; cửa sổ XLSX mặc định là `100` row. Đây là giới hạn cấu hình và sẽ được đánh giá lại bằng PRD-208, không phải tuyên bố hiệu năng production.

### Import CSV

- `POST /api/devices/import/csv` giữ response `ApiResponse<Integer>`; `result` là số thiết bị đã tạo.
- File phải có phần mở rộng `.csv`, không rỗng, tối đa `10 MiB`, và có đủ header `category`, `name`, `model`; `description` là tùy chọn. Header không hợp lệ trả code `1051`.
- File vượt giới hạn trả HTTP `400`, code `1056`. Giới hạn multipart và kiểm tra ở service phải thống nhất để request bị chặn trước hoặc trong xử lý với cùng error contract.
- Parser đọc tuần tự; backend flush/clear persistence context mỗi `100` row. Import giữ tính nguyên tử hiện tại: một row không hợp lệ làm rollback toàn bộ transaction và không trả partial success.
- Validation category/name/model và việc sinh serial tiếp tục dùng nghiệp vụ hiện có; không nhận ID, serial, state hoặc audit field từ file import.

### Data generator an toàn

- Generator PRD-207 chỉ tạo CSV tổng hợp trên filesystem từ số row và đường dẫn output được chỉ định; không đọc `.env`, không mở kết nối mạng và không truy cập database.
- Generator chỉ cho phép từ `1` đến `1.000.000` row, dùng dữ liệu xác định để chạy lặp lại và từ chối ghi đè file trừ khi người chạy truyền `--force`.
- Việc import dataset vào local/Testcontainers là bước riêng có chủ ý. Generator không được tự suy ra hoặc nhận URL production.

### FR-4: Tính đúng đắn khi request đồng thời

- Given hai request cùng thao tác lên một thiết bị, assignment, extension hoặc repair,
- When chúng chạy đồng thời,
- Then chỉ kết quả hợp lệ được commit và dữ liệu không có trạng thái mâu thuẫn.

### FR-5: Email nền đáng tin cậy

- Given email service chậm hoặc tạm ngừng,
- When nghiệp vụ tạo yêu cầu email,
- Then nghiệp vụ chính phải tuân theo contract đã chốt, công việc email được lưu bền vững, có thử lại và không gửi trùng.

### FR-6: Có thể tìm nguyên nhân lỗi

- Given một request hoặc job lỗi,
- When người vận hành có request/job ID,
- Then họ phải tìm được log, trạng thái và số liệu liên quan mà không thấy secret/token/mật khẩu.

### FR-7: Khôi phục được

- Given database demo/test bị mất hoặc bản phát hành mới lỗi,
- When áp dụng runbook,
- Then người dùng có thể restore dữ liệu hoặc quay lại phiên bản ứng dụng trước và kiểm tra tính toàn vẹn.

## Yêu cầu phi chức năng

- Bảo mật: secret chỉ đi qua biến môi trường hoặc secret store; log không chứa mật khẩu, JWT hoặc reset token dạng rõ.
- Hiệu năng: phải có máy/môi trường đo, tập dữ liệu và kết quả trước/sau; chưa chốt con số marketing khi chưa có baseline.
- Tương thích: mọi thay đổi API phá vỡ client phải được ghi và phối hợp với Antigravity trước khi merge.
- Database: migration mới chỉ được thêm tiếp; không sửa migration đã áp dụng. Index chỉ thêm sau khi xem kế hoạch chạy truy vấn và đo thực tế.
- Quan sát: lỗi server có mã liên kết với log; endpoint quản trị/metrics không được public tùy tiện.
- Khôi phục: backup không được coi là hoàn thành nếu chưa restore thử trên môi trường cô lập.

## Tiêu chí chấp nhận tổng thể

- [ ] Project khởi động được bằng quy trình đã tài liệu hóa với database và email test cô lập.
- [ ] Full integration test không thể kết nối database production/Aiven ngoài ý muốn.
- [ ] Password reset đạt ma trận E2E áp dụng và các follow-up production PR-207/208/209 hoàn thành.
- [ ] Mọi API danh sách có thể tăng lớn đều phân trang hoặc có lý do được duyệt để không phân trang.
- [ ] Import/export lớn được xử lý theo nhóm hoặc công việc nền và có test giới hạn tài nguyên.
- [ ] Có test MySQL thật cho các thao tác đồng thời quan trọng.
- [ ] SMTP lỗi không làm mất công việc email; retry và chống gửi trùng có bằng chứng.
- [ ] Có request ID, log chuẩn, health check và dashboard tối thiểu.
- [ ] Có báo cáo đo dữ liệu lớn trước/sau, không dùng số liệu tự suy đoán.
- [ ] Backup/restore và rollback được thực hành trên demo/test.
- [ ] Backend test, frontend lint/build, migration test, E2E áp dụng và `git diff --check` đều có bằng chứng.
- [ ] README/diagram/runbook/handoff phản ánh đúng implementation cuối.

## Giả định

- Giai đoạn đầu dùng Docker local với MySQL và Mailpit để gỡ blocker hiện tại mà không cần tài khoản trả phí.
- Kiến trúc tiếp tục là một ứng dụng Spring Boot; email worker có thể chạy trong cùng ứng dụng ở bước đầu nhưng công việc phải được lưu bền vững.
- Quy mô dữ liệu test khởi đầu sẽ được tăng dần theo khả năng máy; kết quả luôn ghi kèm cấu hình môi trường.

## Điểm cần người dùng xác nhận trước khi Approved

- Đã duyệt phạm vi và thứ tự giai đoạn trong `plan.md` ngày 2026-10-08.
- Đã duyệt contract phân trang mặc định: `page=0`, `size=20`, giới hạn `size=100`. Chi tiết request/response từng API được chốt trong giai đoạn 2 trước khi triển khai.
- Khi tới giai đoạn deploy, chọn nhà cung cấp và ngân sách; quyết định này chưa chặn các giai đoạn local/test.
