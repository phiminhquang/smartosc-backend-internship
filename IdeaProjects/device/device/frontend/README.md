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

## Nguồn yêu cầu

- Kiến trúc và quy tắc chung: `../PROJECT.md` và `../AGENTS.md`.
- API contract của feature đang hoạt động: `../specs/<feature>/spec.md`.
- Bằng chứng lint/build/E2E: `../specs/<feature>/verification.md`.

Không ghi API key, mật khẩu, token thật hoặc URL chứa reset token vào README, source code hay Git.
