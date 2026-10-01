# Kiểm chứng: <Tên thay đổi>

## Môi trường

- Ngày giờ:
- Commit/working tree:
- OS/runtime:
- Database/dịch vụ ngoài:

## Cổng chất lượng

| Kiểm tra | Lệnh/kịch bản | Kết quả | Bằng chứng/Ghi chú |
|---|---|---|---|
| Backend compile | `bash ./mvnw -DskipTests compile` | Chưa chạy | |
| Backend test | `bash ./mvnw test` hoặc nhóm an toàn đã xác định | Chưa chạy | |
| Frontend lint | `npm run lint` | Chưa chạy | |
| Frontend build | `npm run build` | Chưa chạy | |
| Diff check | `git diff --check` | Chưa chạy | |

## Ma trận tiêu chí chấp nhận

| ID | Tình huống | Kết quả mong đợi | Kết quả thực tế | Trạng thái |
|---|---|---|---|---|
| V-01 | | | | Chưa chạy |

## Lỗi và giới hạn

- Ghi lỗi thật, nguyên nhân đã biết và tác động.
- Phân biệt failure của code với hạn chế của môi trường test.

## Kết luận

- Sẵn sàng / Chưa sẵn sàng.
- Điều kiện còn thiếu trước khi Done hoặc phát hành.
