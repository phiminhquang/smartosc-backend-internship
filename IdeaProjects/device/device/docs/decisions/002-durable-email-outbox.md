# ADR-002: Transactional outbox cho email nền

- Trạng thái: Accepted ngày 2026-10-10 qua PRD-402.
- Ngày: 2026-10-10.
- Phạm vi: Password reset, assignment reminder/overdue report và extension notification.
- Supersedes: Không có; mở rộng ADR-001.
- Superseded by: Không có.

## Bối cảnh

Backend hiện gọi SMTP trực tiếp tại sáu điểm trong `PasswordResetServiceImpl`,
`AssignmentServiceImpl` và `AssignmentExtensionServiceImpl`. Password reset,
reminder, overdue và extension notification có thể gọi mạng khi transaction đang
mở; daily report giữ scheduler chờ SMTP. Khi SMTP chậm hoặc dừng, request giữ lock
lâu, thời gian phản hồi password reset khác biệt rõ giữa email tồn tại/không tồn
tại, và email lỗi chỉ được bỏ qua hoặc token bị vô hiệu hóa mà không có retry bền
vững.

Hệ thống hiện đã dùng MySQL và chưa có message broker. Giải pháp cần triển khai
tăng dần, không làm mất công việc email khi process restart, không lưu secret hoặc
raw reset token có thể sử dụng trực tiếp, và phải nói rõ giới hạn gửi đúng một lần
khi kênh ngoài là SMTP.

## Quyết định

### 1. Dùng transactional outbox trong MySQL

- Thêm bảng `email_jobs` bằng migration cộng thêm sau khi PRD-402 được duyệt.
- Nghiệp vụ tạo một job trong cùng transaction với thay đổi domain liên quan;
  không gọi SMTP trong transaction nghiệp vụ.
- Password reset trả response trung lập hiện tại sau khi enqueue; SMTP không nằm
  trên request path.
- Worker đọc job theo batch và gửi email ngoài transaction claim. Không giữ khóa
  database trong lúc gọi mạng.
- Áp dụng lần lượt cho password reset; reminder/overdue/daily report; sau đó hai
  email extension request/review. Gate G4 không đạt nếu vẫn còn điểm gọi SMTP trực
  tiếp trong transaction nghiệp vụ.

### 2. Mô hình job và vòng đời

Schema chi tiết được chốt ở PRD-403 nhưng phải biểu diễn tối thiểu:

- `id`, `job_type`, `recipient`, `subject`, `template_key`, `template_version`, `payload`;
- `idempotency_key` có unique constraint;
- `status`, `attempt_count`, `max_attempts`, `available_at`;
- `lease_owner`, `lease_until`, `created_at`, `updated_at`, `sent_at`;
- `last_error_code` và `last_error_summary` đã lọc dữ liệu nhạy cảm;
- ciphertext/nonce/key-version riêng cho payload bí mật nếu job cần.

Vòng đời chuẩn:

```text
PENDING/RETRY_WAIT -> PROCESSING -> SENT
                              \-> RETRY_WAIT
                              \-> DEAD
                              \-> UNKNOWN
PENDING/RETRY_WAIT -----------> EXPIRED
```

- `DEAD`: hết số lần thử hoặc lỗi vĩnh viễn đã phân loại.
- `EXPIRED`: job không còn giá trị nghiệp vụ, ví dụ reset token đã hết hạn.
- `UNKNOWN`: worker mất lease sau khi bắt đầu thao tác gửi và không thể biết SMTP
  đã nhận thư hay chưa. Không tự retry trạng thái này để tránh gửi trùng mù.
- Chỉ transition có điều kiện theo `status`, `lease_owner` và version hiện tại mới
  được cập nhật; terminal state không được gửi lại tự động.

### 3. Claim và retry

- Worker claim tối đa một batch trong transaction ngắn bằng
  `SELECT ... FOR UPDATE SKIP LOCKED`, chuyển job sang `PROCESSING`, gán lease rồi
  commit trước khi gọi SMTP.
- Chỉ job `available_at <= now`, chưa terminal và còn số lần thử mới được claim.
- Lỗi kết nối rõ ràng trước khi SMTP chấp nhận thư chuyển sang `RETRY_WAIT` với
  exponential backoff và jitter. Lỗi validation/recipient vĩnh viễn chuyển `DEAD`.
- Job `PROCESSING` hết lease không được tự coi là chưa gửi; chuyển `UNKNOWN` để
  người vận hành xử lý hoặc để người dùng tạo yêu cầu mới.
- Network timeout phải nhỏ hơn lease; worker shutdown ngừng claim mới và hoàn tất
  batch đang giữ trong giới hạn timeout.

### 4. Idempotency và ý nghĩa “gửi đúng một lần”

- Mỗi email logic có một `idempotency_key` xác định và unique constraint ngăn
  scheduler/request tạo hai job giống nhau. Database lưu SHA-256 dạng 64 ký tự
  hex của khóa logic chuẩn hóa để tránh đưa recipient/PII vào index này.
- Khóa dự kiến gồm loại event, domain ID, phiên bản/sự kiện và recipient; ví dụ
  `assignment-reminder:{assignmentId}:{expectedReturnAt}:{recipientId}` hoặc
  `extension-reviewed:{extensionId}:{status}:{recipient}`.
- Worker gửi `Message-ID` ổn định theo job ID. Nếu adapter production hỗ trợ
  idempotency key thì truyền cùng khóa cho provider.
- MySQL outbox bảo đảm một job logic được ghi đúng một lần và không có hai worker
  gửi đồng thời. SMTP thuần không thể bảo đảm toán học exactly-once ở cửa sổ
  “SMTP đã nhận nhưng process chết trước khi ghi SENT”. Vì vậy project không tuyên
  bố exactly-once delivery với SMTP thuần; `UNKNOWN` chặn retry tự động trong cửa
  sổ mơ hồ. Muốn cam kết exactly-once ở môi trường production phải chọn provider
  có idempotency API hoặc cơ chế đối soát tương đương.

### 5. Password reset và dữ liệu bí mật

- Giữ nguyên ADR-001: raw reset token sinh từ 32 byte `SecureRandom`, database chỉ
  dùng SHA-256 hash để tra cứu/xác nhận và token chỉ dùng một lần.
- Để worker có thể gửi cùng một link khi retry mà không lưu raw token dạng rõ,
  raw token/reset link được mã hóa AES-256-GCM trong phần secret payload của job.
- Khóa mã hóa nằm trong secret store/biến môi trường, tách khỏi JWT signer key;
  mỗi ciphertext có nonce ngẫu nhiên và key version để hỗ trợ rotation.
- Không log payload, ciphertext, nonce, reset URL hoặc raw token. Secret payload
  bị xóa ngay khi job vào `SENT`, `DEAD`, `UNKNOWN` hoặc `EXPIRED`.
- Job reset không được gửi sau `expires_at`. Token được vô hiệu hóa khi job vào
  `DEAD`/`EXPIRED`; với `UNKNOWN`, token còn hiệu lực tới expiry để link vẫn dùng
  được nếu SMTP thực tế đã nhận thư. Cooldown và pessimistic lock tiếp tục áp dụng.
- Thiếu hoặc sai khóa mã hóa phải fail closed: không gửi email và không đưa secret
  vào log/error summary.

### 6. Snapshot nội dung và trạng thái domain

- Job lưu snapshot dữ liệu cần render cùng `template_version`; worker không dựng
  lại nội dung từ trạng thái domain có thể đã thay đổi, ngoại trừ dữ liệu vận hành
  không nhạy cảm đã được thiết kế rõ.
- `reminderNotifiedAt` và `overdueNotifiedAt` mang nghĩa đã gửi thành công, không
  phải đã enqueue. Worker cập nhật marker cùng transaction chuyển job sang `SENT`.
- Scheduler có thể quét lặp; unique idempotency key ngăn tạo job trùng khi marker
  chưa được cập nhật.

### 7. Giới hạn đã được duyệt ở PRD-402

| Thiết lập | Giá trị đề xuất |
|---|---|
| Batch claim | 20 job |
| Lease | 60 giây |
| Timeout SMTP | connect 5 giây, read/write 10 giây |
| Job thường | tối đa 5 lần: ngay, 1 phút, 5 phút, 30 phút, 2 giờ |
| Password reset | tối đa 4 lần: ngay, 30 giây, 2 phút, 5 phút; không vượt token expiry 15 phút |
| Retention `SENT`/`EXPIRED` | 30 ngày |
| Retention `DEAD`/`UNKNOWN` | 90 ngày để điều tra |
| Error summary | tối đa 500 ký tự đã redaction; không lưu stack trace trong bảng |

Người dùng đã duyệt các giá trị trên ngày 2026-10-10. Mọi thay đổi giới hạn phải
được ghi lại trong ADR mới hoặc một lần cập nhật quyết định có review tương ứng.

## Lý do

- MySQL outbox cho phép ghi thay đổi nghiệp vụ và ý định gửi email một cách atomic
  mà không thêm broker trước khi có nhu cầu tải đã đo.
- Claim ngắn bằng lease/SKIP LOCKED hỗ trợ nhiều worker nhưng không giữ database
  lock trong lúc gọi mạng.
- Unique idempotency key chặn enqueue trùng và snapshot giữ nội dung nhất quán.
- Mã hóa secret payload duy trì mục tiêu của ADR-001 khi email phải được gửi sau
  transaction tạo token.
- Phân biệt retry an toàn với trạng thái `UNKNOWN` phản ánh đúng giới hạn của SMTP
  thay vì đưa ra bảo đảm exactly-once không thể kiểm chứng.

## Phương án đã cân nhắc

### Tiếp tục gửi đồng bộ

- Ưu điểm: ít code, lỗi gửi biết ngay.
- Nhược điểm: giữ transaction/lock khi gọi mạng, không retry bền vững, tạo timing
  side-channel ở password reset.
- Lý do không chọn: không đạt Gate G4 và PR-208.

### Spring `@Async` hoặc queue trong bộ nhớ

- Ưu điểm: nhanh triển khai, request không chờ SMTP.
- Nhược điểm: mất job khi process restart, khó claim giữa nhiều instance và không
  atomic với nghiệp vụ.
- Lý do không chọn: không đáp ứng durability.

### Message broker ngay từ đầu

- Ưu điểm: throughput và tooling tốt khi hệ thống lớn.
- Nhược điểm: thêm hạ tầng, secret, deploy/monitoring và vẫn cần giải quyết dual
  write hoặc outbox relay.
- Lý do chưa chọn: quy mô hiện tại chưa có bằng chứng cần broker; có thể thêm relay
  sau mà không đổi contract enqueue.

### Lưu raw reset token trong JSON job

- Ưu điểm: worker đơn giản.
- Nhược điểm: người đọc database có thể chiếm tài khoản khi token còn hiệu lực,
  trái ADR-001.
- Lý do không chọn: rủi ro bảo mật không chấp nhận được.

## Hệ quả

### Tích cực

- SMTP lỗi không rollback nghiệp vụ hoặc làm mất ý định gửi email.
- Có retry, trạng thái terminal và bằng chứng database để vận hành/kiểm thử.
- Request password reset không còn chờ SMTP và giảm nguồn timing leak lớn nhất.
- Có thể scale worker ngang bằng claim lease mà không gửi đồng thời cùng job.

### Đánh đổi

- Thêm bảng, worker, cleanup, mã hóa payload, rotation key và runbook xử lý
  `DEAD`/`UNKNOWN`.
- Email trở thành eventual delivery; API không thể hứa email đã tới ngay khi trả
  response.
- SMTP thuần vẫn có cửa sổ kết quả mơ hồ; production muốn exactly-once cần provider
  hỗ trợ idempotency/đối soát.
- Rate limit dùng chung và timing test vẫn là task riêng PRD-407/PR-209.

## Kế hoạch triển khai và rollback

1. PRD-402 duyệt giới hạn, retention, delivery semantics và cách quản lý encryption key.
2. PRD-403 thêm schema/entity/repository và test migration/claim.
3. PRD-404 thêm worker, retry, cleanup, redaction và failure/crash tests.
4. PRD-405 chuyển password reset, giữ API response trung lập và kiểm tra token.
5. PRD-406 chuyển reminder, overdue, daily report và extension notification.
6. PRD-409 tắt/bật SMTP sandbox để xác nhận pending/retry/SENT và không enqueue trùng.

Rollback code quay về producer/worker cũ nhưng không xóa bảng hoặc migration đã áp
dụng. Worker mới phải có feature flag/pause claim để rollback không tiếp tục gửi;
job pending được giữ để forward-fix hoặc xử lý theo runbook, không xóa tự động.

## Điều kiện xem xét lại

- Lưu lượng hoặc latency backlog đã đo cho thấy MySQL polling là nút thắt.
- Hệ thống chọn email provider có idempotency API/webhook delivery rõ ràng.
- Yêu cầu compliance thay đổi retention/PII/encryption hoặc cần KMS/HSM.
- Cần ưu tiên, nhiều tenant hoặc nhiều loại event vượt khả năng một bảng job.
