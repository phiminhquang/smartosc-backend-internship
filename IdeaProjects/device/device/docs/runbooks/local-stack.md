# Chạy Device trong môi trường local cô lập

Xem thêm [device-data-scale.md](device-data-scale.md) khi cần sinh dataset tổng
hợp và kiểm tra import/export trên local/Testcontainers.

## Mục đích và ranh giới

Bộ Compose tại `compose.yaml` chỉ dành cho phát triển/kiểm thử local. MySQL dùng volume riêng của Compose, Mailpit giữ email trong môi trường thử, backend chỉ mở cổng trên `127.0.0.1`. Không dùng credentials, URL database hoặc email production trong bộ này.

## Chuẩn bị

1. Cài Docker Engine/Desktop có Docker Compose plugin. Kiểm tra `docker compose version` và `docker info` bằng chính tài khoản sẽ chạy test. Quyền truy cập Docker daemon tương đương quyền quản trị máy; cân nhắc trước khi thêm tài khoản vào nhóm `docker`.
2. Tại thư mục `device/device`, sao chép `.env.example` thành `.env` rồi điền ba giá trị local-only. `.env` đã được Git bỏ qua. Dùng mật khẩu khác production; `LOCAL_JWT_SIGNER_KEY` cần ít nhất 64 byte, có thể tạo bằng `openssl rand -hex 64`.
3. Kiểm tra các giá trị không rỗng và không dán chúng vào log, issue hoặc chat. Đừng đưa `.env` vào Git.

## Khởi động

```bash
docker compose config --quiet
docker compose up --build -d
docker compose ps
```

Compose sẽ đợi MySQL báo healthy trước khi khởi động backend, sau đó đợi backend trước khi khởi động frontend. Flyway chạy V1/V2 khi backend kết nối database mới. Giao diện ở `http://localhost:5173`, API ở `http://localhost:8080`; hộp thư test ở `http://localhost:8025`.

## Phát triển backend bằng IntelliJ

Chế độ này chạy MySQL và Mailpit trong Docker, còn backend chạy trực tiếp trong IntelliJ để dùng breakpoint và hot reload nhanh hơn. Secret vẫn chỉ nằm trong `.env`; run configuration được chia sẻ không chứa mật khẩu hoặc khóa JWT.

1. Chuẩn bị `.env` như phần trên, sau đó chạy:

```bash
bash scripts/dev-ide.sh up
```

2. Trong IntelliJ, chọn run configuration `Device Backend (IDE)` thay cho cấu hình `Unnamed`, rồi bấm Run hoặc Debug. Profile `dev,ide` tự đọc `.env`, kết nối MySQL qua `127.0.0.1:3307` và Mailpit SMTP qua `127.0.0.1:1025`.
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

## Kiểm thử tự động

```bash
bash scripts/verify.sh safe
bash scripts/verify.sh integration
bash scripts/verify.sh frontend-e2e
```

Lệnh thứ hai cần Docker: profile `test` dùng MySQL Testcontainers tạm thời và chạy migration trên database đó. Lệnh thứ ba yêu cầu đủ bốn service Compose đang healthy và chạy live frontend/API/Mailpit smoke. Nếu Docker không có hoặc daemon không chạy, test integration phải thất bại; không đổi sang URL MySQL/Aiven thật để cho test xanh.

## Dừng và chẩn đoán

```bash
docker compose logs --tail=100 backend
docker compose logs --tail=100 mysql
docker compose ps
docker compose down
```

`docker compose down` giữ volume MySQL. Không dùng tùy chọn xóa volume khi chưa backup và xác nhận dữ liệu local không cần giữ. Nếu backend không lên, kiểm tra MySQL healthy, các biến `.env`, rồi log Flyway. Nếu email không tới Mailpit, kiểm tra `mailpit` đang chạy và `MAIL_HOST=mailpit` trong Compose.
