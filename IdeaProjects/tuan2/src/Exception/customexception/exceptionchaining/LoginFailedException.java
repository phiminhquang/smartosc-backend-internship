package Exception.customexception.exceptionchaining;

// Cho kế thừa RuntimeException để làm bom tàng hình
public class LoginFailedException extends RuntimeException {

    // Constructor 1: Dùng khi lỗi đơn giản, chỉ cần báo Message
    public LoginFailedException(String message) {
        super(message);
    }

    // Constructor 2 (CÁI BẠN ĐANG HỎI): Nhận vào Message và một cái Lỗi Gốc (cause)
    // Chữ 'Throwable' là cụ tổ của mọi loại Exception, nên nó chứa được hết các loại lỗi.
    public LoginFailedException(String message, Throwable cause) {
        // Gửi cả message và cái lỗi gốc lên cho lớp cha giữ hộ
        super(message, cause);
    }
}
