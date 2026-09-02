package Exception.customexception.gomloi;


// 1. Tạo 1 quả bom duy nhất dùng để chứa nhiều lỗi (Validation = Xác thực dữ liệu)
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}