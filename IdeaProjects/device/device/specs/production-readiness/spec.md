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
- Baseline ban đầu có scheduler/password reset gửi email đồng bộ và ghi lỗi bằng `System.out`; sau PRD-406/501, production Java không còn console output trực tiếp và các sự kiện API/job/scheduler dùng log `event` + trường cho phép.
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

`GET /api/devices` giữ filter/response hiện tại nhưng phải dùng validation chung cho `page/size` và giới hạn `size=100`; frontend hiện truyền `size=10` nên không đổi hành vi màn hình hiện tại. Trang không có keyword sắp xếp `name,asc` + `id,asc`; trang có keyword giữ thứ tự tên không phân biệt hoa/thường bằng `LOWER(name),asc` + `id,asc` để không ép leading-wildcard scan đi theo B-tree `name`.

### Tương thích và phạm vi frontend

- Đây là breaking change có chủ ý cho tám endpoint hiện trả `List<T>`; endpoint chi tiết và endpoint ghi dữ liệu không đổi.
- Frontend hiện chỉ gọi API thiết bị, chưa có service/page cho users, assignments, repairs hoặc extension requests; vì vậy branch backend không sửa `frontend/`.
- Phạm vi PRD-202 đã duyệt: không tạo màn hình quản trị mới trong Giai đoạn 2. Khi có màn hình sản phẩm được duyệt, Antigravity dùng shape trang ổn định ở trên.

### Nguyên tắc truy vấn và bằng chứng

- Mọi query phải chọn thứ tự xác định với `id` làm tie-breaker; không dựa vào thứ tự tự nhiên của database.
- Dùng `EXPLAIN ANALYZE`, dataset và phép đo lặp lại trước khi thêm index. Không thêm index chỉ vì một cột xuất hiện trong filter hoặc có selectivity thấp.
- Offset pagination là contract đã duyệt. Deep page có thể chậm; nếu benchmark chứng minh bottleneck, cân nhắc truy vấn hai bước lấy page ID trên covering index rồi join/fetch DTO chi tiết. Không áp dụng mẹo này trước PRD-207/208/209.
- Kết quả PRD-209 trên 100k chỉ chứng minh lợi ích rõ cho trang đầu, nên migration tối thiểu là `devices(name)`. Deep offset vẫn scan/sort và chưa đủ bằng chứng để thêm deferred join; keyword leading-wildcard vẫn là table scan đã biết.
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

### Baseline dữ liệu lớn PRD-208

- Benchmark chỉ chạy thủ công khi có `--confirm-isolated`; datasource cấu hình phải là `jdbc:tc:mysql` và database thực tế phải là `device_test`.
- Dataset mặc định là `1.000`, `10.000`, `100.000` row từ generator PRD-207. Report ghi commit, CPU/RAM, Java/MySQL, JVM heap, warm-up, mọi lần đo và cách lấy peak heap/RSS.
- Read/export chạy một warm-up và ba lần ghi nhận cho mỗi dataset. Import có một warm-up hạ tầng bằng dataset nhỏ nhất và mặc định một lần ghi nhận mỗi dataset vì đây là phép đo phá dữ liệu, chi phí cao; giới hạn này phải hiện trong report.
- Phép đo gồm import CSV, page đầu, deep page, keyword ở cuối dataset, CSV export và XLSX export. Report lưu raw latency, heap/RSS baseline/peak/delta, số row hoặc byte output.
- Cùng report phải lưu `EXPLAIN ANALYZE` trước index cho deep-page data/count và keyword data/count. PRD-208 chỉ thu baseline; PRD-209 mới review plan và quyết định index.
- Report sinh tự động nằm dưới `target/benchmarks/` và không được ghi đè nếu thiếu `--force`. Dataset tạm bị xóa khi kết thúc; không đọc `.env` và không kết nối database ngoài.

### FR-4: Tính đúng đắn khi request đồng thời

- Given hai request cùng thao tác lên một thiết bị, assignment, extension hoặc repair,
- When chúng chạy đồng thời,
- Then chỉ kết quả hợp lệ được commit và dữ liệu không có trạng thái mâu thuẫn.
- Request thắng trả HTTP `200` theo response hiện tại. Request thua vì trạng thái đã
  được request kia chuyển trước trả HTTP `409` và giữ mã nghiệp vụ cụ thể:
  - assign cùng thiết bị: code `1022` (`DEVICE_NOT_AVAILABLE`);
  - return cùng assignment: code `1029` (`DEVICE_ALREADY_RETURNED`);
  - approve/reject cùng extension: code `1035` (`EXTENSION_REQUEST_ALREADY_REVIEWED`);
  - complete/unrepairable cùng repair: code `1046` (`REPAIR_CANNOT_FINISH`).
- Optimistic-lock conflict ngoài bốn trạng thái trên tiếp tục trả HTTP `409`, code
  `1053` (`DEVICE_CONCURRENTLY_MODIFIED`). Response không lộ exception hoặc chi tiết
  database.

### FR-5: Email nền đáng tin cậy

- Given email service chậm hoặc tạm ngừng,
- When nghiệp vụ tạo yêu cầu email,
- Then nghiệp vụ chính phải tuân theo contract đã chốt, công việc email được lưu bền vững, có thử lại và không gửi trùng.

### FR-6: Rate limit dùng chung cho login và password reset

- `POST /api/auth/token` và `POST /api/auth/password-reset/request` áp dụng đồng thời
  bucket theo IP client và email đã chuẩn hóa trước khi gọi service nghiệp vụ.
- Bucket là fixed window lưu trong MySQL để mọi instance backend dùng chung. Mặc định:
  login `20/IP` và `5/identity` trong `5 phút`; password reset `10/IP` và
  `3/identity` trong `15 phút`. Mọi ngưỡng/window đều cấu hình được.
- Database chỉ lưu SHA-256 của action, scope, discriminator và thời điểm đầu window;
  không lưu email hoặc IP dạng rõ trong bảng rate limit.
- Email tồn tại và không tồn tại đi qua cùng policy/counter. Khi vượt bất kỳ bucket
  nào, API trả HTTP `429`, code `1057` và header `Retry-After`; password reset không
  thực hiện account lookup hoặc tạo token/job ở request bị chặn.
- Password-reset request hợp lệ dùng response window mặc định tối thiểu `150 ms`
  cộng jitter ngẫu nhiên `0-25 ms` để che chênh lệch lookup/outbox đã đo. Hai giá trị
  cấu hình được, fail-fast ngoài khoảng 0-5 giây; response `429` không bị trì hoãn.
- Backend dùng địa chỉ remote đã được servlet container xử lý. Khi chạy sau reverse
  proxy, chỉ tin forwarded headers từ proxy nội bộ và không public backend trực tiếp.

### FR-7: Có thể tìm nguyên nhân lỗi

- Given một request hoặc job lỗi,
- When người vận hành có request/job ID,
- Then họ phải tìm được log, trạng thái và số liệu liên quan mà không thấy secret/token/mật khẩu.
- Backend nhận hoặc sinh `X-Request-ID`. Giá trị client gửi chỉ được giữ khi khớp
  `[A-Za-z0-9][A-Za-z0-9._-]{0,63}`; nếu thiếu hoặc không hợp lệ thì backend sinh UUID.
- Mọi HTTP response trả lại `X-Request-ID`. Error body `ApiResponse` có trường
  `requestId`; success body giữ tương thích và dùng response header làm nguồn ID.
- Log hoàn tất request và log lỗi có `request_id`. Email job tạo trong request lưu
  `request_id` để worker log đồng thời `request_id` và `job_id`; job do scheduler tạo
  ngoài HTTP có `request_id = NULL` và được truy vết bằng `job_id`.
- Request ID chỉ dùng cho correlation/support, không dùng để xác thực, phân quyền hoặc
  làm idempotency key; log không ghi query string, request body hoặc secret.
- `GET /actuator/health`, `/actuator/health/liveness` và
  `/actuator/health/readiness` là các probe công khai tối thiểu, chỉ trả trạng thái và
  không lộ component/detail. Readiness phụ thuộc application state và database; SMTP
  không chặn readiness vì email đã được lưu bền vững để worker retry.
- Chỉ `health` được expose qua HTTP ở bước PRD-503. Actuator index và mọi endpoint
  quản trị khác không được truy cập ẩn danh; metrics chỉ được mở theo contract bảo vệ
  của PRD-504/505.
- Metrics dùng prefix/tag có cardinality hữu hạn: HTTP request theo route template,
  method/status/outcome; datasource/JVM theo binder chuẩn; scheduler theo tên task và
  success/failure; email job theo enum status/type và outcome cố định. Không dùng
  request ID, job ID, email, exception message hoặc dữ liệu người dùng làm metric tag.
- Các metric vận hành tối thiểu gồm `http.server.requests`, connection pool JDBC/Hikari,
  JVM, `device.scheduler.duration`, `device.email.jobs`, batch size, delivery outcome,
  lease recovery và retention cleanup.
- Profile local `monitoring` đặt Actuator trên cổng quản trị `8081` chỉ nằm trong mạng
  Compose. Prometheus scrape `backend:8081/actuator/prometheus`; cổng API `8080` không
  phục vụ metrics cho request ẩn danh. Prometheus và Grafana chỉ bind loopback; Grafana
  anonymous Viewer là cấu hình local, không phải contract cho Internet/demo.
- Dashboard cơ sở phải hiển thị scrape health, HTTP p95/error, JDBC pool, durable email
  jobs/delivery outcome, scheduler duration và JVM heap; panel chưa có sự kiện được phép
  hiện `No data` thay vì tạo dữ liệu giả.

### FR-8: Khôi phục được

- Given database demo/test bị mất hoặc bản phát hành mới lỗi,
- When áp dụng runbook,
- Then người dùng có thể restore dữ liệu hoặc quay lại phiên bản ứng dụng trước và kiểm tra tính toàn vẹn.

## Contract deploy demo CV — PRD-601/602/603

- Người dùng chọn demo portfolio ngân sách 0đ và cho phép tiếp tục cấu hình ngày
  2026-10-12. Phương án hiện tại là backend Render Free Web Service, MySQL Aiven
  Free, frontend Render Static Site và Resend Free qua HTTPS API. Koyeb bị loại vì
  người dùng mới chỉ được dùng gói trả phí; Render Free chặn SMTP 25/465/587.
  Aiven `device_demo`, Render account và Resend onboarding email đã có; Render
  services/migration cloud chưa chạy nên PRD-601 chưa hoàn tất.
- Backend dùng riêng profile `demo`, health/readiness trên cổng API 8080, chỉ expose
  health. Không bật profile `monitoring` trên Internet. Docker image demo giới hạn
  heap 256 MiB, email batch/thread 2, Hikari tối đa 5 connections và Tomcat 16 threads.
- `DB_URL` chỉ chứa JDBC host/port/database; credentials qua secret env. MySQL dùng
  `VERIFY_IDENTITY`, CA từ `DB_CA_CERT_BASE64` và truststore sinh lúc container khởi động;
  startup thất bại khi thiếu CA. Migration chỉ được chạy trên database demo trống sau
  khi người dùng duyệt PRD-606, không dùng service Aiven đang chứa dữ liệu thật.
- Frontend gọi `https://<backend-host>/api` qua `VITE_API_BASE_URL`. Backend chỉ cho
  origin HTTPS chính xác trong `FRONTEND_ORIGIN`, không dùng wildcard/credentials.
  CORS chỉ áp dụng `/api/**`, cho Authorization/Content-Type/X-Request-ID, expose
  X-Request-ID/Retry-After; preflight không cần JWT nhưng API vẫn giữ quyền hiện có.
  JSON/error contract không đổi. Antigravity cấu hình SPA reload và build frontend.
- Giới hạn công khai: backend Free ngủ khi idle nên scheduler/worker dừng; reminder
  trước hạn có thể lỡ, daily cron không tự chạy bù, job còn hợp lệ xử lý khi thức.
  Email reset quá hạn bị EXPIRED, delivery có lease mơ hồ giữ UNKNOWN; không cam kết
  mọi job sẽ gửi sau khi thức. UI hiện chưa có màn hình assignment/return/repair/extension;
  các luồng đó trình diễn bằng API/test, không coi backend endpoint là tính năng UI.
- Profile demo dùng Resend API với `Idempotency-Key` ổn định theo email job ID;
  local/dev/test tiếp tục SMTP. Khi chưa có custom domain, recipient bị giới hạn bằng
  allow-list tới email chủ tài khoản đã thử thành công. Không tuyên bố public demo gửi
  email tùy ý hoặc exactly-once tuyệt đối; key chỉ đi qua secret store.
- Public demo chỉ dùng dữ liệu tổng hợp nhỏ. Benchmark 100k và dashboard đầy đủ giữ
  bằng chứng local. PRD-602 kiểm chứng image trên MySQL TLS cô lập, không thay thế
  PRD-606/608 kiểm tra thật Render/Aiven/Resend và rate limit sau proxy.

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
- [x] Có request ID, log chuẩn, health check và dashboard tối thiểu; PRD-508/509 đã
  xác nhận correlation từ UI tới log, ảnh dashboard và network/security boundary local.
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
