# Password-reset timing comparison

## Mục tiêu và môi trường

- Mục tiêu: PR-209, so sánh nhánh email tồn tại và không tồn tại sau PR-207/208.
- Lệnh: `bash scripts/benchmark-password-reset-timing.sh --confirm-isolated --samples 50 --warmups 10 --output target/benchmarks/password-reset-timing-prd-410.md --force`.
- Phạm vi: MockMvc → controller → shared MySQL rate limit → password-reset service/outbox.
- Database: MySQL 8.4.11 Testcontainers `device_test` dùng một lần, Flyway V1-V7; không dùng Compose DB, Aiven hoặc production.
- Runtime: OpenJDK 17.0.20.1, Linux Mint 22.3, working tree có thay đổi chưa commit.
- Mỗi request dùng một email test riêng; 10 cặp warm-up và 50 cặp ghi nhận, xen kẽ existing-first/missing-first để giảm drift.
- Mọi mẫu của hai nhánh phải trả cùng HTTP 202 và cùng body.

Ngưỡng phát hiện chênh lệch rõ ràng được chốt trong benchmark trước khi chạy: median lệch không quá 10 ms, tỷ lệ median không quá 2,0 và xác suất một mẫu existing chậm hơn một mẫu missing không quá 0,80.

## Baseline trước sửa

| Nhánh | Mean (ms) | Median (ms) | P95 (ms) | Min (ms) | Max (ms) |
|---|---:|---:|---:|---:|---:|
| Email tồn tại | 84,511 | 86,052 | 107,503 | 55,039 | 117,192 |
| Email không tồn tại | 46,640 | 48,330 | 58,717 | 30,164 | 66,458 |

- Chênh lệch median: 37,722 ms.
- Tỷ lệ median: 1,780.
- Xác suất mẫu existing chậm hơn missing: 0,990.
- Kết luận: **KHÔNG ĐẠT**; HTTP/body trung lập nhưng thời gian còn phân biệt rõ.

## Thay đổi

- Thêm `PasswordResetTimingGuard` tại request endpoint sau khi rate limit cho phép và sau khi transaction nghiệp vụ hoàn tất.
- Mặc định phản hồi hợp lệ được giữ trong cửa sổ tối thiểu 150 ms cộng jitter mật mã ngẫu nhiên 0-25 ms; có thể cấu hình bằng `PASSWORD_RESET_RESPONSE_MIN_DURATION` và `PASSWORD_RESET_RESPONSE_JITTER`.
- Request vượt rate limit vẫn trả HTTP 429 trước account lookup và không chờ cửa sổ trung lập.
- Mỗi duration bị giới hạn từ 0 đến 5 giây để fail-fast khi cấu hình sai. Rate limit theo IP/identity tiếp tục giới hạn khả năng lấy mẫu và giảm tác động giữ request thread.

## Kết quả sau sửa

| Nhánh | Mean (ms) | Median (ms) | P95 (ms) | Min (ms) | Max (ms) |
|---|---:|---:|---:|---:|---:|
| Email tồn tại | 173,675 | 175,697 | 183,879 | 159,495 | 185,221 |
| Email không tồn tại | 172,038 | 170,486 | 183,130 | 158,437 | 184,545 |

- Chênh lệch median: 5,210 ms.
- Tỷ lệ median: 1,031.
- Xác suất mẫu existing chậm hơn missing: 0,560.
- Cùng một HTTP status và body cho toàn bộ 100 request ghi nhận.
- Benchmark: 1/1 test đạt, `BUILD SUCCESS` trong 1 phút 23 giây.
- Kết luận: **ĐẠT** theo ngưỡng đã chốt.

## Giới hạn diễn giải

Kết quả này phát hiện và loại bỏ chênh lệch rõ ràng trong môi trường local ít nhiễu; không chứng minh hai phân phối thời gian giống nhau về mặt mật mã. Cửa sổ phản hồi phải được đo lại trên hạ tầng demo/production nếu latency database vượt ngưỡng 150 ms. Shared rate limit vẫn là lớp bảo vệ bắt buộc, không được tắt để dựa riêng vào jitter.
