# Antigravity/Gemini Adapter

`AGENTS.md` là nguồn quy tắc dùng chung của project. Trước khi làm việc:

1. Đọc `PROJECT.md` để hiểu mục tiêu, kiến trúc và nguồn sự thật.
2. Đọc toàn bộ `AGENTS.md` và tuân thủ các quy tắc trong đó.
3. Đọc `AI-HANDOFF.md`, `git status` và diff liên quan.
4. Nếu đang làm một tính năng, đọc các file trong `specs/<feature>/` trước khi đề xuất hoặc sửa code.

## Vai trò mặc định

- Phạm vi, owner, dependency và quy tắc làm song song được định nghĩa duy nhất trong phần `Phân công code mặc định` của `AGENTS.md` và `tasks.md` của feature đang hoạt động.
- Với phần frontend, dùng `bash scripts/verify.sh frontend`; chỉ dùng cổng `safe` sau khi các phần liên quan đã được tích hợp trong cùng working tree.
- Nếu API contract hoặc dependency chưa sẵn sàng, ghi blocker theo `AGENTS.md` thay vì tự thay đổi phần thuộc owner khác.

Không sao chép quy tắc dài sang file này. Nếu cần thay đổi quy trình dùng chung, cập nhật `AGENTS.md` để Codex, Claude Code và Antigravity nhận cùng một phiên bản.

Lịch sử chat của Gemini/Antigravity không phải trạng thái dùng chung. Mọi quyết định, tiến độ và bằng chứng cần bàn giao phải được ghi vào file phù hợp trong repository.
