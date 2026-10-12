# Chạy Device trong môi trường local cô lập

Xem thêm [device-data-scale.md](device-data-scale.md) khi cần sinh dataset tổng
hợp và kiểm tra import/export trên local/Testcontainers. Khi cần điều tra API chậm,
DB connection hoặc scheduler/email job, dùng
[operations-troubleshooting.md](operations-troubleshooting.md).

## Mục đích và ranh giới

Bộ Compose tại `compose.yaml` chỉ dành cho phát triển/kiểm thử local. MySQL dùng volume riêng của Compose, Mailpit giữ email trong môi trường thử, backend chỉ mở cổng trên `127.0.0.1`. Không dùng credentials, URL database hoặc email production trong bộ này.

## Chuẩn bị

1. Cài Docker Engine/Desktop có Docker Compose plugin. Kiểm tra `docker compose version` và `docker info` bằng chính tài khoản sẽ chạy test. Quyền truy cập Docker daemon tương đương quyền quản trị máy; cân nhắc trước khi thêm tài khoản vào nhóm `docker`.
2. Tại thư mục `device/device`, sao chép `.env.example` thành `.env` rồi điền bốn giá trị local-only. `.env` đã được Git bỏ qua. Dùng mật khẩu khác production. `LOCAL_JWT_SIGNER_KEY` cần ít nhất 64 byte, có thể tạo bằng `openssl rand -hex 64`; `LOCAL_EMAIL_JOB_SECRET_KEY` phải là đúng 32 byte sau khi giải mã Base64, có thể tạo bằng `openssl rand -base64 32`. Hai khóa này phải độc lập.
3. Kiểm tra các giá trị không rỗng và không dán chúng vào log, issue hoặc chat. Đừng đưa `.env` vào Git. Khi xoay khóa email job, giữ khóa cũ trong cấu hình cho tới khi không còn job nào được mã hóa bằng phiên bản đó.

## Khởi động

```bash
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

Compose sẽ đợi MySQL báo healthy trước khi khởi động backend, sau đó dùng
`/actuator/health/readiness` để đợi application state và database sẵn sàng trước khi
khởi động frontend. Flyway chạy V1-V8 khi backend kết nối database mới; profile local
bật email worker để xử lý outbox. Giao diện ở `http://localhost:5173`, API ở
`http://localhost:8080`; hộp thư test ở `http://localhost:8025`.

## Phát triển backend bằng IntelliJ

Chế độ này chạy MySQL và Mailpit trong Docker, còn backend chạy trực tiếp trong IntelliJ để dùng breakpoint và hot reload nhanh hơn. Secret vẫn chỉ nằm trong `.env`; run configuration được chia sẻ không chứa mật khẩu, khóa JWT hoặc khóa mã hóa email job.

1. Chuẩn bị `.env` như phần trên, sau đó chạy:

```bash
bash scripts/dev-ide.sh up
```

2. Trong IntelliJ, chọn run configuration `Device Backend (IDE)` thay cho cấu hình `Unnamed`, rồi bấm Run hoặc Debug. Profile `dev,ide` tự đọc `.env`, kết nối MySQL qua `127.0.0.1:3307`, Mailpit SMTP qua `127.0.0.1:1025` và bật email worker.
3. Chạy frontend Vite trong terminal khác:

```bash
cd frontend
npm run dev
```

4. Khi muốn quay lại full Compose:

```bash
bash scripts/dev-ide.sh full
```

Không chạy đồng thời backend IntelliJ và backend container vì cả hai dùng cổng `8080`. Không thêm secret trực tiếp vào file `.run` hoặc commit `.env`.

Khi cần phát triển frontend với Vite hot reload thay vì container Nginx, có thể chạy riêng:

```bash
cd frontend
npm ci
npm run dev
```

Trước đó dừng riêng service frontend container hoặc chọn cổng Vite khác để tránh xung đột cổng `5173`. Frontend Vite proxy `/api` về backend tại `localhost:8080`; frontend container Nginx proxy `/api` qua service `backend` trong mạng Compose.

## Kiểm tra quên mật khẩu bằng tay

1. Đăng nhập bằng `admin@device.local` và `LOCAL_ADMIN_PASSWORD` đã đặt khi tạo volume lần đầu.
2. Gửi yêu cầu quên mật khẩu cho `admin@device.local` trên frontend.
3. Mở Mailpit, bấm liên kết reset, nhập mật khẩu mới, đăng nhập lại. Xác nhận mật khẩu cũ không còn dùng được.
4. Yêu cầu lại và kiểm tra cooldown theo `specs/password-reset/`. Không dán reset token vào tài liệu hoặc ảnh.

Backend chỉ seed tài khoản admin nếu email chưa tồn tại. Đổi `LOCAL_ADMIN_PASSWORD` sau lần khởi tạo volume **không** tự đổi mật khẩu đã lưu. Không xóa volume để “thử lại” nếu còn dữ liệu cần giữ.

## Diễn tập SMTP failure/recovery PRD-409

Không bấm link reset trong bài diễn tập này và không chép recipient, payload hoặc
reset token vào log/tài liệu.

1. Xác nhận bốn service đang healthy bằng `docker compose ps`, sau đó dừng riêng
   SMTP sandbox: `docker compose stop mailpit`.
2. Trên trang `/forgot-password`, gửi đúng một yêu cầu cho tài khoản local có thật.
   API phải vẫn trả HTTP `202`; backend không chờ SMTP.
3. Chờ worker xử lý lần đầu, rồi xem bản ghi mới nhất bằng truy vấn chỉ chọn metadata:

```bash
docker compose exec -T mysql sh -lc 'mysql --batch -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" -e "SELECT id, job_type, status, attempt_count, max_attempts, available_at, last_error_code FROM email_jobs WHERE job_type = '\''PASSWORD_RESET'\'' ORDER BY created_at DESC LIMIT 1"'
```

   Kỳ vọng job còn tồn tại, `status=RETRY_WAIT`, `attempt_count >= 1`,
   `last_error_code=EMAIL_SEND_FAILED`; không được là `SENT` hoặc biến mất.
4. Khởi động lại sandbox bằng `docker compose start mailpit`. Password-reset retry
   đầu tiên đến sau khoảng 30 giây cộng tối đa 10% jitter; worker poll mỗi 5 giây.
5. Chạy lại truy vấn metadata cho tới khi job thành `SENT`; `attempt_count` phải
   tăng đúng một so với lần quan sát `RETRY_WAIT` gần nhất trước khi bật lại Mailpit.
   Mở `http://localhost:8025`, xác nhận chỉ có một email của yêu cầu vừa tạo.
6. Xem log đã lọc bằng `docker compose logs --since=10m backend`; phải có failure
   retryable nhưng không có raw reset token, password, payload hoặc reset URL.

Nếu để Mailpit tắt qua mọi lần retry, password-reset job sẽ đi `DEAD` và token bị
vô hiệu hóa; đó là kịch bản khác, không cần chờ hết 15 phút cho PRD-409 cơ bản.
Không dùng `docker compose down -v` trong bài diễn tập.

## Kiểm thử tự động

```bash
bash scripts/verify.sh safe
bash scripts/verify.sh integration
bash scripts/verify.sh frontend-e2e
```

Lệnh thứ hai cần Docker: profile `test` dùng MySQL Testcontainers tạm thời và chạy migration trên database đó. Lệnh thứ ba yêu cầu đủ bốn service Compose đang healthy và chạy live frontend/API/Mailpit smoke. Nếu Docker không có hoặc daemon không chạy, test integration phải thất bại; không đổi sang URL MySQL/Aiven thật để cho test xanh.

## Monitoring local PRD-505

Profile monitoring chỉ dành cho máy local. Prometheus và Grafana đều bind vào
`127.0.0.1`; management port `8081` của backend chỉ nằm trong Compose network và
không publish ra host.

```bash
docker compose --profile monitoring up --build -d
docker compose ps
```

- Grafana: `http://127.0.0.1:3000`, anonymous chỉ có quyền Viewer và không có tài
  khoản admin được tạo. Dashboard provisioned nằm trong folder `Device` với tên
  `Device Management Operations`.
- Prometheus: `http://127.0.0.1:9090`; target `device-backend` phải `UP` sau tối đa
  hai chu kỳ scrape 15 giây.
- API vẫn ở `127.0.0.1:8080`; request ẩn danh tới `/actuator/prometheus` trên port
  API bị chặn với HTTP `401` và không nhận dữ liệu metrics. Prometheus chỉ scrape
  `backend:8081/actuator/prometheus` qua mạng Compose nội bộ.
- Dashboard có health/scrape, HTTP p95 và error rate, JDBC pool, email job/outcome,
  scheduler duration và JVM heap. Panel chưa có traffic có thể hiện `No data` cho tới
  khi request/job/scheduler tương ứng chạy.

Dừng riêng monitoring mà giữ stack ứng dụng:

```bash
docker compose --profile monitoring stop grafana prometheus
```

Không dùng cấu hình anonymous Viewer này cho Internet/staging/production. Môi trường
demo thật phải đặt Grafana sau authentication/TLS và giữ scrape port trên private
network hoặc service-to-service authentication.

## Dừng và chẩn đoán

```bash
docker compose logs --tail=100 backend
docker compose logs --tail=100 mysql
docker compose ps
docker compose down
```

`docker compose down` giữ volume MySQL. Không dùng tùy chọn xóa volume khi chưa backup và xác nhận dữ liệu local không cần giữ. Nếu backend không lên, kiểm tra MySQL healthy, các biến `.env`, rồi log Flyway. Nếu email không tới Mailpit, kiểm tra `mailpit` đang chạy và `MAIL_HOST=mailpit` trong Compose.
