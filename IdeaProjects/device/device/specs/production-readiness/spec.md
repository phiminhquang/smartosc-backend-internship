# Đặc tả: Hoàn thiện hệ thống để vận hành, chịu tải và phục hồi sự cố

## Trạng thái

- Giai đoạn: Approved (2026-10-08, người dùng duyệt trong cuộc trò chuyện).
- Mức: L.
- Người duyệt phạm vi: Người dùng/chủ project.
- Được triển khai theo từng giai đoạn và owner trong `tasks.md`; kết quả chỉ được đánh dấu hoàn thành khi có bằng chứng.

## Cách các AI tiếp quản công việc

Mỗi AI bắt đầu phiên liên quan đến kế hoạch này phải đọc theo thứ tự:

1. `PROJECT.md` và `AGENTS.md`.
2. `AI-HANDOFF.md`.
3. File này.
4. `plan.md`.
5. `tasks.md` để nhận đúng task và owner.
6. `verification.md` để biết bằng chứng thật và phần chưa kiểm tra.
7. Spec của feature phụ thuộc, trước mắt là `specs/password-reset/`.

Không dùng lịch sử chat làm nguồn trạng thái. Không đánh dấu hoàn thành nếu chưa có code hoặc bằng chứng trong repository.

## Hiện trạng đã xác nhận

- Backend là Spring Boot, Spring Security/JWT, JPA, MySQL và Flyway; frontend là React/TypeScript.
- Hệ thống đã có quản lý thiết bị, người dùng, vai trò, cấp phát, gia hạn, hoàn trả, sửa chữa, dashboard, email và import/export.
- API thiết bị đã hỗ trợ tìm kiếm và phân trang; nhiều API người dùng, cấp phát, sửa chữa và gia hạn vẫn trả danh sách đầy đủ.
- Export thiết bị đang đọc toàn bộ dữ liệu bằng `findAll()`.
- Scheduler và password reset còn gửi email đồng bộ; lỗi email ở một số luồng đang ghi bằng `System.out`.
- `Device` đã có trường `version` và một số thao tác đã khóa bản ghi, nhưng chưa có integration test MySQL thật cho các request đồng thời.
- Test hiện tại chủ yếu là unit/web-security test cô lập; chưa có MySQL test/staging an toàn để chạy full integration test và Flyway.
- Repository chưa có Docker/Compose, CI, metrics dashboard hoặc quy trình backup/restore được kiểm chứng.
- Password reset vẫn còn các task production và E2E mở trong `specs/password-reset/tasks.md`.

## Vấn đề

Project có đủ nghiệp vụ để trình diễn nhưng chưa chứng minh được rằng nó:

- chạy lặp lại trên máy mới mà không phụ thuộc cấu hình cá nhân;
- giữ đúng dữ liệu khi nhiều request cùng sửa một tài nguyên;
- phản hồi ổn khi dữ liệu tăng lớn;
- không giữ request/database transaction trong lúc chờ email;
- cung cấp đủ log và số liệu để tìm nguyên nhân khi chậm hoặc lỗi;
- có thể sao lưu, khôi phục và quay lui sau một lần phát hành lỗi.

## Mục tiêu

- Tạo môi trường local/test/demo cô lập, có thể khởi động lặp lại và không chạm database thật ngoài ý muốn.
- Hoàn tất kiểm chứng tích hợp password reset trước khi public demo.
- Phân trang các danh sách có thể tăng lớn và xử lý import/export theo từng nhóm dữ liệu.
- Có dữ liệu thử và phép đo trước/sau cho các truy vấn quan trọng; chỉ thêm index dựa trên bằng chứng.
- Chứng minh các luồng cấp phát, hoàn trả, gia hạn và sửa chữa giữ đúng dữ liệu khi có request đồng thời.
- Chuyển email sang công việc nền có trạng thái, thử lại và chống gửi trùng.
- Có log theo request, health check và dashboard theo dõi API, database, scheduler và email nền.
- Có quy trình deploy demo, backup, restore và rollback đã được thực hành trên môi trường không phải production.
- Tạo bằng chứng kỹ thuật trung thực để dùng trong README, CV và phỏng vấn.

## Ngoài phạm vi

- Không thêm chatbot, AI/OCR, QR hoặc nghiệp vụ mới không cần cho production readiness.
- Không tách microservice, không thêm Kafka và không dùng Kubernetes trong kế hoạch cơ sở này.
- Không đổi MySQL sang database khác chỉ để bổ sung từ khóa CV.
- Không tối ưu dựa trên phỏng đoán hoặc ghi số chịu tải chưa được đo.
- Không tự tạo tài khoản cloud, phát sinh chi phí, nhập secret hoặc chạy migration/restore trên database thật thay người dùng.
- Không tự sửa frontend bằng Codex hoặc backend bằng Antigravity nếu chưa có ngoại lệ được người dùng ghi rõ.

## Vai trò và quyền quyết định

### Codex/GPT

- Sở hữu backend, database migration, backend test, cấu hình backend, Compose/root infrastructure và tài liệu chung.
- Chốt API contract trong spec trước khi giao task frontend phụ thuộc.
- Tích hợp kết quả, chạy cổng kiểm tra và cập nhật verification/handoff.

### Antigravity/Gemini

- Sở hữu toàn bộ `frontend/`.
- Chỉ triển khai theo API contract đã chốt; không tự đoán endpoint hoặc response.
- Báo blocker và kết quả để Codex tích hợp vào tài liệu chung.

### Người dùng/chủ project

- Duyệt phạm vi, API thay đổi và điểm chuyển giai đoạn.
- Chọn nhà cung cấp deploy, tạo tài khoản, duyệt chi phí và nhập secret ngoài Git.
- Thực hiện kiểm tra trình duyệt/email, backup/restore và diễn tập sự cố với hướng dẫn của Codex.
- Duyệt bằng chứng trước khi cho phép ghi kết quả vào CV.

## Yêu cầu chức năng

### FR-1: Môi trường cô lập và lặp lại được

- Given một máy có Docker và source code,
- When chạy lệnh khởi động đã tài liệu hóa,
- Then frontend, backend, MySQL và hộp thư test phải chạy mà không cần dùng credential production.

### FR-2: Kiểm thử database thật nhưng an toàn

- Given integration test hoặc migration test,
- When test chạy,
- Then MySQL tạm/cô lập phải được tạo và hủy độc lập với Aiven hoặc production.

### FR-3: Danh sách và file lớn

- Given dữ liệu người dùng/cấp phát/sửa chữa/gia hạn tăng lớn,
- When client truy vấn danh sách,
- Then API chỉ trả một trang có giới hạn và bộ lọc/sắp xếp được xác định trong contract.
- Given export/import lớn,
- When xử lý file,
- Then backend không được bắt buộc giữ toàn bộ bảng hoặc toàn bộ file trong bộ nhớ.

### FR-4: Tính đúng đắn khi request đồng thời

- Given hai request cùng thao tác lên một thiết bị, assignment, extension hoặc repair,
- When chúng chạy đồng thời,
- Then chỉ kết quả hợp lệ được commit và dữ liệu không có trạng thái mâu thuẫn.

### FR-5: Email nền đáng tin cậy

- Given email service chậm hoặc tạm ngừng,
- When nghiệp vụ tạo yêu cầu email,
- Then nghiệp vụ chính phải tuân theo contract đã chốt, công việc email được lưu bền vững, có thử lại và không gửi trùng.

### FR-6: Có thể tìm nguyên nhân lỗi

- Given một request hoặc job lỗi,
- When người vận hành có request/job ID,
- Then họ phải tìm được log, trạng thái và số liệu liên quan mà không thấy secret/token/mật khẩu.

### FR-7: Khôi phục được

- Given database demo/test bị mất hoặc bản phát hành mới lỗi,
- When áp dụng runbook,
- Then người dùng có thể restore dữ liệu hoặc quay lại phiên bản ứng dụng trước và kiểm tra tính toàn vẹn.

## Yêu cầu phi chức năng

- Bảo mật: secret chỉ đi qua biến môi trường hoặc secret store; log không chứa mật khẩu, JWT hoặc reset token dạng rõ.
- Hiệu năng: phải có máy/môi trường đo, tập dữ liệu và kết quả trước/sau; chưa chốt con số marketing khi chưa có baseline.
- Tương thích: mọi thay đổi API phá vỡ client phải được ghi và phối hợp với Antigravity trước khi merge.
- Database: migration mới chỉ được thêm tiếp; không sửa migration đã áp dụng. Index chỉ thêm sau khi xem kế hoạch chạy truy vấn và đo thực tế.
- Quan sát: lỗi server có mã liên kết với log; endpoint quản trị/metrics không được public tùy tiện.
- Khôi phục: backup không được coi là hoàn thành nếu chưa restore thử trên môi trường cô lập.

## Tiêu chí chấp nhận tổng thể

- [ ] Project khởi động được bằng quy trình đã tài liệu hóa với database và email test cô lập.
- [ ] Full integration test không thể kết nối database production/Aiven ngoài ý muốn.
- [ ] Password reset đạt ma trận E2E áp dụng và các follow-up production PR-207/208/209 hoàn thành.
- [ ] Mọi API danh sách có thể tăng lớn đều phân trang hoặc có lý do được duyệt để không phân trang.
- [ ] Import/export lớn được xử lý theo nhóm hoặc công việc nền và có test giới hạn tài nguyên.
- [ ] Có test MySQL thật cho các thao tác đồng thời quan trọng.
- [ ] SMTP lỗi không làm mất công việc email; retry và chống gửi trùng có bằng chứng.
- [ ] Có request ID, log chuẩn, health check và dashboard tối thiểu.
- [ ] Có báo cáo đo dữ liệu lớn trước/sau, không dùng số liệu tự suy đoán.
- [ ] Backup/restore và rollback được thực hành trên demo/test.
- [ ] Backend test, frontend lint/build, migration test, E2E áp dụng và `git diff --check` đều có bằng chứng.
- [ ] README/diagram/runbook/handoff phản ánh đúng implementation cuối.

## Giả định

- Giai đoạn đầu dùng Docker local với MySQL và Mailpit để gỡ blocker hiện tại mà không cần tài khoản trả phí.
- Kiến trúc tiếp tục là một ứng dụng Spring Boot; email worker có thể chạy trong cùng ứng dụng ở bước đầu nhưng công việc phải được lưu bền vững.
- Quy mô dữ liệu test khởi đầu sẽ được tăng dần theo khả năng máy; kết quả luôn ghi kèm cấu hình môi trường.

## Điểm cần người dùng xác nhận trước khi Approved

- Đã duyệt phạm vi và thứ tự giai đoạn trong `plan.md` ngày 2026-10-08.
- Đã duyệt contract phân trang mặc định: `page=0`, `size=20`, giới hạn `size=100`. Chi tiết request/response từng API được chốt trong giai đoạn 2 trước khi triển khai.
- Khi tới giai đoạn deploy, chọn nhà cung cấp và ngân sách; quyết định này chưa chặn các giai đoạn local/test.
