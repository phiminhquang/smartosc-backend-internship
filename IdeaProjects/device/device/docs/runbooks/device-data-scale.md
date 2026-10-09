# Runbook dữ liệu lớn cho thiết bị

Runbook này phục vụ PRD-207/208 trên local hoặc test cô lập. Không dùng với
database production/Aiven và không ghi credential vào lệnh, log hoặc tài liệu.

## Sinh CSV tổng hợp

Generator chỉ ghi file CSV; nó không đọc `.env`, không kết nối mạng và không
truy cập database.

```bash
bash scripts/generate-device-csv.sh \
  --rows 10000 \
  --output /tmp/device-scale-10000.csv
```

- `--rows` hợp lệ từ `1` đến `1000000`.
- Thư mục cha của `--output` phải tồn tại.
- Generator từ chối ghi đè. Chỉ dùng `--force` khi đã kiểm tra đúng file đích.
- Output có header `category,name,model,description` và dữ liệu xác định, nên có
  thể tạo lại cùng quy mô để so sánh các lần đo.

Kiểm tra nhanh mà không in dữ liệu nhạy cảm:

```bash
wc -l /tmp/device-scale-10000.csv
head -n 2 /tmp/device-scale-10000.csv
```

File 10.000 row phải có 10.001 dòng gồm header. Không commit dataset sinh ra.

## Giới hạn import/export hiện tại

- Import CSV tối đa `10 MiB`, xử lý tuần tự và flush/clear mỗi `100` row.
- Import giữ tính nguyên tử: một row lỗi làm rollback toàn bộ file.
- CSV/XLSX export đọc database theo keyset batch `500` bản ghi.
- XLSX dùng cửa sổ `100` row trên bộ nhớ và file tạm của Apache POI.
- Các giá trị trên là cấu hình kiểm thử ban đầu, chưa phải tuyên bố năng lực
  production. Chỉ thay đổi sau khi có bằng chứng PRD-208.

## Quy tắc chạy benchmark

1. Xác nhận datasource chỉ tới MySQL local/Testcontainers. Dừng nếu URL chứa
   host cloud hoặc không thể xác định môi trường.
2. Ghi CPU, RAM, Java/MySQL version, commit và số row trước khi chạy.
3. Tăng dataset theo bước `1.000`, `10.000`, `100.000` trong giới hạn file.
4. Warm-up trước, chạy lặp lại và ghi toàn bộ kết quả thay vì chọn số đẹp nhất.
5. Không thêm index trước khi lưu query plan và baseline của cùng dataset.

## Chạy baseline cô lập

Lệnh mặc định tạo ba dataset tạm, chạy duy nhất `DeviceScaleBenchmarkIT` với
heap tối đa 512 MiB và MySQL Testcontainers, rồi ghi report Markdown:

```bash
bash scripts/benchmark-device-scale.sh --confirm-isolated
```

Report mặc định: `target/benchmarks/device-scale-baseline.md`. Script từ chối
ghi đè; chỉ thêm `--force` sau khi đã lưu report cần giữ. Có thể chạy smoke nhỏ:

```bash
bash scripts/benchmark-device-scale.sh \
  --confirm-isolated \
  --datasets 1000 \
  --repetitions 1 \
  --import-repetitions 1 \
  --output /tmp/device-scale-smoke.md
```

- Script bỏ các biến `DB_*` và `SPRING_DATASOURCE_*`; test tiếp tục xác nhận URL
  cấu hình bắt đầu bằng `jdbc:tc:mysql:` và database là `device_test`.
- Mặc định read/export warm-up một lần và ghi ba lần; import warm-up hạ tầng
  bằng dataset nhỏ nhất rồi ghi một lần mỗi mức vì chi phí cao và làm thay đổi
  dữ liệu cô lập. Report nêu rõ số lần đo, không trình bày một lần import như
  median nhiều mẫu.
- Peak heap lấy từ JVM và RSS lấy từ `/proc/self/status` mỗi 10 ms. Số đo này
  phụ thuộc máy, container cache và sampler; không phải cam kết production.
- Report có raw result, median read/export và `EXPLAIN ANALYZE` trước index để
  PRD-209 so sánh cùng môi trường.
- Dataset tạm bị xóa khi script kết thúc. Generator vẫn không tự import và
  benchmark không đọc `.env`.

Baseline đã ghi ngày 2026-10-09 nằm tại
[`docs/benchmarks/device-scale-baseline-2026-10-09.md`](../benchmarks/device-scale-baseline-2026-10-09.md).
Không dùng số liệu này như cam kết production; PRD-209 phải so sánh trên cùng
môi trường và dataset trước khi quyết định index.
