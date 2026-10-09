# Sơ đồ hệ thống quản lý thiết bị

Các sơ đồ dưới đây mô tả vòng đời thiết bị và luồng cấp phát đang được backend
triển khai. Tài liệu chỉ sử dụng những trạng thái và chuyển đổi thực sự tồn tại
trong source code.

## Luồng trạng thái thiết bị

```mermaid
stateDiagram-v2
    direction LR

    state "Sẵn sàng (AVAILABLE)" as AVAILABLE
    state "Đã cấp phát (ASSIGNED)" as ASSIGNED
    state "Đang sửa chữa (UNDER_REPAIR)" as UNDER_REPAIR

    [*] --> AVAILABLE: Tạo thiết bị

    AVAILABLE --> ASSIGNED: Cấp phát thiết bị
    ASSIGNED --> AVAILABLE: Trả thiết bị trong tình trạng GOOD
    ASSIGNED --> UNDER_REPAIR: Trả thiết bị trong tình trạng DAMAGED

    AVAILABLE --> UNDER_REPAIR: Cập nhật trạng thái thủ công
    UNDER_REPAIR --> AVAILABLE: Hoàn tất sửa chữa
    UNDER_REPAIR --> AVAILABLE: Cập nhật thủ công khi không có phiên sửa chữa mở

    note right of ASSIGNED
        Phiếu cấp phát ACTIVE hoặc OVERDUE
        đều giữ thiết bị ở trạng thái ASSIGNED.
    end note

    note right of UNDER_REPAIR
        Phiếu sửa chữa có thể là PENDING hoặc IN_PROGRESS.
        Kết quả UNREPAIRABLE giữ thiết bị
        ở trạng thái UNDER_REPAIR.
    end note
```

### Quy tắc chuyển trạng thái

- `ASSIGNED` do luồng cấp phát quản lý và không thể được chọn trực tiếp qua API
  cập nhật trạng thái thiết bị thông thường.
- Chỉ có thể cấp phát thiết bị đang ở trạng thái `AVAILABLE` và không có phiếu
  cấp phát `ACTIVE` hoặc `OVERDUE`.
- Trả thiết bị trong tình trạng `GOOD` sẽ chuyển thiết bị thành `AVAILABLE`;
  tình trạng `DAMAGED` sẽ chuyển thiết bị thành `UNDER_REPAIR`.
- Khi có phiếu sửa chữa đang mở (`PENDING` hoặc `IN_PROGRESS`), hệ thống không
  cho phép cập nhật trạng thái thiết bị thủ công.
- Hoàn tất sửa chữa sẽ chuyển thiết bị thành `AVAILABLE`. Đánh dấu không thể sửa
  (`UNREPAIRABLE`) sẽ giữ thiết bị ở trạng thái `UNDER_REPAIR`.
- Mô hình hiện tại chưa có trạng thái thanh lý hoặc ngừng sử dụng thiết bị.

## Sơ đồ tuần tự: cấp phát thiết bị
Endpoint: `POST /api/assignments`

Vai trò được phép: `ADMIN` hoặc `IT_STAFF`

Dữ liệu bắt buộc: `userId`, `deviceId` và thời điểm trả dự kiến trong tương lai
`expectedReturnAt`.

```mermaid
sequenceDiagram
    autonumber

    actor Staff as Quản trị viên / Nhân viên IT
    participant Client as Ứng dụng gửi yêu cầu
    participant API as Spring Security + AssignmentController
    participant Service as AssignmentServiceImpl
    participant UserRepo as UserRepository
    participant DeviceRepo as DeviceRepository
    participant AssignmentRepo as DeviceAssignmentRepository
    participant DB as MySQL

    Staff->>Client: Chọn người dùng, thiết bị và ngày trả dự kiến
    Client->>API: POST /api/assignments kèm JWT và JSON
    API->>API: Xác thực, kiểm tra quyền và validate dữ liệu
    API->>Service: assignDevice(request)

    Service->>UserRepo: findById(userId)
    UserRepo->>DB: Truy vấn người dùng
    DB-->>UserRepo: Người dùng hoặc không có kết quả

    alt Không tìm thấy người dùng
        UserRepo-->>Service: Không có kết quả
        Service-->>API: USER_NOT_FOUND
        API-->>Client: Phản hồi lỗi 404
        Client-->>Staff: Hiển thị thông báo không tìm thấy người dùng
    else Tìm thấy người dùng
        UserRepo-->>Service: User
        Service->>DeviceRepo: findById(deviceId)
        DeviceRepo->>DB: Truy vấn thiết bị
        DB-->>DeviceRepo: Thiết bị hoặc không có kết quả

        alt Không tìm thấy thiết bị
            DeviceRepo-->>Service: Không có kết quả
            Service-->>API: DEVICE_NOT_FOUND
            API-->>Client: Phản hồi lỗi 404
            Client-->>Staff: Hiển thị thông báo không tìm thấy thiết bị
        else Tìm thấy thiết bị
            DeviceRepo-->>Service: Device
            Service->>AssignmentRepo: existsByDeviceIdAndStatusIn(ACTIVE, OVERDUE)
            AssignmentRepo->>DB: Kiểm tra phiếu cấp phát đang mở
            DB-->>AssignmentRepo: true hoặc false
            AssignmentRepo-->>Service: hasOpenAssignment

            alt Thiết bị không AVAILABLE hoặc đã có phiếu cấp phát mở
                Service-->>API: DEVICE_NOT_AVAILABLE
                API-->>Client: Phản hồi lỗi 400
                Client-->>Staff: Hiển thị thông báo thiết bị không khả dụng
            else Thiết bị khả dụng
                Service->>Service: Tạo phiếu ACTIVE và chuyển thiết bị thành ASSIGNED
                Service->>DeviceRepo: saveAndFlush(device)
                DeviceRepo->>DB: Cập nhật trạng thái thiết bị
                DB-->>DeviceRepo: Đã lưu thiết bị
                Service->>AssignmentRepo: save(assignment)
                AssignmentRepo->>DB: Thêm phiếu cấp phát
                DB-->>AssignmentRepo: Đã lưu phiếu cấp phát
                AssignmentRepo-->>Service: Phiếu cấp phát đã lưu
                Service-->>API: DeviceAssignmentResponse
                API-->>Client: Phản hồi thành công 200
                Client-->>Staff: Hiển thị xác nhận cấp phát thành công
            end
        end
    end
```

Phương thức service chạy trong transaction. Nếu quá trình lưu dữ liệu thất bại,
việc đổi trạng thái thiết bị và tạo phiếu cấp phát sẽ được rollback cùng nhau.

## Source code tham chiếu

- `src/main/java/com/example/device/enums/DeviceState.java`
- `src/main/java/com/example/device/controller/AssignmentController.java`
- `src/main/java/com/example/device/service/impl/AssignmentServiceImpl.java`
- `src/main/java/com/example/device/service/impl/DeviceServiceImpl.java`
- `src/main/java/com/example/device/service/impl/DeviceRepairServiceImpl.java`
