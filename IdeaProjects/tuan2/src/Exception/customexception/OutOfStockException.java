package Exception.customexception;

// OutOfStockException: Lỗi Hết Hàng
// Cho kế thừa RuntimeException để nó thành "Bom tàng hình" (không ép dùng throws)
public class OutOfStockException extends RuntimeException {

    // Constructor: Hàm khởi tạo nhận vào một câu thông báo (message)
    public OutOfStockException(String message) {

        // super(message): Lệnh này cực kỳ quan trọng!
        // Nó gọi lên lớp cha (RuntimeException) và gửi cái 'message' này cho lớp cha giữ.
        // Nhờ dòng này mà lát nữa bạn mới dùng được lệnh e.getMessage() đấy!
        super(message);
    }
}
