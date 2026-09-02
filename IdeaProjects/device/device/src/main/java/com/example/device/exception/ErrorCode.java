package com.example.device.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    UNCATEGORIZED_EXCEPTION(9999, "Lỗi không xác định", HttpStatus.INTERNAL_SERVER_ERROR),

    // Authentication
    UNAUTHENTICATED(1001, "Chưa xác thực", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "Bạn không có quyền thực hiện thao tác này", HttpStatus.FORBIDDEN),

    // Validation
    INVALID_KEY(1003, "Key không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL(1004, "Email không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1005, "Mật khẩu không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_NAME(1006, "Tên không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_USER_ID(1007, "User ID không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_DEVICE_ID(1008, "Device ID không được để trống", HttpStatus.BAD_REQUEST),
    INVALID_ROLE_NAME(1009, "Tên role không được để trống", HttpStatus.BAD_REQUEST),

    // User
    EMAIL_EXISTED(1010, "Email đã tồn tại", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1011, "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),

    // Role
    ROLE_NOT_FOUND(1012, "Không tìm thấy quyền", HttpStatus.NOT_FOUND),
    ROLE_ALREADY_EXISTS(1013, "Role đã tồn tại", HttpStatus.BAD_REQUEST),
    ROLE_ALREADY_ASSIGNED(1014, "Người dùng đã được gán role này", HttpStatus.BAD_REQUEST),
    ROLE_NOT_ASSIGNED(1015, "Người dùng chưa được gán role này", HttpStatus.BAD_REQUEST),

    // Device
    INVALID_DEVICE_CATEGORY(1016, "Category thiết bị không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DEVICE_NAME(1017, "Tên thiết bị không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DEVICE_MODEL(1018, "Model thiết bị không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_DEVICE_STATE(1019, "Trạng thái thiết bị không hợp lệ", HttpStatus.BAD_REQUEST),
    DEVICE_NOT_FOUND(1020, "Không tìm thấy thiết bị", HttpStatus.NOT_FOUND),
    SERIAL_NUMBER_EXISTED(1021, "Serial number đã tồn tại", HttpStatus.BAD_REQUEST),
    DEVICE_NOT_AVAILABLE(1022, "Thiết bị hiện không khả dụng", HttpStatus.BAD_REQUEST),

    // Assignment
    ASSIGNMENT_NOT_FOUND(1023, "Không tìm thấy thông tin cấp phát", HttpStatus.NOT_FOUND),
    INVALID_RETURN_DATE(1028, "Thời gian trả thiết bị không hợp lệ", HttpStatus.BAD_REQUEST),
    DEVICE_ALREADY_RETURNED(1029, "Thiết bị đã được trả", HttpStatus.BAD_REQUEST),

    // System owner
    SYSTEM_OWNER_REQUIRED(1024, "Chỉ quản trị viên hệ thống mới được thực hiện thao tác này", HttpStatus.FORBIDDEN),
    CANNOT_DELETE_SYSTEM_OWNER(1025, "Không thể xóa quản trị viên hệ thống", HttpStatus.FORBIDDEN),
    CANNOT_MODIFY_SYSTEM_OWNER(1026, "Không thể sửa quản trị viên hệ thống", HttpStatus.FORBIDDEN),
    CANNOT_CREATE_ADMIN(1027, "Chỉ quản trị viên hệ thống mới được tạo hoặc cấp quyền ADMIN", HttpStatus.FORBIDDEN),

    DEVICE_HAS_ASSIGNMENT_HISTORY(1030, "Không thể xóa thiết bị đã có lịch sử cấp phát", HttpStatus.BAD_REQUEST),
    DEVICE_STATE_MANAGED_BY_ASSIGNMENT(1031, "Trạng thái ASSIGNED chỉ được thay đổi thông qua nghiệp vụ cấp/trả thiết bị", HttpStatus.BAD_REQUEST),
    USER_HAS_ASSIGNMENT_HISTORY(1032, "Không thể xóa người dùng đã có lịch sử cấp phát thiết bị", HttpStatus.BAD_REQUEST),

    EXTENSION_REQUEST_NOT_FOUND(1033, "Không tìm thấy yêu cầu gia hạn", HttpStatus.NOT_FOUND),
    EXTENSION_REQUEST_ALREADY_PENDING(1034, "Assignment đã có yêu cầu gia hạn đang chờ xử lý", HttpStatus.BAD_REQUEST),
    EXTENSION_REQUEST_ALREADY_REVIEWED(1035, "Yêu cầu gia hạn đã được xử lý", HttpStatus.BAD_REQUEST),
    INVALID_EXTENSION_DATE(1036, "Thời hạn gia hạn phải lớn hơn thời hạn hiện tại", HttpStatus.BAD_REQUEST),
    CANNOT_EXTEND_RETURNED_ASSIGNMENT(1037, "Không thể gia hạn thiết bị đã được trả", HttpStatus.BAD_REQUEST),

    RETURN_NOTE_REQUIRED(1038, "Cần ghi chú khi thiết bị bị hỏng", HttpStatus.BAD_REQUEST),

    INVALID_REPAIR_ISSUE(1039, "Mô tả lỗi thiết bị không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_REPAIR_NOTE(1040, "Ghi chú sửa chữa không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_REPAIR_COST(1041, "Chi phí sửa chữa không hợp lệ", HttpStatus.BAD_REQUEST),

    REPAIR_NOT_FOUND(1042, "Không tìm thấy phiếu sửa chữa", HttpStatus.NOT_FOUND),
    DEVICE_NOT_UNDER_REPAIR(1043, "Thiết bị không ở trạng thái sửa chữa", HttpStatus.BAD_REQUEST),
    REPAIR_ALREADY_OPEN(1044, "Thiết bị đã có phiếu sửa chữa đang xử lý", HttpStatus.BAD_REQUEST),
    REPAIR_CANNOT_START(1045, "Chỉ phiếu PENDING mới có thể bắt đầu sửa", HttpStatus.BAD_REQUEST),
    REPAIR_CANNOT_FINISH(1046, "Chỉ phiếu IN_PROGRESS mới có thể kết thúc", HttpStatus.BAD_REQUEST),

    DEVICE_HAS_REPAIR_HISTORY(1047, "Không thể xóa thiết bị đã có lịch sử sửa chữa", HttpStatus.BAD_REQUEST),
    DEVICE_STATE_MANAGED_BY_REPAIR(1048, "Trạng thái thiết bị đang được quản lý bởi quy trình sửa chữa", HttpStatus.BAD_REQUEST);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}