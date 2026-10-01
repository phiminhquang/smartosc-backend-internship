# Công việc: <Tên thay đổi>

## Trạng thái tổng thể

- Spec: Draft / Reviewed / Approved.
- Implementation: Chưa bắt đầu / Đang làm / Hoàn thành.
- Verification: Chưa bắt đầu / Đang làm / Hoàn thành.

## Phân công

- Backend owner: Codex/GPT.
- Frontend owner: Antigravity/Gemini.
- Integration/documentation owner: Codex/GPT, trừ khi người dùng chỉ định khác.
- Dependency chính: <Không có / mô tả contract hoặc task phải hoàn thành trước>.

## 1. Đặc tả và thiết kế

- [ ] TASK-001 Xác nhận phạm vi và ngoài phạm vi.
- [ ] TASK-002 Xác nhận tiêu chí chấp nhận.
- [ ] TASK-003 Review plan và rủi ro.

## 2. Implementation

- [ ] TASK-101 `[Owner: Codex]` <Thay đổi backend có thể review độc lập>.
- [ ] TASK-102 `[Owner: Antigravity]` <Thay đổi frontend có thể review độc lập>. Phụ thuộc: <API contract hoặc task backend>.
- [ ] TASK-103 `[Owner: Integration]` <Ghép hai phía và xử lý khác biệt contract>.

## 3. Verification

- [ ] TASK-201 Chạy test áp dụng.
- [ ] TASK-202 Chạy lint/build áp dụng.
- [ ] TASK-203 Kiểm tra migration/integration/E2E áp dụng.
- [ ] TASK-204 Ghi bằng chứng vào `verification.md`.

## 4. Hoàn tất

- [ ] TASK-301 Review diff.
- [ ] TASK-302 Cập nhật ADR/spec/handoff.
- [ ] TASK-303 Commit hoặc merge theo quy trình project.

## Blocker

- Không có / mô tả blocker, chủ sở hữu và điều kiện gỡ chặn.

## Quy tắc

- Chỉ đánh dấu `[x]` khi có implementation hoặc bằng chứng tương ứng.
- Agent chỉ sửa code thuộc phạm vi owner của mình; ngoại lệ cần yêu cầu rõ từ người dùng.
- Nếu dependency chưa đạt hoặc API contract thiếu, ghi blocker thay vì tự sửa phần của agent khác.
- Task phát sinh ngoài phạm vi phải quay lại bước review spec.
