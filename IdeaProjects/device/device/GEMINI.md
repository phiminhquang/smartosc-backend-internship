# Antigravity/Gemini Adapter

`AGENTS.md` là nguồn quy tắc dùng chung của project. Trước khi làm việc:

1. Đọc `PROJECT.md` để hiểu mục tiêu, kiến trúc và nguồn sự thật.
2. Đọc toàn bộ `AGENTS.md` và tuân thủ các quy tắc trong đó.
3. Đọc `AI-HANDOFF.md`, `git status` và diff liên quan.
4. Nếu đang làm một tính năng, đọc các file trong `specs/<feature>/` trước khi đề xuất hoặc sửa code.

## Vai trò mặc định

- Antigravity/Gemini chỉ triển khai frontend trong `frontend/`.
- Đọc API contract trong `specs/<feature>/spec.md` và các dependency trong `tasks.md` trước khi code.
- Không sửa Java, migration, cấu hình backend hoặc backend test nếu người dùng không giao rõ.
- Không tự đổi endpoint hoặc cấu trúc request/response để khớp frontend. Nếu contract thiếu hoặc backend chưa sẵn sàng, báo blocker thay vì tự đoán.
- Được đọc backend và chạy `bash scripts/verify.sh frontend`; chỉ chạy cổng `safe` sau khi phần backend và frontend đã được tích hợp trong cùng working tree.
- Khi làm song song với Codex, dùng branch/worktree riêng và không sửa tài liệu chung; gửi kết quả để Codex cập nhật task, verification và handoff.

Không sao chép quy tắc dài sang file này. Nếu cần thay đổi quy trình dùng chung, cập nhật `AGENTS.md` để Codex, Claude Code và Antigravity nhận cùng một phiên bản.

Lịch sử chat của Gemini/Antigravity không phải trạng thái dùng chung. Mọi quyết định, tiến độ và bằng chứng cần bàn giao phải được ghi vào file phù hợp trong repository.
