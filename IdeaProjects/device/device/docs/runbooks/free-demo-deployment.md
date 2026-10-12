# Demo CV miễn phí: Render + Aiven + Resend

## Phạm vi và trạng thái

Người dùng chọn phương án portfolio 0đ ngày 2026-10-12. Backend và frontend dùng
Render Free, MySQL dùng Aiven Free, email demo dùng Resend Free qua HTTPS API.
Codex sở hữu backend PRD-602; Antigravity sở hữu frontend PRD-603.

Trạng thái tài khoản đã được người dùng xác nhận:

- Aiven MySQL 8.4.8 Free đang `Running`; database riêng `device_demo` đã được tạo,
  các database cũ giữ nguyên và CA certificate đã tải ra ngoài repository.
- Render Dashboard truy cập được và chưa tạo service.
- Resend đã tạo API key và gửi email onboarding thành công tới email chủ tài khoản.
- Chưa commit/push source, tạo Render service, nhập toàn bộ secret, deploy hoặc chạy
  migration trên Aiven.

Koyeb không còn là provider của kế hoạch. Thông báo chính thức ngày 2026-02-17 nói
người dùng mới phải đăng ký gói trả phí; màn hình tài khoản mới của người dùng không
có Dashboard deploy. Brevo không được dùng vì onboarding yêu cầu địa chỉ tổ chức có
thể xuất hiện trong email, không phù hợp khi người dùng không có địa chỉ công khai.

Render Free Web Service có 0,1 CPU/512 MB và ngủ sau 15 phút không có inbound traffic.
Khi ngủ, scheduler/email worker trong cùng JVM không chạy: reminder trước hạn có thể
lỡ, daily cron không tự chạy bù. Job pending còn hợp lệ xử lý khi service thức; reset
token quá hạn không được gửi lại, lease không chắc chắn chuyển `UNKNOWN`.

Render Free chặn outbound SMTP 25/465/587. Profile `demo` vì vậy dùng Resend REST API
qua HTTPS 443; local/dev/test vẫn dùng SMTP/Mailpit. Outbox, retry, mã hóa reset secret
và HTTP contract không đổi. Mỗi request Resend có `Idempotency-Key` ổn định theo email
job ID; điều này hỗ trợ retry nhưng không nâng contract thành exactly-once tuyệt đối.

Chưa có custom domain nên ứng dụng bắt buộc `RESEND_ALLOWED_RECIPIENTS` là email chủ
tài khoản đã thử thành công. Public demo không quảng bá khả năng gửi tới email tùy ý.
Account khác vẫn nhận HTTP password-reset response trung lập, nhưng delivery ngoài
allow-list đi trạng thái lỗi vĩnh viễn đã redaction. Khi có domain xác minh, mở rộng
allow-list là một thay đổi cấu hình có chủ đích và phải kiểm tra lại deliverability.

Nguồn chính thức đã đối chiếu ngày 2026-10-12:

- [Koyeb chuyển sang Mistral và người dùng mới cần gói trả phí](https://www.koyeb.com/blog/koyeb-is-joining-mistral-ai-to-build-the-future-of-ai-infrastructure)
- [Render Free và giới hạn sleep/SMTP](https://render.com/docs/free)
- [Render Web Services và Docker](https://render.com/docs/web-services)
- [Render Docker deployment](https://render.com/docs/docker)
- [Render Static Sites](https://render.com/docs/static-sites)
- [Render Deploy Hooks](https://render.com/docs/deploy-hooks)
- [Render Rollbacks](https://render.com/docs/rollbacks)
- [Aiven MySQL Free](https://aiven.io/docs/products/mysql/concepts/mysql-free-tier)
- [Aiven MySQL backup/restore bằng mysqldump](https://aiven.io/docs/products/mysql/howto/migrate-database-mysqldump)
- [MySQL Connector/J TLS/truststore](https://dev.mysql.com/doc/connector-j/en/connector-j-reference-using-ssl.html)
- [Resend Free](https://resend.com/pricing)
- [Resend Email API và idempotency key](https://resend.com/features/email-api)

Không đăng ký paid compute/database, autoscaling, dedicated IP hoặc custom domain.
Nếu provider yêu cầu thanh toán để tiếp tục, dừng trước khi nhập phương thức thanh toán.

## Kiểm chứng image trước khi deploy

Tại thư mục Device chứa `pom.xml`:

```bash
bash scripts/verify.sh safe
docker build -f docker/Dockerfile.demo -t device-demo:prd602 .
bash scripts/smoke-demo.sh --confirm-isolated
```

Smoke tạo MySQL 8.4 yêu cầu TLS và CA/server certificate dùng một lần trên network
riêng. Backend không publish port ra host, giới hạn RAM/swap 512 MiB và CPU 0,1.
Script kiểm tra `VERIFY_IDENTITY`, profile Resend có đủ biến cấu hình nhưng không gọi
Internet, Flyway V1-V8, readiness, login, API devices phân trang, CORS và management
boundary. Nó không đọc `.env`, không nhận external DB URL và tự dọn đúng container,
volume/network tạm của lần chạy.

Test `ResendEmailTransportTest` dùng fake HTTP client, xác nhận JSON, recipient
allow-list, idempotency key và phân loại 400/429 mà không gọi Resend thật. Người dùng
đã gửi email onboarding thật từ Resend Dashboard; application delivery thật chỉ được
xác nhận sau PRD-606 khi backend Render đã deploy.

Image Resend đã kiểm chứng là `3b3fbf302cc0`: readiness sau 425 giây dưới Docker
hard cap 0,1 CPU, RAM mẫu 268,3 MiB/512 MiB và không OOM. Smoke đạt MySQL
VERIFY_IDENTITY TLS, Flyway V1-V8, Resend demo configuration, login, devices, CORS
và management boundary. Số 425 giây không phải dự báo cold start Render hoặc peak
RSS. Mở sẵn demo trước phỏng vấn và đo wake-up thật sau deploy.

## Việc người dùng chuẩn bị — PRD-601

1. Aiven Free/database `device_demo`/CA: đã xong; không xóa `defaultdb` hoặc các
   database `device_db*` cũ.
2. Render account/Dashboard: đã xong; chưa tạo service trước khi source được review,
   commit và push.
3. Resend account/API key/email onboarding: đã xong. Key chỉ lưu trong Render secret
   hoặc password manager; không gửi qua chat, ảnh, Git hay Markdown.
4. Render Static Site/Web Service, các secret còn lại và URL thật: chưa làm.
5. Migration Aiven/Flyway và cloud verification: chờ người dùng duyệt ở PRD-606.

Cloud build chỉ thấy source trên remote repository, không thấy working tree local.
Review/commit/push cần một lượt riêng có sự cho phép của người dùng.

## Thứ tự tạo Render resource để không bị vòng lặp URL

Sau khi PRD-603 hoàn tất và source đã được push:

1. Tạo Static Site frontend với `VITE_API_BASE_URL=https://placeholder.invalid/api`
   để lấy hostname frontend thật; chưa coi site này là hoạt động.
2. Tạo backend Web Service, dùng hostname frontend thật cho `FRONTEND_ORIGIN` và
   `PASSWORD_RESET_BASE_URL`; deploy và lấy hostname backend thật.
3. Đổi `VITE_API_BASE_URL` sang `https://<backend-host>.onrender.com/api`, redeploy
   frontend.
4. Chạy toàn bộ cloud check ở cuối runbook. Không để placeholder tồn tại sau deploy.

Giữ Auto-Deploy `Off` cho cả hai service. Workflow GitHub chỉ gọi deploy hook sau khi
`safe`, `integration` và `compose-smoke` đều đạt trên push `main`; điều này tránh một
bản chưa qua kiểm tra tự deploy chỉ vì được push.

## Backend Render Web Service

| Setting | Giá trị |
|---|---|
| Type | Web Service |
| Source | Git provider repository của project |
| Branch | `main` sau khi review/push |
| Language/runtime | Docker |
| Root Directory từ Git root | `IdeaProjects/device/device` |
| Dockerfile path trong root | `docker/Dockerfile.demo` |
| Compute plan | `Free`, 0,1 CPU/512 MB |
| Region | Chọn gần region Aiven nhất trong lựa chọn Free |
| Port | Dùng biến `PORT` do Render cấp; application bind `0.0.0.0` |
| Health check path | `/actuator/health/readiness` |
| Profile | Chỉ `demo` từ image; không override thành `dev,monitoring` |
| Auto deploy ban đầu | Tắt/manual; CI/CD thuộc PRD-604 |

Image tạo CA truststore dưới `/tmp` lúc startup bằng `keytool`, chạy UID 10001.
Không bake CA/secret vào image hoặc build argument. Cổng management 8081/Prometheus
không được public.

## Runtime environment và secrets

Nhập trực tiếp bằng Render Environment Group/service UI. Không ghi giá trị thật vào
Markdown, Git, chat hoặc screenshot.

| Key | Cách điền |
|---|---|
| `DB_URL` | `jdbc:mysql://<aiven-host>:<port>/device_demo`; không nhúng user/password hoặc TLS query override |
| `DB_USERNAME`, `DB_PASSWORD` | Credentials Aiven; không gửi qua chat |
| `DB_CA_CERT_BASE64` | Base64 một dòng từ CA đã tải ngoài repository |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Owner admin riêng, không public cho người xem demo |
| `JWT_SIGNER_KEY` | Secret độc lập, tối thiểu 64 byte cho HS512 |
| `EMAIL_JOB_SECRET_KEY_BASE64` | Secret 32 byte Base64, độc lập JWT |
| `FRONTEND_ORIGIN` | Origin Static Site HTTPS chính xác, không dấu `/` cuối |
| `PASSWORD_RESET_BASE_URL` | `https://<frontend-host>.onrender.com/reset-password` |
| `RESEND_API_KEY` | Key `Sending access`; không dùng Full access nếu không cần |
| `RESEND_ALLOWED_RECIPIENTS` | Email chủ Resend đã nhận onboarding; có thể là danh sách phân cách dấu phẩy |
| `RESEND_FROM` | Mặc định `Device Management <onboarding@resend.dev>` khi chưa có domain |

Không đặt `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` trên Render.
Profile demo cố định transport `resend`; SMTP vẫn dành cho local/dev/test.

Image mặc định `SPRING_PROFILES_ACTIVE=demo`, heap 256 MiB, metaspace 128 MiB,
direct memory 32 MiB, SerialGC, stack 512 KiB, exit khi OOM và timezone
`Asia/Ho_Chi_Minh`. `-XX:TieredStopAtLevel=1` ưu tiên startup trên CPU nhỏ nhưng có
thể giảm peak throughput; chỉ dùng cho demo ít người. Demo Hikari tối đa 5 connection,
email batch/thread 2 và Tomcat tối đa 16 thread.

Entrypoint tự tạo `DB_TLS_TRUSTSTORE_URL/PASSWORD`; người dùng không điền hai biến đó.
Không đưa secret vào `JAVA_TOOL_OPTIONS` vì JVM in biến này khi khởi động. Không hạ
`VERIFY_IDENTITY`, tắt recipient allow-list hoặc tắt rate limit để xử lý lỗi.

CA là chứng chỉ công khai nhưng phải đúng service Aiven. Encode ngoài repository:

```bash
base64 -w 0 /duong-dan-ngoai-repo/aiven-device-demo-ca.pem
```

## Bàn giao PRD-603 cho Antigravity

- Chỉ sửa `frontend/`; đọc contract trong `specs/production-readiness/spec.md`.
- Render Root Directory: `IdeaProjects/device/device/frontend`; build
  `npm ci && npm run build`; Publish Directory `dist`.
- `VITE_API_BASE_URL=https://<backend-host>.onrender.com/api` là build-time value,
  không phải secret. Backend chỉ allow-list hostname frontend thật.
- SPA rewrite `/*` → `/index.html`; giữ NoReferrer/nosniff/DENY theo config hiện có.
- Kiểm tra login/forgot/reset/dashboard/devices, deep-link reload, 401/429 và request ID.
  Axios chưa có timeout chung; xác nhận loading/error khi backend Free đang wake-up.
- Báo file sửa, test/lint/build và blocker URL thật cho Codex tích hợp. Không sửa
  backend/spec/handoff trong working tree dùng chung.

## CI/CD và post-deploy check — PRD-604

Workflow `.github/workflows/device-ci.yml` có job `deploy-demo`, chỉ chạy khi đồng thời:

- ref là `main`, event là push hoặc manual `workflow_dispatch` có chọn `deploy_demo`;
- `safe`, `integration` và `compose-smoke` đều thành công;
- GitHub environment/repository variable `DEMO_DEPLOY_ENABLED` bằng `true`.

Tạo GitHub environment tên `demo`. Sau khi cả hai Render service đã tồn tại và đã
hoàn tất quy trình placeholder, khai báo:

| Loại | Tên | Giá trị |
|---|---|---|
| Secret | `RENDER_API_TOKEN` | Token Render chỉ lưu trong GitHub secret; dùng để đọc trạng thái deploy |
| Secret | `RENDER_BACKEND_DEPLOY_HOOK_URL` | Deploy hook bí mật của backend |
| Secret | `RENDER_FRONTEND_DEPLOY_HOOK_URL` | Deploy hook bí mật của frontend |
| Variable | `RENDER_BACKEND_SERVICE_ID` | ID `srv-...` của backend |
| Variable | `RENDER_FRONTEND_SERVICE_ID` | ID `srv-...` của frontend |
| Variable | `DEMO_BACKEND_URL` | Backend HTTPS origin, không có `/api` hoặc dấu `/` cuối |
| Variable | `DEMO_FRONTEND_URL` | Frontend HTTPS origin, không có dấu `/` cuối |
| Repository variable | `DEMO_DEPLOY_ENABLED` | Giữ `false`/không tạo đến khi cấu hình xong; đổi thành `true` để cho phép deploy. Phải là repository/org variable vì job-level `if` được xét trước khi GitHub nạp environment `demo` |

Không ghi ba secret vào workflow, shell history, ảnh hoặc Markdown. Nếu deploy hook
lộ, regenerate trong Render và thay GitHub secret. Nếu API token lộ, revoke rồi tạo
token mới. `scripts/render-deploy-and-wait.sh` yêu cầu cờ xác nhận, deploy đúng SHA,
không đưa hook/token lên command line, lấy deploy ID và chờ Render API báo `live`.
Deploy đang chồng lấn nhưng Render không trả ID sẽ làm CI fail an toàn để chạy lại,
không kiểm tra nhầm bản cũ.

Sau lần cấu hình resource đầu tiên, có thể vào GitHub Actions chạy manual workflow
trên branch `main` với `deploy_demo=true`; không cần tạo commit rỗng chỉ để kích hoạt
deploy. Mọi cổng CI vẫn chạy trước job deploy.

Sau backend và frontend cùng `live`, `scripts/post-deploy-check.sh` xác nhận tự động:

- readiness `UP` nhưng không lộ component/detail;
- frontend root, deep-link SPA, ba security header và bundle không còn placeholder;
- bundle chứa chính xác `${DEMO_BACKEND_URL}/api`;
- API được bảo vệ trả 401 và Prometheus không public;
- CORS cho đúng frontend origin, không credentials; origin giả bị 403.

Có thể chạy lại smoke không deploy bằng lệnh sau; đây là check chỉ đọc:

```bash
bash scripts/post-deploy-check.sh \
  --backend-url https://<backend-host>.onrender.com \
  --frontend-url https://<frontend-host>.onrender.com
```

### Rollback code/config

1. Đặt `DEMO_DEPLOY_ENABLED=false` trước, rồi tắt Auto-Deploy của cả hai Render
   service. Không push một commit khác trong lúc xử lý sự cố.
2. Xác định cặp backend/frontend deploy gần nhất đã qua post-deploy check và ghi deploy
   ID, commit SHA, thời gian, lý do rollback vào verification. Không chọn hai commit
   không tương thích với nhau.
3. Nếu worker/email có thể làm sự cố nặng hơn, đặt `EMAIL_JOB_WORKER_ENABLED=false`
   trong Render và redeploy cấu hình trước khi rollback code. Không sửa trạng thái
   `email_jobs` thủ công.
4. Từ trang Deploys của từng service, chọn deploy thành công đã biết và dùng
   `Rollback`. Rollback bằng Dashboard tự tắt auto-deploy; nếu dùng API thì phải tự
   tắt auto-deploy trước vì API không làm việc này thay người vận hành.
5. Không rollback Flyway bằng `DROP`, sửa migration cũ hoặc restore thẳng backup vào
   `device_demo`. Migration đã áp dụng giữ nguyên; code rollback chỉ hợp lệ khi tương
   thích schema tiến về trước. Nếu không tương thích, roll forward bằng migration/code
   sửa lỗi đã review.
6. Chạy `scripts/post-deploy-check.sh`, sau đó manual login/password-reset kiểm tra.
   Chỉ bật lại worker và `DEMO_DEPLOY_ENABLED` khi bằng chứng đạt.

Render chỉ giữ một số build artifact gần đây tùy plan. Trước thay đổi rủi ro, ghi
known-good deploy và không dựa vào khả năng rollback vô thời hạn.

## Backup và restore cô lập — PRD-605/607

Aiven có backup managed theo plan, nhưng Gate G6 yêu cầu bằng chứng restore thật.
Runbook này dùng logical dump bổ sung qua `mysqldump`; file chứa dữ liệu nhạy cảm và
phải lưu ngoài Git, quyền 600. Cài MySQL client tương thích trước khi chạy.

Tạo MySQL option file ngoài repository, ví dụ `/duong-dan-rieng/aiven-device.cnf`:

```ini
[client]
host=<aiven-host>
port=<aiven-port>
user=<aiven-user>
password=<aiven-password>
ssl-mode=VERIFY_IDENTITY
ssl-ca=/duong-dan-rieng/aiven-device-demo-ca.pem
```

Sau đó đặt quyền `chmod 600 /duong-dan-rieng/aiven-device.cnf`. Không chép file này,
CA path, mật khẩu hoặc câu lệnh có secret vào repository/chat. Backup chỉ đọc đúng
`device_demo`, từ chối ghi đè và tạo checksum:

```bash
bash scripts/demo-db-backup-restore.sh backup \
  --confirm-demo-readonly \
  --config /duong-dan-rieng/aiven-device.cnf \
  --database device_demo \
  --output /duong-dan-backup/device-demo-YYYYMMDD-HHMM.sql
```

Trước restore, người dùng tự tạo một database rỗng có tên
`device_demo_restore_<suffix>` trên test/demo service đã duyệt. Script không tạo/xóa
database và tuyệt đối không nhận `device_demo` làm target:

```bash
bash scripts/demo-db-backup-restore.sh restore \
  --confirm-isolated-restore \
  --config /duong-dan-rieng/aiven-device.cnf \
  --database device_demo_restore_YYYYMMDD \
  --input /duong-dan-backup/device-demo-YYYYMMDD-HHMM.sql
```

Restore kiểm tra checksum, cấm dump có `CREATE/DROP DATABASE` hoặc `USE`, yêu cầu
target hoàn toàn rỗng và sau đó chỉ in số bảng/Flyway/users/devices. Nếu restore lỗi,
không retry vào database đã restore dở; review rồi chỉ drop/recreate database cô lập.
Sau restore, trỏ một backend tạm hoặc MySQL client vào database cô lập để kiểm tra
login/bản ghi. Không đổi `DB_URL` của demo chính trước khi PRD-607 được xác nhận.

## Kiểm tra cloud sau PRD-606

Người dùng duyệt migration trên `device_demo` trước lần backend deploy đầu: startup
áp Flyway V1-V8 và bootstrap owner khi database trống. Không public owner credential.

1. Readiness `UP`, không lộ component; Actuator index/Prometheus bị chặn.
2. Frontend origin preflight được phép, origin khác bị chặn; API thiếu JWT trả 401.
3. Login và CRUD/devices đúng; reload route đúng; UI hiển thị request ID khi lỗi.
4. Password reset với email allow-list gửi qua Resend HTTPS, link đúng frontend,
   token dùng một lần; Resend log có đúng một delivery cho job thử.
5. Email ngoài allow-list không gọi provider, không lộ tài khoản qua HTTP response và
   job có lỗi vĩnh viễn redacted; đây là giới hạn demo có chủ đích.
6. Rate limit sau proxy không được tin header IP giả từ client; kiểm tra bucket theo
   IP thật và không gom nhầm mọi người dùng.
7. Ghi RAM/CPU/latency/wake-up thật; đo lại timing neutral response với Aiven vì
   latency liên vùng có thể vượt floor local 150 ms.

CI/CD, post-deploy check, rollback và backup/restore helper đã được chuẩn bị ở
PRD-604/605. Deploy/rollback/restore thật và bằng chứng vẫn thuộc PRD-606/607/608/609;
Gate G6 chưa đóng khi chỉ build image hoặc gửi email onboarding thành công. Dashboard
Prometheus/Grafana local không phải bằng chứng monitoring liên tục trên cloud.
