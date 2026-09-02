package Exception.throwvsthrows;


public class DeliveryService {

    // -----------------------------------------------------------------
    // TRƯỜNG HỢP 1: NÉM BOM TÀNG HÌNH (Unchecked Exception)
    // - Không có chữ 'throws' nào ở trên tên hàm cả!
    // - Lỗi IllegalArgumentException tự động được Java châm chước.
    // -----------------------------------------------------------------
    public void checkDistance(double distanceInKm) {
        System.out.println("[SYSTEM] Checking delivery distance...");

        // distance = khoảng cách
        if (distanceInKm < 0 || distanceInKm > 20) {
            // throw: Hành động tự tay ném lỗi tham số không hợp lệ
            // "Invalid distance! We only deliver within 0-20km."
            // -> Khoảng cách không hợp lệ! Chúng tôi chỉ giao trong phạm vi 0-20km.
            throw new IllegalArgumentException("Invalid distance! We only deliver within 0-20km.");
        }

        System.out.println("[SYSTEM] Distance is OK. Proceeding to checkout...");
    }

    // -----------------------------------------------------------------
    // TRƯỜNG HỢP 2: NÉM BOM HẠNG NẶNG (Checked Exception)
    // - BẮT BUỘC phải dán nhãn 'throws Exception' ở chữ ký hàm.
    // - Báo cho ai gọi hàm này biết: "Cẩn thận rớt mạng nhé!"
    // -----------------------------------------------------------------
    public void confirmOrder() throws Exception {
        System.out.println("[SYSTEM] Connecting to server to find a driver...");

        // networkLag: Biến giả lập tình trạng rớt mạng (lag = giật lag, mạng chậm)
        boolean networkLag = true;

        if (networkLag) {
            // throw: Hành động tự ném lỗi hệ thống
            // "Connection timeout! Failed to find a driver."
            // -> Hết thời gian kết nối! Tìm tài xế thất bại.
            throw new Exception("Connection timeout! Failed to find a driver.");
        }

        System.out.println("[SYSTEM] Order SUCCESS! The driver is picking up your food.");
    }
}