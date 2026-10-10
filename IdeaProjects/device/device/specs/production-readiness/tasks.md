# Công việc: Hoàn thiện hệ thống để vận hành, chịu tải và phục hồi sự cố

## Trạng thái tổng thể

- Spec: Approved ngày 2026-10-08.
- Implementation: Giai đoạn 0/1 và phần kỹ thuật PRD-201 đến PRD-209 đã hoàn tất; PRD-210 chờ người dùng xác nhận trên máy đã ghi.
- Verification: Cổng `safe`, MySQL integration Flyway V1-V3, benchmark 100k, frontend static tests/build, local Compose bốn service, Playwright Chromium E2E và ba job GitHub CI trước PRD-209 đều đạt.
- Feature password reset vẫn là công việc đang Verifying và là dependency của Giai đoạn 1/4.

## Ký hiệu owner và cách làm

- `[Owner: Codex] [AI thực hiện]`: Codex được phép sửa backend/cấu hình/tài liệu trong phạm vi sau khi spec Approved.
- `[Owner: Antigravity] [AI thực hiện]`: Antigravity chỉ sửa `frontend/` sau khi contract/dependency backend sẵn sàng.
- `[Owner: User] [AI hướng dẫn]`: người dùng phải quyết định hoặc thao tác; AI cung cấp checklist/lệnh và kiểm tra kết quả.
- `[Owner: Integration]`: Codex ghép kết quả, chạy cổng kiểm tra và cập nhật tài liệu chung.

## Quy tắc bắt đầu phiên cho cả hai AI

- Đọc `PROJECT.md`, `AGENTS.md`, `AI-HANDOFF.md` và toàn bộ `specs/production-readiness/`.
- Đọc `git status` và diff liên quan; không ghi đè thay đổi có sẵn.
- Chỉ nhận task có đúng owner và mọi dependency đã đạt.
- Không sửa cùng file/cùng working tree khi làm song song; dùng branch/worktree riêng.
- Antigravity không sửa file chung; gửi kết quả để Codex tích hợp hoặc làm trên worktree riêng.
- Chỉ Codex/Integration cập nhật trạng thái task và verification chung sau khi kiểm tra bằng chứng.

## Giai đoạn 0 — Duyệt và baseline

- [x] PRD-001 `[Owner: Codex] [AI thực hiện]` Khảo sát tài liệu, kiến trúc, API collection, email, test, migration và khoảng trống hạ tầng hiện tại.
- [x] PRD-002 `[Owner: Codex] [AI thực hiện]` Tạo `spec.md`, `plan.md`, `tasks.md`, `verification.md` cho feature mức L.
- [x] PRD-003 `[Owner: Antigravity] [AI thực hiện]` Review contract/frontend dependency hiện tại. Phạm vi xác nhận khi integration: `LoginPage`, `ForgotPasswordPage`, `ResetPasswordPage`, route trong `App.tsx`, `AuthContext`, `authService` và cấu hình `http`.
- [x] PRD-004 `[Owner: User] [AI hướng dẫn]` Duyệt phạm vi, ngoài phạm vi, thứ tự phase và contract phân trang mặc định. Người dùng duyệt ngày 2026-10-08: `page=0`, `size=20`, tối đa `100`.
- [x] PRD-005 `[Owner: Integration]` Chuyển spec sang `Approved`, ghi quyết định đã chốt và cập nhật handoff. Người dùng đã duyệt PRD-004; PRD-003 vẫn là điều kiện trước phần frontend.
- [x] PRD-006 `[Owner: Codex] [AI thực hiện]` Ghi baseline `scripts/verify.sh safe`, xác nhận datasource hiện tại và thiết kế fail-fast bảo vệ production. Phụ thuộc: PRD-005. Xem `verification.md`; test integration chưa chạy.

### Gate G0

- [x] G0-1 Spec đã Approved.
- [x] G0-2 Không còn câu hỏi làm thay đổi đáng kể thiết kế Giai đoạn 1 của Codex; provider demo thuộc Giai đoạn 6.
- [x] G0-3 Test datasource được ép sang `jdbc:tc` bằng test profile, Surefire và `@DynamicPropertySource`; lệnh integration bỏ biến DB môi trường và dừng khi thiếu Docker. Đây là bằng chứng cấu hình; runtime MySQL vẫn phải xác nhận tại G1.

## Giai đoạn 1 — Docker, MySQL/Mailpit test, CI và password reset E2E

- [x] PRD-101 `[Owner: Codex] [AI thực hiện]` Viết Dockerfile backend tối thiểu, non-root nếu stack hỗ trợ và không đóng gói secret. Image build thành công; container chạy bằng `10001:10001`.
- [x] PRD-102 `[Owner: Codex] [AI thực hiện]` Tạo Compose cho backend, MySQL và Mailpit với health check/volume áp dụng. Cả ba service healthy; backend/Mailpit chỉ bind loopback.
- [x] PRD-103 `[Owner: Antigravity] [AI thực hiện]` Viết Dockerfile frontend, Nginx SPA/API proxy và cấu hình `VITE_API_BASE_URL`. Image build thành công; service frontend được tích hợp Compose và healthy trên loopback `5173`.
- [x] PRD-104 `[Owner: Codex] [AI thực hiện]` Tách profile dev/test/demo và thêm kiểm tra fail-fast chống datasource production trong test. Full integration đã đạt trên MySQL Testcontainers cô lập.
- [x] PRD-105 `[Owner: Codex] [AI thực hiện]` Thêm MySQL Testcontainers và migration test V1 -> V2 trên database mới. Full suite đạt 31 test trên MySQL 8.4.11 tạm.
- [x] PRD-106 `[Owner: Codex] [AI thực hiện]` Viết backend integration test password reset với database/email test áp dụng. Test DB đạt; Compose smoke đạt request/email/confirm/reuse/login.
- [x] PRD-107 `[Owner: Antigravity] [AI thực hiện]` Hoàn thiện Playwright Chromium E2E điều khiển DOM thật: login thành công, forgot/reset qua Mailpit, xóa token khỏi URL, kiểm tra `Referer`, login bằng mật khẩu mới, từ chối mật khẩu cũ và khôi phục mật khẩu ban đầu. Codex chạy lại `bash scripts/verify.sh frontend-e2e` thành công ngày 2026-10-09.
- [x] PRD-108 `[Owner: Codex] [AI thực hiện]` Tạo GitHub Actions backend/migration/safe checks tại Git root `.github/workflows/device-ci.yml`; `safe` và `integration` đã chạy xanh trên GitHub cho commit `5837be2`.
- [x] PRD-109 `[Owner: Antigravity] [AI thực hiện]` Frontend contract/security tests, lint/build và Playwright Compose E2E đã được tích hợp vào `scripts/verify.sh` và workflow Device CI; `compose-smoke` đã chạy xanh trên GitHub.
- [x] PRD-110 `[Owner: User] [AI hướng dẫn]` Chạy Compose, mở Mailpit và kiểm tra password reset trên trình duyệt. Người dùng xác nhận đạt ngày 2026-10-09: bốn container healthy, nhận email, mở link, token biến mất khỏi URL, reset/login bằng mật khẩu mới thành công và link cũ bị từ chối.
- [x] PRD-111 `[Owner: Integration]` Ghi kết quả Giai đoạn 1 vào verification và cập nhật task password-reset liên quan.

### Gate G1

- [x] G1-1 GitHub hosted runner checkout sạch, tạo volume MySQL mới và chạy đủ bốn service healthy bằng workflow đã ghi.
- [x] G1-2 Migration và integration test chạy trên MySQL cô lập: 31 test đạt, Flyway V1/V2 đạt.
- [x] G1-3 Playwright Chromium password reset E2E đạt login, Mailpit, reset, URL/referrer và mật khẩu cũ/mới.
- [x] G1-4 GitHub `safe`, `integration` và `compose-smoke` đều đạt trên commit `5837be2`.

## Giai đoạn 2 — Phân trang, dữ liệu lớn và import/export

- [x] PRD-201 `[Owner: Codex] [AI thực hiện]` Bổ sung API contract phân trang/filter/sort cho users, assignments, repairs và extension requests. Contract đã được duyệt ngày 2026-10-09.
- [x] PRD-202 `[Owner: User] [AI hướng dẫn]` Người dùng duyệt breaking response cho tám endpoint và xác nhận chưa tạo màn hình frontend mới ngày 2026-10-09. Phụ thuộc: PRD-201.
- [x] PRD-203 `[Owner: Codex] [AI thực hiện]` Implement `PageResult`, validation/allow-list, deterministic sort và filter ở controller-service-repository; devices dùng chung validation `page/size`.
- [x] PRD-204 `[Owner: Codex] [AI thực hiện]` Integration test MySQL cho page boundary, tám endpoint, filter, sort, giới hạn size và input lỗi; full suite đạt 34/34 test.
- [x] PRD-205 `[Owner: Antigravity] [AI thực hiện]` Đóng N/A: audit xác nhận frontend hiện chỉ gọi devices và không tiêu thụ tám endpoint breaking; phạm vi đã duyệt không tạo màn hình mới nên không sửa `frontend/`.
- [x] PRD-206 `[Owner: Codex] [AI thực hiện]` Export CSV/XLSX stream trực tiếp, đọc MySQL theo keyset batch; import CSV có giới hạn 10 MiB và flush/clear theo batch. Unit test và MySQL integration test đạt.
- [x] PRD-207 `[Owner: Codex] [AI thực hiện]` Generator CSV tổng hợp chỉ ghi filesystem, giới hạn row và từ chối ghi đè mặc định; không đọc `.env`, không có kết nối database/mạng.
- [x] PRD-208 `[Owner: Codex] [AI thực hiện]` Đã thu baseline import/query/export, peak heap/RSS và `EXPLAIN ANALYZE` trên 1k/10k/100k với máy/JVM/MySQL được ghi; report lưu tại `docs/benchmarks/device-scale-baseline-2026-10-09.md`.
- [x] PRD-209 `[Owner: Codex] [AI thực hiện]` Đã đo trước/sau 100k và thêm Flyway V3 `devices(name)`. Median trang đầu giảm 447,520 xuống 50,178 ms; keyword control không regression; deep offset không được tuyên bố đã giải quyết.
- [ ] PRD-210 `[Owner: User] [AI hướng dẫn]` Chạy kịch bản dữ liệu lớn, lưu kết quả/ảnh và xác nhận giới hạn máy.
- [x] PRD-211 `[Owner: Integration]` Local `safe` đạt, integration 42/42 đạt trên MySQL 8.4.11/Flyway V1-V3 và báo cáo trước/sau đã lưu; GitHub CI/PR của branch vẫn cần chạy sau commit.

### Gate G2

- [x] G2-1 Collection lớn trong phạm vi PRD-201 đã phân trang; devices giữ contract hiện tại nhưng có validation chung.
- [x] G2-2 Import/export không bắt buộc nạp toàn bộ dữ liệu lớn vào bộ nhớ; implementation và test xác nhận DB/persistence context/row window đều được giới hạn theo batch.
- [x] G2-3 Index có bằng chứng query plan và đo trước/sau; trang đầu dùng `idx_devices_name`, còn deep offset và keyword vẫn table scan theo giới hạn đã ghi.
- [x] G2-4 Frontend không tiêu thụ tám endpoint breaking và tiếp tục dùng contract devices không đổi.

## Giai đoạn 3 — Request đồng thời và tính đúng đắn dữ liệu

- [ ] PRD-301 `[Owner: Codex] [AI thực hiện]` Viết MySQL concurrent integration test cho hai request cùng assign một device.
- [ ] PRD-302 `[Owner: Codex] [AI thực hiện]` Viết test đồng thời cho return, extension approve/reject và repair transition quan trọng.
- [ ] PRD-303 `[Owner: Codex] [AI thực hiện]` Sửa transaction/locking/constraint tối thiểu theo failure thực tế, không đổi kiến trúc khi chưa cần.
- [ ] PRD-304 `[Owner: Codex] [AI thực hiện]` Chốt error contract xung đột và test HTTP, dự kiến 409 sau khi review.
- [ ] PRD-305 `[Owner: Antigravity] [AI thực hiện]` Chặn submit lặp, hiển thị conflict và refresh dữ liệu. Phụ thuộc: PRD-304.
- [ ] PRD-306 `[Owner: User] [AI hướng dẫn]` Chạy demo hai request đồng thời và kiểm tra trạng thái database.
- [ ] PRD-307 `[Owner: Integration]` Ghi bằng chứng lặp lại và review dữ liệu sau race test.

### Gate G3

- [ ] G3-1 Chỉ kết quả hợp lệ được commit trong mọi scenario đã duyệt.
- [ ] G3-2 Không có active assignment/transition mâu thuẫn do race.
- [ ] G3-3 Frontend không báo thành công giả hoặc gửi lặp không cần thiết.

## Giai đoạn 4 — Email nền và rate limit

- [ ] PRD-401 `[Owner: Codex] [AI thực hiện]` Viết ADR email job/outbox, retry, idempotency và xử lý reset token.
- [ ] PRD-402 `[Owner: User] [AI hướng dẫn]` Duyệt ADR và giới hạn retry/retention trước migration.
- [ ] PRD-403 `[Owner: Codex] [AI thực hiện]` Thêm migration bảng job và repository claim an toàn theo batch.
- [ ] PRD-404 `[Owner: Codex] [AI thực hiện]` Implement worker, retry có giới hạn, failure state và chống gửi trùng.
- [ ] PRD-405 `[Owner: Codex] [AI thực hiện]` Chuyển password reset sang job nền mà không tạo account/timing leak.
- [ ] PRD-406 `[Owner: Codex] [AI thực hiện]` Chuyển reminder, overdue và daily report theo từng bước có test.
- [ ] PRD-407 `[Owner: Codex] [AI thực hiện]` Implement rate limit login/reset với shared storage được duyệt.
- [ ] PRD-408 `[Owner: Antigravity] [AI thực hiện]` Cập nhật UX response nhận yêu cầu nếu contract đổi. Phụ thuộc: PRD-405.
- [ ] PRD-409 `[Owner: User] [AI hướng dẫn]` Tắt/bật SMTP test, quan sát retry và xác nhận không gửi trùng.
- [ ] PRD-410 `[Owner: Integration]` Hoàn tất bằng chứng password-reset PR-207/208/209 và cập nhật hai bộ verification.

### Gate G4

- [ ] G4-1 SMTP lỗi không làm mất job hoặc giữ lock nghiệp vụ trong lúc gọi mạng.
- [ ] G4-2 Job retry và hoàn tất đúng một lần theo contract.
- [ ] G4-3 Rate limit không phụ thuộc email có tồn tại.

## Giai đoạn 5 — Log, health, metrics và chẩn đoán

- [ ] PRD-501 `[Owner: Codex] [AI thực hiện]` Thay `System.out`, thêm structured logging và redaction.
- [ ] PRD-502 `[Owner: Codex] [AI thực hiện]` Thêm request/job ID xuyên suốt response-log-job.
- [ ] PRD-503 `[Owner: Codex] [AI thực hiện]` Thêm Actuator/health và bảo vệ management endpoints.
- [ ] PRD-504 `[Owner: Codex] [AI thực hiện]` Thêm metrics request, datasource, scheduler và email jobs.
- [ ] PRD-505 `[Owner: Codex] [AI thực hiện]` Thêm Compose profile Prometheus/Grafana và dashboard cơ sở.
- [ ] PRD-506 `[Owner: Antigravity] [AI thực hiện]` Hiển thị support/request ID cho lỗi không xác định. Phụ thuộc: PRD-502.
- [ ] PRD-507 `[Owner: Codex] [AI thực hiện]` Viết runbook tìm API chậm, DB connection exhaustion, scheduler/email job kẹt.
- [ ] PRD-508 `[Owner: User] [AI hướng dẫn]` Tạo lỗi test, tìm log bằng ID và lưu ảnh dashboard.
- [ ] PRD-509 `[Owner: Integration]` Security review log/metrics/endpoints và ghi verification.

### Gate G5

- [ ] G5-1 Từ lỗi UI/API tìm được log/metric liên quan.
- [ ] G5-2 Dashboard phản ánh health/latency/error/DB/job cơ bản.
- [ ] G5-3 Không lộ secret/token và management endpoint không public trái phép.

## Giai đoạn 6 — Deploy demo, backup/restore và rollback

- [ ] PRD-601 `[Owner: User] [AI hướng dẫn]` Chọn provider/ngân sách và tạo tài khoản/demo resources; không ghi secret vào chat/repository.
- [ ] PRD-602 `[Owner: Codex] [AI thực hiện]` Chuẩn bị backend/container/deployment config theo provider đã chọn.
- [ ] PRD-603 `[Owner: Antigravity] [AI thực hiện]` Chuẩn bị frontend production deploy/routing/backend URL. Phụ thuộc: PRD-601.
- [ ] PRD-604 `[Owner: Codex] [AI thực hiện]` Viết CI/CD, post-deploy health check và rollback runbook.
- [ ] PRD-605 `[Owner: Codex] [AI thực hiện]` Viết backup/restore runbook/script có guard cho test/demo.
- [ ] PRD-606 `[Owner: User] [AI hướng dẫn]` Nhập secret vào provider, duyệt migration và deploy demo.
- [ ] PRD-607 `[Owner: User] [AI hướng dẫn]` Thực hành backup và restore vào database cô lập; kiểm tra bản ghi và login.
- [ ] PRD-608 `[Owner: User] [AI hướng dẫn]` Kiểm tra demo bằng trình duyệt và email test/demo.
- [ ] PRD-609 `[Owner: Integration]` Ghi kết quả deploy/restore/rollback và giới hạn còn lại.

### Gate G6

- [ ] G6-1 Demo chạy qua quy trình kiểm tra tự động.
- [ ] G6-2 Backup đã được restore thành công, không chỉ được tạo.
- [ ] G6-3 Rollback code/config có kịch bản và bằng chứng áp dụng.

## Giai đoạn 7 — Portfolio và hoàn tất

- [ ] PRD-701 `[Owner: Codex] [AI thực hiện]` Cập nhật kiến trúc, README, runbook, ADR và số liệu thật.
- [ ] PRD-702 `[Owner: Antigravity] [AI thực hiện]` Cung cấp ảnh/giao diện demo đã kiểm tra; không sửa tài liệu chung trực tiếp khi làm song song.
- [ ] PRD-703 `[Owner: User] [AI hướng dẫn]` Quay demo và duyệt claim CV.
- [ ] PRD-704 `[Owner: Codex] [AI hướng dẫn]` Luyện giải thích với người dùng về DB lớn, concurrency, email failure và restore.
- [ ] PRD-705 `[Owner: Integration]` Review diff cuối, chạy toàn bộ cổng áp dụng và cập nhật handoff.
- [ ] PRD-706 `[Owner: User]` Duyệt feature `Done` và cho phép commit/merge/phát hành theo quy trình.

## Blocker hiện tại

- PRD-003, PRD-103, PRD-107 và phần tích hợp CI PRD-109 đã được kiểm tra; Playwright Chromium E2E local đã đạt.
- Docker Engine/Compose và quyền user đã hoạt động; blocker Docker được gỡ. Full stack đạt bốn service healthy trong lần kiểm tra PRD-110.
- Password reset đã có MySQL Testcontainers, full Compose, Playwright E2E, kiểm tra trình duyệt thủ công và GitHub CI xanh; JWT cũ trên Compose và PR-207/208/209 vẫn còn mở.
- PRD-210 cần người dùng chạy/lưu ảnh trên máy đã ghi; local technical gate G2 đã có bằng chứng và commit `37333db`, nhưng branch PRD-209 chưa push/CI/PR do GitHub CLI mất xác thực.
- Chưa chọn provider demo; blocker này chỉ áp dụng Giai đoạn 6, không chặn local/test.

## Quy tắc cập nhật

- Chỉ đánh dấu `[x]` khi có implementation hoặc bằng chứng tương ứng.
- Task `[Owner: User]` không được AI tự đánh dấu hoàn thành nếu người dùng chưa xác nhận kết quả.
- Agent chỉ sửa code thuộc owner; ngoại lệ cần yêu cầu rõ từ người dùng.
- Dependency chưa đạt thì ghi blocker, không tự sửa phần của agent khác.
- Task phát sinh ngoài phạm vi phải quay lại review spec.
- Trạng thái thật chỉ nằm ở file này; bằng chứng thật chỉ nằm ở `verification.md`.
