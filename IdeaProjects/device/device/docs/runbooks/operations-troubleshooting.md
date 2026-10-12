# Chẩn đoán vận hành Device

Runbook này dành cho local/test và môi trường demo đã được cô lập. Nó giúp phân loại
ba tình huống: API chậm, cạn connection database, và scheduler/email job bị kẹt.
Không dùng các bước bên dưới để sửa trực tiếp dữ liệu production.

## Nguyên tắc trước khi chẩn đoán

1. Ghi thời điểm bắt đầu, timezone, môi trường và triệu chứng. Với lỗi từ UI/API, lấy
   `X-Request-ID` hoặc “Mã tham chiếu”; với email lấy `job_id` từ log, không lấy token.
2. Bắt đầu bằng thao tác chỉ đọc. Không restart, kill connection, sửa `email_jobs`, xóa
   volume hoặc tăng pool trước khi đã lưu bằng chứng và xác định tác động.
3. Không chép password, JWT, reset URL/token, recipient, `payload`, `secret_ciphertext`
   hoặc biến môi trường bí mật vào ticket, ảnh hay lệnh.
4. Với local Compose, bật stack quan sát theo
   [local-stack.md](local-stack.md#monitoring-local-prd-505), rồi xác nhận:

```bash
docker compose --profile monitoring ps
```

Prometheus ở `http://127.0.0.1:9090`; Grafana ở `http://127.0.0.1:3000` và dashboard
`Device Management Operations`. Management port `8081` không được publish ra host.

## Tìm một request từ mã hỗ trợ

Dùng đúng ID do response trả về; không tìm bằng email, token hoặc body:

```bash
docker compose logs --since=30m backend | grep -F 'request_id=<REQUEST_ID>'
```

Một request bình thường có event `http_request_completed` với `method`, `path`,
`http_status`, `duration_ms`. Request bị từ chối/lỗi có thêm một trong các event
`api_request_rejected`, `api_request_conflict`, `api_request_failed`. Với password
reset, cùng `request_id` có thể dẫn tới `email_job_enqueued` rồi tới delivery event có
`job_id`.

Nếu không tìm thấy, kiểm tra đúng container, cửa sổ `--since`, timezone và ID. Không
nới tìm kiếm sang request body hoặc secret.

## Tình huống A — API chậm

### 1. Xác nhận phạm vi

Mở panel `HTTP latency p95` và chọn cùng cửa sổ thời gian với sự cố. Có thể chạy các
PromQL sau trong Prometheus:

```promql
histogram_quantile(0.95, sum by (le, uri) (rate(http_server_requests_seconds_bucket{job="device-backend"}[5m])))
```

```promql
sum by (status) (rate(http_server_requests_seconds_count{job="device-backend",status=~"4..|5.."}[5m]))
```

- Một route tăng p95: ưu tiên tìm request ID của route đó và kiểm tra dependency riêng.
- Mọi route cùng tăng: kiểm tra DB pool, JVM heap và trạng thái container trước.
- Chỉ một request chậm nhưng p95 bình thường: dùng `duration_ms` trong log để phân biệt
  outlier với suy giảm toàn hệ thống.

### 2. Kiểm tra database và JVM cùng thời điểm

```promql
sum(jdbc_connections_active{job="device-backend"}) / clamp_min(sum(jdbc_connections_max{job="device-backend"}), 1)
```

```promql
sum(jvm_memory_used_bytes{job="device-backend",area="heap"})
```

Pool gần 100% và idle bằng 0 dẫn sang tình huống B. Heap tăng không giảm qua nhiều cửa
sổ chỉ là tín hiệu cần điều tra thêm, không tự kết luận memory leak và không tự tăng heap.

### 3. Kết luận và hành động

Ghi route, p95, error rate, request ID đại diện, `duration_ms`, pool active/max và heap
trong cùng cửa sổ. Nếu tái hiện được ở local/test, lưu lệnh và thời gian. Không claim
root cause chỉ từ một panel; cần log hoặc database evidence phù hợp.

## Tình huống B — DB connection pool cạn

### 1. Xác nhận bằng metrics

```promql
sum(jdbc_connections_active{job="device-backend"})
```

```promql
sum(jdbc_connections_idle{job="device-backend"})
```

```promql
sum(jdbc_connections_max{job="device-backend"})
```

Nếu active chạm max, idle bằng 0 và latency/error tăng cùng lúc, lấy log backend trong
cửa sổ đó và tìm exception type liên quan connection/timeout; không ghi exception
message nhạy cảm vào báo cáo.

### 2. Xem trạng thái MySQL chỉ bằng metadata

Các lệnh dùng credential đã có bên trong container và cố ý không chọn cột SQL text:

```bash
docker compose exec -T mysql sh -lc 'mysql --batch -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT ID, USER, HOST, DB, COMMAND, TIME, STATE FROM information_schema.PROCESSLIST WHERE DB = DATABASE() ORDER BY TIME DESC"'
```

```bash
docker compose exec -T mysql sh -lc 'mysql --batch -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SHOW GLOBAL STATUS WHERE Variable_name IN ('\''Threads_connected'\'', '\''Threads_running'\'', '\''Max_used_connections'\'')"'
```

Ghi số connection, thời gian giữ lâu nhất và state. Không chép cột `INFO`, vì có thể
chứa literal từ câu SQL. Không `KILL` connection hoặc restart MySQL chỉ để làm biểu đồ
hết đỏ; việc đó có thể rollback transaction đang chạy.

### 3. Phân loại

- Connection `Sleep` có thể là connection nhàn rỗi bình thường của pool; so sánh với
  active/idle và pool max trước khi điều tra release path hoặc transaction boundary.
- Nhiều connection chạy lâu: xác định endpoint/request ID và xem query plan trên bản
  sao test; không chạy `EXPLAIN ANALYZE` tùy tiện trên production.
- MySQL bình thường nhưng pool vẫn đầy: kiểm tra network/timeout giữa app và DB cùng
  log exception type.

Mitigation như restart backend, kill session, tăng pool hoặc đổi timeout cần owner môi
trường duyệt. Sau mitigation phải kiểm tra lại readiness, latency/error và active/idle;
không coi restart là root-cause fix.

## Tình huống C — Scheduler hoặc email job kẹt

### 1. Kiểm tra scheduler và worker

```promql
sum by (scheduler, outcome) (increase(device_scheduler_duration_seconds_count{job="device-backend"}[15m]))
```

```promql
sum by (outcome) (increase(device_email_worker_deliveries_total{job="device-backend"}[15m]))
```

```promql
sum by (status) (device_email_jobs{job="device-backend"})
```

Với local/dev, `email_poll` phải tăng thường xuyên khi worker bật. `failure` tăng, hàng
đợi `pending`/`retry_wait` tăng liên tục, hoặc `processing` không giảm là tín hiệu điều
tra. Scheduler chạy theo cron như daily report có thể hợp lệ khi không tăng trong cửa
sổ 15 phút; đối chiếu lịch cấu hình trước khi kết luận bị kẹt.

Kiểm tra flag local mà không in toàn bộ environment:

```bash
docker compose exec -T backend printenv EMAIL_JOB_WORKER_ENABLED
```

### 2. Tìm log theo event hoặc ID

```bash
docker compose logs --since=30m backend | grep -E 'event=(assignment_notification_batch|daily_overdue_report_enqueued|email_job_)'
```

Các outcome chính:

- `email_job_delivery_succeeded`: job đã gửi và commit `SENT`.
- `failure_kind=retryable`: job về `RETRY_WAIT`; worker sẽ thử lại theo backoff.
- `failure_kind=permanent`: job đi `DEAD`, không retry tự động.
- `email_job_delivery_unknown` hoặc `email_job_lease_recovered`: delivery outcome mơ
  hồ; không retry thủ công vì có nguy cơ gửi trùng.

### 3. Đọc email job metadata an toàn

Không chọn `recipient`, `subject`, `payload`, ciphertext, nonce hoặc key version:

```bash
docker compose exec -T mysql sh -lc 'mysql --batch -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT status, COUNT(*) AS job_count, MIN(created_at) AS oldest_created_at, MIN(available_at) AS earliest_available_at, MAX(attempt_count) AS max_attempt_count FROM email_jobs GROUP BY status ORDER BY status"'
```

```bash
docker compose exec -T mysql sh -lc 'mysql --batch -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT BIN_TO_UUID(id) AS job_id, request_id, job_type, status, attempt_count, max_attempts, available_at, lease_until, last_error_code, created_at, updated_at, sent_at FROM email_jobs WHERE status IN ('\''PENDING'\'', '\''RETRY_WAIT'\'', '\''PROCESSING'\'', '\''DEAD'\'', '\''UNKNOWN'\'') ORDER BY created_at LIMIT 50"'
```

Job `PROCESSING` có `lease_until` đã qua cần được recovery scheduler xử lý thành
`UNKNOWN`. Job `RETRY_WAIT` chỉ bị coi kẹt khi `available_at` đã qua, worker đang bật,
backend healthy nhưng nhiều chu kỳ poll vẫn không claim. `DEAD` và `UNKNOWN` là trạng
thái cần review, không được sửa SQL về `PENDING`.

### 4. Kiểm tra dependency và phục hồi an toàn

```bash
docker compose ps backend mailpit mysql
```

- Mailpit/SMTP down: khôi phục dependency rồi quan sát cùng job chuyển từ `RETRY_WAIT`
  sang `SENT`; không tạo request mới liên tục.
- Backend không ready: xử lý readiness/DB trước, vì scheduler nằm trong backend.
- Worker flag sai: sửa cấu hình qua quy trình deploy rồi restart có kiểm soát; không
  thay database để bù cho worker tắt.
- `UNKNOWN`/lease lost: giữ nguyên row và chuyển xử lý cùng `job_id`, `request_id`, thời
  gian và log; SMTP thuần không chứng minh được email chưa gửi.

## Mẫu bằng chứng incident

Ghi ngắn gọn, không chứa dữ liệu nhạy cảm:

```text
Môi trường / timezone:
Cửa sổ thời gian:
Triệu chứng và endpoint:
Request ID / job ID:
PromQL và kết quả chính:
Log event + status/error code:
DB metadata (không recipient/payload/SQL text):
Hành động đã duyệt:
Kết quả sau hành động:
Giới hạn hoặc bước cần chuyển xử lý:
```

Sau khi xử lý, xác nhận target Prometheus `up`, backend readiness khỏe, error/latency
trở về baseline và backlog không tiếp tục tăng. Chỉ đóng incident khi nguyên nhân hoặc
giới hạn chưa xác định đã được ghi rõ; không dùng “restart xong” làm kết luận.
