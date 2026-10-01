# Architecture Decision Records

Thư mục này lưu các quyết định kỹ thuật có ảnh hưởng lâu dài để agent sau hiểu lý do, không chỉ thấy kết quả trong code.

## Khi cần ADR

Tạo ADR khi quyết định liên quan một hoặc nhiều mục:

- Kiến trúc hoặc ranh giới module.
- Database schema/migration có ảnh hưởng lâu dài.
- Xác thực, phân quyền hoặc dữ liệu nhạy cảm.
- Hợp đồng API khó thay đổi.
- Tích hợp dịch vụ ngoài.
- Đánh đổi đáng kể giữa độ đơn giản, hiệu năng, chi phí hoặc độ an toàn.

Không cần ADR cho đổi tên, chỉnh giao diện nhỏ hoặc implementation có thể đảo ngược dễ dàng.

## Quy ước

- Tên file: `NNN-ten-quyet-dinh.md`.
- Số tăng dần và không tái sử dụng.
- Trạng thái: Proposed, Accepted, Superseded hoặc Deprecated.
- Không sửa lịch sử để làm như quyết định cũ chưa từng tồn tại; tạo ADR mới và liên kết `Superseded by`.
- Dùng `000-template.md` làm mẫu.

## Danh mục

- `001-password-reset-token-and-session-revocation.md`: dùng reset token dạng hash và thu hồi JWT bằng token version.
