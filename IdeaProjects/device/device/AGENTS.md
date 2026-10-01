# Device Management

## Phạm vi dự án

- Backend nằm tại thư mục hiện tại, sử dụng Java 17, Spring Boot, Maven, Spring Security, JPA, MySQL và Flyway.
- Frontend nằm trong `frontend/`, sử dụng React, TypeScript, Vite, React Router và Axios.
- Chỉ làm việc trong dự án Device, không sửa các bài tập hoặc project khác trong repository cha nếu người dùng không yêu cầu.

## Cách làm việc

- Đọc code và cấu hình liên quan trước khi thay đổi; ưu tiên mở rộng cấu trúc hiện có.
- Chỉ sửa những file liên quan trực tiếp đến yêu cầu, không tự thêm tính năng ngoài phạm vi.
- Khi thay đổi API backend, cập nhật API contract trong `specs/<feature>/`; không tự sửa phần frontend nếu đang ở vai trò backend.
- Không sửa hoặc commit `target/`, `node_modules/`, cache IDE và file sinh tự động.
- Không ghi mật khẩu, token, khóa bí mật hoặc thông tin đăng nhập vào code, Markdown hay Git.
- Không thay đổi cấu hình database, migration hoặc dữ liệu khi chưa xác định rõ tác động.
- Nếu thiếu thông tin có thể gây sửa sai hoặc mất dữ liệu, hỏi người dùng trước; nếu chỉ thiếu chi tiết nhỏ, nêu giả định hợp lý.

## Phân công code mặc định

- Codex/GPT chịu trách nhiệm backend: `src/main/`, `src/test/`, `pom.xml`, migration, cấu hình backend và backend test. Codex được đọc frontend và chạy kiểm tra frontend nhưng không sửa `frontend/` nếu người dùng không giao rõ.
- Antigravity/Gemini chịu trách nhiệm frontend: toàn bộ `frontend/`. Antigravity được đọc backend và API contract nhưng không sửa backend nếu người dùng không giao rõ.
- Phân công này là mặc định, chỉ thay đổi khi người dùng yêu cầu rõ trong phiên hoặc task ghi owner khác.
- Hai phía giao tiếp qua API contract trong `specs/<feature>/spec.md`; frontend không tự đoán endpoint, request, response hoặc error contract còn thiếu.
- Ghi owner và dependency trong `tasks.md`. Agent chỉ nhận task thuộc owner của mình; nếu bị phụ thuộc phần bên kia thì ghi blocker và dừng ở ranh giới đó.
- Codex mặc định điều phối tài liệu chung (`specs/`, `docs/decisions/`, `AI-HANDOFF.md`). Khi làm song song, Antigravity không sửa các file chung; báo kết quả frontend để Codex tích hợp, hoặc làm trên branch/worktree riêng.
- Không để hai agent sửa cùng một file hoặc cùng một working tree tại cùng thời điểm. Nếu chạy song song, dùng branch/worktree riêng và có một lượt tích hợp sau cùng.

## Quy trình dựa trên tài liệu

- Đọc `PROJECT.md` để hiểu kiến trúc và nguồn sự thật trước khi thiết kế thay đổi lớn.
- Thay đổi hành vi phải đi theo chuỗi Specify -> Plan -> Tasks -> Implement -> Verify -> Review.
- Tính năng liên quan API, database, bảo mật hoặc cả frontend/backend phải có thư mục `specs/<feature>/` gồm `spec.md`, `plan.md`, `tasks.md` và `verification.md`.
- Thay đổi nhỏ có thể dùng một file spec duy nhất nhưng vẫn phải nêu mục tiêu, phạm vi, thiết kế, tiêu chí chấp nhận và kết quả kiểm tra.
- Dùng `specs/README.md` để chọn mức S/M/L và sao chép mẫu phù hợp từ `specs/_template/`.
- Chỉ đánh dấu task hoàn thành khi code hoặc bằng chứng tương ứng đã tồn tại.
- Ghi lệnh và kết quả kiểm tra thật vào `verification.md`; không suy ra trạng thái từ lịch sử chat.
- Quyết định kiến trúc có ảnh hưởng lâu dài được ghi trong `docs/decisions/`.
- Mỗi thời điểm chỉ một agent được sửa cùng một file; agent khác chỉ phản biện hoặc làm phần độc lập.
- Không để tài liệu quy trình thúc đẩy việc thêm kiến trúc hoặc tính năng ngoài nhu cầu thực tế.

## Kiểm tra

- Cổng an toàn mặc định: `bash scripts/verify.sh safe`.
- Backend compile: `bash scripts/verify.sh backend`.
- Frontend lint/build: `bash scripts/verify.sh frontend`.
- Nhóm test password reset/JWT/security không cần datasource thật: `bash scripts/verify.sh password-reset`.
- Chỉ chạy toàn bộ `bash ./mvnw test` khi đã xác nhận test profile dùng database cô lập; không để `contextLoads` áp dụng Flyway lên Aiven/production.
- Với thay đổi liên quan cả hai phía, kiểm tra cả backend và frontend.
- Khi hoàn thành, báo ngắn gọn file đã sửa, kiểm thử đã chạy và lỗi còn lại.

## Chuyển giữa các AI

- Trước khi bàn giao, cập nhật `AI-HANDOFF.md` với mục tiêu, việc đã làm, file đã sửa, kiểm thử, vấn đề còn lại và bước tiếp theo.
- Khi tiếp quản, đọc `PROJECT.md`, `AGENTS.md`, `AI-HANDOFF.md`, spec đang hoạt động, `git status` và diff liên quan trước khi tiếp tục.
- Codex, Claude Code và Antigravity chia sẻ trạng thái qua file trong repository; không coi lịch sử chat của một agent là trạng thái mà agent khác đã biết.
- Khi bàn giao backend cho frontend, Codex phải ghi API contract đã chốt, backend task đã hoàn thành, test đã chạy và dependency frontend còn lại. Khi frontend hoàn thành, kết quả phải được tích hợp vào `tasks.md`, `verification.md` và handoff trước khi coi feature là hoàn tất.
