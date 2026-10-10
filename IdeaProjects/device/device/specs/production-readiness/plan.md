# Kế hoạch kỹ thuật: Hoàn thiện hệ thống để vận hành, chịu tải và phục hồi sự cố

## Tổng quan

Kế hoạch giữ kiến trúc Spring Boot + React + MySQL hiện tại và cải thiện theo từng cổng nhỏ. Không tách microservice hoặc thêm hạ tầng phức tạp trước khi có số liệu chứng minh nhu cầu.

```text
Giai đoạn 0  Duyệt phạm vi và bảo vệ dữ liệu thật
      |
Giai đoạn 1  Docker local + MySQL/Mailpit test + CI + password reset E2E
      |
Giai đoạn 2  Phân trang + dữ liệu lớn + import/export + index có bằng chứng
      |
Giai đoạn 3  Test request đồng thời và bảo vệ tính đúng đắn
      |
Giai đoạn 4  Email nền có retry và chống gửi trùng
      |
Giai đoạn 5  Log, request ID, health và dashboard vận hành
      |
Giai đoạn 6  Deploy demo + backup/restore + rollback
      |
Giai đoạn 7  Đóng gói bằng chứng cho CV và bàn giao
```

Mỗi giai đoạn chỉ bắt đầu khi gate của giai đoạn trước đã đạt và được ghi trong `verification.md`.

## Kiến trúc mục tiêu cơ sở

```text
React frontend
      |
      | HTTP/JSON + request ID
      v
Spring Boot API
      |
      +-- Security/JWT/rate limit
      +-- Transactional business services
      +-- Paginated repositories
      +-- Durable email-job table
      +-- Background email worker
      +-- Health/metrics/logging
      |
      v
    MySQL

Mailpit/SMTP <--- background email worker
Prometheus/Grafana <--- health and metrics
```

## Quy tắc phối hợp Codex và Antigravity

1. Codex viết hoặc cập nhật API contract trong `spec.md` trước.
2. Codex triển khai backend và backend test thuộc giai đoạn.
3. Codex cập nhật task backend và thông báo contract đã sẵn sàng.
4. Antigravity chỉ sửa `frontend/` theo contract đó.
5. Nếu làm song song, hai phía dùng branch/worktree riêng; không cùng sửa tài liệu chung.
6. Codex thực hiện lượt integration, chạy cổng kiểm tra và cập nhật `verification.md`/`AI-HANDOFF.md`.
7. Người dùng thực hiện kiểm tra thủ công hoặc thao tác bên ngoài được ghi trong task; AI hướng dẫn từng bước nhưng không tự tạo chi phí, nhập secret hoặc phá dữ liệu.

## Giai đoạn 0: Duyệt và tạo baseline

### Codex thực hiện

- Duy trì bộ tài liệu này và bản đồ dependency.
- Ghi danh sách API trả collection, truy vấn lớn, luồng email và điểm truy cập dịch vụ ngoài.
- Ghi baseline của cổng `scripts/verify.sh safe` và trạng thái password reset.
- Bảo vệ test profile khỏi datasource production.

### Antigravity thực hiện

- Kiểm kê API frontend đang dùng và màn hình phụ thuộc response hiện tại.
- Không implement cho tới khi contract liên quan được chốt.

### Người dùng thực hiện với hướng dẫn

- Duyệt spec, plan, nhiệm vụ và contract phân trang đề xuất.

### Gate

- Spec chuyển `Approved`; task và owner không còn mơ hồ; không có test được phép chạm database thật.

## Giai đoạn 1: Môi trường cô lập và password reset E2E

### Codex thực hiện

- Dockerfile backend và Compose gốc cho backend/MySQL/Mailpit.
- Profile `dev`, `test`, `demo` không chứa secret thật.
- Testcontainers hoặc giải pháp tương đương tạo MySQL tạm cho integration test.
- Migration test từ schema trống qua V1/V2.
- Backend integration/E2E cho password reset và failure path áp dụng.
- GitHub Actions cho backend, migration và cổng an toàn.

### Antigravity thực hiện

- Dockerfile frontend và cấu hình backend URL theo môi trường.
- Kiểm tra lint/build trong container hoặc CI.
- Browser test cho login/forgot/reset và bảo vệ token URL/referrer.

### Người dùng thực hiện với hướng dẫn

- Cài/chạy Docker, mở Mailpit, kiểm tra email và luồng trình duyệt thật.
- Xác nhận không dùng Aiven trong toàn bộ lần chạy.

### Gate

- Máy sạch chạy được project bằng quy trình đã ghi; password reset E2E đạt; CI xanh; kết quả được ghi thật.

## Giai đoạn 2: Database lớn, phân trang và file lớn

### Codex thực hiện

- Chốt contract phân trang cho users, assignments, repairs và extension requests.
- Validate `page`, `size`, sort/filter allow-list; giới hạn size theo spec đã duyệt.
- Thay repository/service/controller trả collection lớn bằng page/slice phù hợp.
- Đổi export/import sang xử lý theo nhóm hoặc công việc nền theo kết quả đo.
- Tạo dữ liệu thử tăng dần; không dùng dữ liệu production.
- Đo câu truy vấn, xem kế hoạch chạy và thêm migration index tối thiểu có bằng chứng.
- Kiểm tra dashboard và scheduler trên tập dữ liệu lớn.

### Antigravity thực hiện

- Cập nhật type/service/component đang phụ thuộc API đổi contract.
- Thêm điều khiển trang, trạng thái loading/empty/error cho màn hình thực sự thuộc phạm vi đã duyệt.
- Không tự tạo màn hình quản trị mới nếu chưa có spec sản phẩm.

### Người dùng thực hiện với hướng dẫn

- Chạy kịch bản dữ liệu lớn trên máy đã ghi cấu hình.
- Lưu ảnh/kết quả trước và sau; học cách giải thích index và phân trang.

### Gate

- Không còn collection không giới hạn trong phạm vi; file lớn không bắt buộc nằm toàn bộ trong heap; có báo cáo đo lặp lại được.

### Kết quả quyết định PRD-209

- Thêm duy nhất Flyway V3 `CREATE INDEX idx_devices_name ON devices (name)` vì benchmark 100k chứng minh trang đầu cải thiện rõ và `EXPLAIN ANALYZE` dùng index.
- Keyword giữ `ORDER BY LOWER(name), id` để leading-wildcard không bị optimizer kéo qua ordered B-tree scan; integration test bảo vệ thứ tự mixed-case và count query.
- Không thêm deferred join/full-text ở Giai đoạn 2: deep offset không cải thiện trong phép đo và keyword vẫn nằm ngoài khả năng seek của B-tree thường.
- Trước khi migration áp dụng có thể rollback branch/code. Sau khi V3 đã áp dụng, rollback schema cần migration/quy trình đã duyệt với `DROP INDEX idx_devices_name ON devices`; không sửa hoặc xóa V3 đã phát hành.

## Giai đoạn 3: Tính đúng đắn khi thao tác đồng thời

### Codex thực hiện

- Viết MySQL integration test chạy request đồng thời cho assign, return, extension review và repair completion.
- Kiểm chứng `@Version`, pessimistic lock và transaction boundary hiện tại thay vì giả định chúng đủ.
- Chuẩn hóa lỗi xung đột thành contract HTTP phù hợp, dự kiến `409` sau khi duyệt.
- Thêm cơ chế chống lặp cho endpoint có nguy cơ tạo kết quả trùng nếu bằng chứng cho thấy cần.

### Antigravity thực hiện

- Chặn submit lặp khi request đang chạy.
- Hiển thị lỗi xung đột dễ hiểu và tải lại dữ liệu mới.

### Người dùng thực hiện với hướng dẫn

- Chạy kịch bản hai request cùng lúc và kiểm tra database sau test.

### Gate

- Test lặp lại chứng minh chỉ trạng thái hợp lệ được commit; frontend không báo thành công giả.

## Giai đoạn 4: Email nền đáng tin cậy và rate limit

### Codex thực hiện

- Thiết kế ADR cho job/outbox email và vòng đời retry/idempotency.
- Thêm migration bảng công việc email bằng thay đổi cộng thêm.
- Ghi công việc email cùng transaction với nghiệp vụ liên quan.
- Worker claim theo batch, timeout/retry có giới hạn, chống gửi trùng và lưu lỗi đã lọc dữ liệu nhạy cảm.
- Chuyển password reset, reminder, overdue và daily report sang luồng mới theo từng bước.
- Rate limit login/password reset bằng storage dùng chung phù hợp với kiểu deploy đã duyệt; không dùng map trong bộ nhớ làm giải pháp production.

### Antigravity thực hiện

- Giữ response password reset trung lập và không chờ trạng thái gửi email.
- Cập nhật UX nếu contract trạng thái nhận yêu cầu thay đổi.

### Người dùng thực hiện với hướng dẫn

- Tắt/bật SMTP test, quan sát job chờ, retry và xác nhận không có email trùng.

### Gate

- SMTP hỏng không làm mất job; bật lại thì job hoàn tất đúng một lần; PR-207/208/209 của password reset có bằng chứng.

## Giai đoạn 5: Quan sát và chẩn đoán

### Codex thực hiện

- Thay `System.out` bằng structured logging; thêm request/job ID và lọc secret.
- Chuẩn hóa response lỗi có mã hỗ trợ nhưng không lộ chi tiết nội bộ.
- Thêm Actuator, metrics và Docker Compose profile cho Prometheus/Grafana hoặc stack được duyệt.
- Theo dõi request latency/error, connection pool, scheduler, queue email và tài nguyên JVM.
- Bảo vệ management endpoints và viết runbook tìm lỗi.

### Antigravity thực hiện

- Hiển thị mã hỗ trợ trong lỗi không xác định; không hiển thị stack trace/token.

### Người dùng thực hiện với hướng dẫn

- Tạo một lỗi test, tìm log bằng request ID và lưu ảnh dashboard.

### Gate

- Có thể đi từ thông báo lỗi tới log/metric liên quan; dashboard không public trái phép.

## Giai đoạn 6: Deploy demo, backup/restore và rollback

### Codex thực hiện

- Chuẩn bị artifact/container, health check, quy trình deploy và kiểm tra sau deploy.
- Viết script/runbook backup/restore an toàn cho test/demo; không tự chạy thao tác phá hủy production.
- Viết rollback code/config và chiến lược migration tiến về trước khi schema đã áp dụng.
- Chuẩn bị CI/CD sau khi người dùng chọn provider.

### Antigravity thực hiện

- Xác minh production build, routing reload, backend URL và trạng thái lỗi trên demo.

### Người dùng thực hiện với hướng dẫn

- Chọn provider/ngân sách, tạo tài khoản/database demo, nhập secret và duyệt migration.
- Thực hành backup sang nơi an toàn, restore vào database cô lập và kiểm tra dữ liệu.
- Kiểm tra demo bằng trình duyệt.

### Gate

- Demo truy cập được; deploy qua cổng kiểm tra; restore và rollback có bằng chứng thật.

## Giai đoạn 7: Đóng gói và bàn giao

### Codex thực hiện

- Cập nhật PROJECT/README/diagram/runbook/ADR/verification/handoff.
- Tổng hợp số liệu thật, giới hạn thật và quyết định kỹ thuật để dùng trong CV/phỏng vấn.
- Review diff và bảo mật trước commit/merge.

### Antigravity thực hiện

- Kiểm tra trải nghiệm demo, responsive áp dụng và cung cấp ảnh màn hình frontend cho lượt integration.

### Người dùng thực hiện với hướng dẫn

- Quay demo, duyệt mô tả CV và luyện giải thích database lớn, request đồng thời, email lỗi và restore.

### Gate

- Mọi claim có đường dẫn tới code hoặc bằng chứng; feature chuyển `Done`.

## Thành phần thay đổi dự kiến

| Thành phần | Thay đổi | Owner |
|---|---|---|
| `src/main/` | Phân trang, transaction/concurrency, email jobs, logging/metrics | Codex |
| `src/test/` | Integration, migration, concurrency và failure test | Codex |
| `src/main/resources/db/migration/` | Migration cộng thêm cho index/job/rate-limit nếu áp dụng | Codex |
| `pom.xml` | Dependency test/actuator/metrics tối thiểu | Codex |
| Root Docker/CI/scripts | Compose, CI, data generator, runbook helper | Codex/Integration |
| `frontend/` | Container, contract phân trang, lỗi xung đột/request ID, E2E | Antigravity |
| `specs/`, `docs/`, `AI-HANDOFF.md` | Contract, ADR, task, verification, bàn giao | Codex/Integration |
| Cloud/demo/secret | Tài khoản, chi phí, secret, thao tác có tác động ngoài | Người dùng với Codex hướng dẫn |

## Hợp đồng và dữ liệu

- Contract cụ thể của từng API sẽ được bổ sung vào `spec.md` trước task backend tương ứng và trước khi Antigravity implement.
- Contract phân trang dự kiến dùng zero-based page, mặc định 20 và tối đa 100; cần người dùng duyệt.
- Thay đổi schema luôn qua migration mới; không sửa V1/V2 đã áp dụng.
- Job email phải tham chiếu dữ liệu tối thiểu cần thiết; thiết kế password-reset payload phải được security review để không lưu/log raw token lâu hơn cần thiết.
- Khả năng tương thích ngược được đánh giá theo từng endpoint; nếu không giữ được, backend và frontend phải phát hành phối hợp.

## Bảo mật và quyền riêng tư

- Không ghi credential, JWT, mật khẩu, raw reset token hoặc nội dung nhạy cảm vào Git/log/metrics.
- Test và data generator không được đọc production.
- Management endpoints chỉ mở nội bộ hoặc yêu cầu quyền phù hợp.
- Rate limit không được tiết lộ email có tồn tại.
- File import phải kiểm tra loại, kích thước và nội dung; export phải tuân quyền backend.

## Rủi ro

| Rủi ro | Tác động | Biện pháp |
|---|---|---|
| Test chạm Aiven | Mất hoặc sửa dữ liệu thật | Profile cô lập, fail-fast, Testcontainers và review datasource trước full test |
| Đổi collection sang page làm vỡ frontend | UI lỗi | Chốt contract, giao theo dependency, integration cùng giai đoạn |
| Thêm index không đúng | Ghi chậm, tốn dung lượng | Đo query plan trước/sau, thêm tối thiểu qua migration |
| Worker gửi trùng | Người dùng nhận nhiều email | Claim atomically, idempotency key, test crash/retry |
| Log làm lộ token | Rủi ro bảo mật | Filter/redaction, security test và review log |
| Kế hoạch quá rộng | Dang dở, khó chứng minh | Gate theo giai đoạn; không bắt đầu phase mới khi phase cũ chưa có verification |
| Hai AI xung đột file | Mất thay đổi | Owner rõ, worktree riêng khi song song, một lượt integration |
| Cloud phát sinh chi phí | Chi phí ngoài ý muốn | Người dùng chọn provider và duyệt trước mọi thay đổi bên ngoài |

## Kiểm chứng

- Unit test cho rule cục bộ.
- Integration test với MySQL cô lập cho JPA/Flyway/transaction/concurrency.
- Failure test tắt SMTP, lỗi job, restart worker và lỗi datasource có kiểm soát.
- Test dữ liệu lớn ghi rõ dataset, máy, lệnh và kết quả trước/sau.
- Frontend lint/build và browser E2E cho contract bị ảnh hưởng.
- Manual drill do người dùng thực hiện với checklist cho email, request ID, backup/restore và demo.
- Cổng mặc định `bash scripts/verify.sh safe`; full test chỉ chạy sau khi datasource cô lập được xác nhận.

## Rollback

- Code/container quay về image/commit đã kiểm chứng trước.
- Cấu hình có phiên bản và secret nằm ngoài Git.
- Migration đã áp dụng không bị sửa hoặc rollback bằng cách xóa bừa; sửa bằng migration tiến về trước đã review.
- Trước migration có rủi ro, backup và thử restore trên môi trường cô lập.
- API breaking change chỉ phát hành khi frontend tương thích sẵn sàng hoặc có chiến lược chuyển tiếp được ghi rõ.
