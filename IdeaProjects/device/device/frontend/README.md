# Device Management Frontend

Giao diện React/TypeScript cho hệ thống Device Management. Frontend cung cấp đăng nhập, dashboard được bảo vệ và luồng yêu cầu/xác nhận khôi phục mật khẩu.

## Công nghệ

- React 19 và TypeScript.
- Vite cho development server và production build.
- React Router cho điều hướng.
- Axios cho REST API.
- Oxlint cho kiểm tra tĩnh.

## Yêu cầu

- Node.js và npm.
- Backend Device chạy tại `http://localhost:8080` khi phát triển local.

## Chạy local

```bash
npm install
npm run dev
```

Vite phục vụ frontend tại `http://localhost:5173`. Mọi request bắt đầu bằng `/api` được proxy tới backend local ở cổng `8080` theo `vite.config.ts`.

## Lệnh chính

```bash
npm run dev      # development server
npm run lint     # Oxlint
npm run build    # TypeScript check và Vite production build
npm run preview  # xem thử production build
```

Từ thư mục gốc project có thể dùng cổng chuẩn hóa:

```bash
bash scripts/verify.sh frontend
```

## Route hiện tại

- `/login`: đăng nhập.
- `/forgot-password`: yêu cầu email khôi phục mật khẩu.
- `/reset-password`: xác nhận token và mật khẩu mới.
- `/`: dashboard, yêu cầu access token hợp lệ.

## Cấu trúc chính

- `src/pages/`: các trang và luồng giao diện.
- `src/components/`: component dùng lại và route protection.
- `src/services/`: HTTP client và lời gọi API.
- `src/auth/`: trạng thái/xử lý xác thực.
- `src/types/`: kiểu dữ liệu API và domain.

HTTP client dùng base URL `/api`. Access token được giữ trong `sessionStorage` với khóa `device_access_token` và tự gắn vào header `Authorization`.

## Triển khai Render Static Site (PRD-603)

Khi triển khai frontend dưới dạng **Render Static Site**:

1. **Thông số cài đặt trên Render Dashboard**:
   - **Root Directory**: `IdeaProjects/device/device/frontend` (đường dẫn từ Git repository root).
   - **Build Command**: `npm ci && npm run build`
   - **Publish Directory**: `dist`
2. **SPA Rewrite (Bắt buộc cho React Router client-side routing)**:
   - Trong mục **Redirects/Rewrites**:
     - **Source**: `/*`
     - **Destination**: `/index.html`
     - **Action**: `Rewrite`
3. **Security Headers**:
   - `X-Content-Type-Options: nosniff`
   - `X-Frame-Options: DENY`
   - `Referrer-Policy: no-referrer`
4. **Biến môi trường (Build-time Environment Variable)**:
   - `VITE_API_BASE_URL`: Địa chỉ Backend REST API.
   - Bước 1: Khi tạo Static Site lần đầu để lấy hostname frontend thật, đặt `VITE_API_BASE_URL=https://placeholder.invalid/api`.
   - Bước 2: Sau khi Backend Render Web Service deploy xong và có hostname thật, cập nhật `VITE_API_BASE_URL=https://<backend-host>.onrender.com/api` và trigger redeploy frontend.
5. **Cấu hình Infrastructure as Code (`render.yaml`)**:
   - File `render.yaml` trong thư mục `frontend/` đã chuẩn hóa `type: web`, `runtime: static`, monorepo `rootDir`, routes, headers và envVars.
   - Khi tạo Blueprint, chọn Blueprint Path `IdeaProjects/device/device/frontend/render.yaml` vì file không nằm ở Git repository root.
   - `autoDeployTrigger: off` tránh deploy trước khi CI đạt; workflow chỉ bật deploy demo sau khi người dùng cấu hình environment `demo` và biến `DEMO_DEPLOY_ENABLED=true`.

## Nguồn yêu cầu

- Kiến trúc và quy tắc chung: `../PROJECT.md` và `../AGENTS.md`.
- Runbook triển khai demo: `../docs/runbooks/free-demo-deployment.md`.
- API contract của feature đang hoạt động: `../specs/<feature>/spec.md`.
- Bằng chứng lint/build/E2E: `../specs/<feature>/verification.md`.

Không ghi API key, mật khẩu, token thật hoặc URL chứa reset token vào README, source code hay Git.
