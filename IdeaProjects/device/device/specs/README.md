# Quy trình đặc tả

Thư mục `specs/` lưu yêu cầu, thiết kế, công việc và bằng chứng kiểm tra của từng thay đổi. Đây là giao diện bàn giao giữa con người, Codex, Claude Code và Antigravity.

## Chọn mức tài liệu

Mọi thay đổi dùng cùng một quy trình nhưng độ sâu khác nhau.

### Mức S — thay đổi nhỏ

Áp dụng khi thay đổi cục bộ, không đổi API/database/bảo mật và rủi ro thấp.

- Dùng một file `specs/<feature>/spec.md`.
- File phải có mục tiêu, phạm vi, thiết kế ngắn, task, tiêu chí chấp nhận và kết quả kiểm tra.
- Không tạo thêm tài liệu chỉ để đủ số lượng file.

### Mức M — tính năng vừa

Áp dụng khi thay đổi nhiều file hoặc có phối hợp frontend/backend nhưng không có quyết định kiến trúc lớn.

- Dùng `spec.md`, `plan.md`, `tasks.md` và `verification.md`.
- Có review phạm vi trước khi implement.
- Ghi rõ rủi ro và trường hợp lỗi quan trọng.

### Mức L — thay đổi lớn hoặc nhạy cảm

Áp dụng khi liên quan database, migration, xác thực, phân quyền, dữ liệu nhạy cảm, tích hợp ngoài hoặc nhiều giai đoạn phát hành.

- Dùng đủ bộ tài liệu mức M.
- Thêm ADR trong `docs/decisions/` cho quyết định có ảnh hưởng lâu dài.
- Có kế hoạch migration/rollback và kiểm tra staging.
- Không phát hành nếu chưa có bằng chứng cho các cổng chất lượng áp dụng.

## Vòng đời

```text
Draft -> Reviewed -> Approved -> Implementing -> Verifying -> Done
```

- `Draft`: đang làm rõ yêu cầu, chưa được phép implement ngoài khảo sát/prototype an toàn.
- `Reviewed`: đã được phản biện nhưng còn điểm cần quyết định.
- `Approved`: phạm vi và tiêu chí chấp nhận đã được người dùng duyệt.
- `Implementing`: code đang được thay đổi theo plan.
- `Verifying`: implementation đã có, đang chạy kiểm tra và xử lý lỗi.
- `Done`: tiêu chí chấp nhận áp dụng đã đạt, verification và handoff đã cập nhật.

Nếu phạm vi thay đổi đáng kể sau khi Approved, cập nhật spec/plan và yêu cầu duyệt lại phần thay đổi trước khi tiếp tục.

## Cách tạo feature mới

1. Đặt tên thư mục bằng kebab-case, ví dụ `device-import`.
2. Chọn mức S, M hoặc L dựa trên rủi ro, không dựa riêng vào số dòng code.
3. Sao chép file cần thiết từ `specs/_template/`.
4. Điền dữ liệu thật; xóa hướng dẫn hoặc mục không áp dụng.
5. Liên kết ADR nếu có quyết định lâu dài.
6. Ghi feature đang hoạt động vào `AI-HANDOFF.md`.

## Quy tắc phối hợp

- Phân công code mặc định: Codex/GPT sở hữu backend; Antigravity/Gemini sở hữu frontend. Ngoại lệ phải do người dùng giao rõ hoặc được ghi trong task.
- API contract trong `spec.md` là giao diện chính thức giữa hai phía. Thay đổi contract phải được ghi trước khi bên phụ thuộc tiếp tục triển khai.
- Mỗi task phải có owner `Codex`, `Antigravity`, `Integration` hoặc người được chỉ định và ghi dependency nếu có.
- Một thời điểm chỉ một agent chịu trách nhiệm sửa cùng một file.
- Nếu các agent chạy song song, dùng branch/worktree riêng; không cùng sửa tài liệu chung. Lượt integration chịu trách nhiệm ghép code, cập nhật tài liệu và chạy cổng kiểm tra đầy đủ.
- Agent phản biện có thể để nhận xét nhưng không tự mở rộng phạm vi đã duyệt.
- Trạng thái task chỉ nằm trong `tasks.md`; không duy trì danh sách cạnh tranh trong chat.
- Bằng chứng kiểm tra chỉ nằm trong `verification.md`; không đánh dấu thành công dựa trên dự đoán.
- `AI-HANDOFF.md` chỉ tóm tắt và liên kết, không sao chép toàn bộ spec.

## Định nghĩa hoàn thành

Một feature được đánh dấu Done khi:

- Các tiêu chí chấp nhận áp dụng đã đạt.
- Task bắt buộc đã hoàn thành hoặc phần hoãn có lý do rõ ràng.
- Test/lint/build/migration cần thiết có bằng chứng thật.
- Không còn lỗi bảo mật hoặc dữ liệu đã biết trong phạm vi.
- Spec, ADR, verification và handoff phản ánh đúng code hiện tại.
- Diff đã được review trước khi commit hoặc merge.
