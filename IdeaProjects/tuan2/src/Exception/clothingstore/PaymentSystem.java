package Exception.clothingstore;

// PaymentSystem: Hệ thống thanh toán
public class PaymentSystem {

    // openConnection: Mở kết nối
    public void openConnection() {
        System.out.println("[SYSTEM] Opening connection to bank server...");
        System.out.println("[SYSTEM] Connection SUCCESS. Ready to scan items.");
    }

    // closeConnection: Đóng kết nối
    public void closeConnection() {
        System.out.println("[SYSTEM] Closing bank connection to free memory...");
        System.out.println("[SYSTEM] Connection CLOSED safely.");
    }
}