package Exception.customexception.checkedandunchecked;

public class Main {
    public static void main(String[] args) {
        BankAccount myAccount = new BankAccount();

        // Khách hàng muốn chuyển 60 triệu
        int amount = 60000000;

        System.out.println("=== BANK TRANSFER APP ===");

        // 1. GỌI HÀM CÓ UNCHECKED EXCEPTION
        // Bạn cứ khơi khơi gọi hàm validateAmount() ở bên ngoài, không cần try-catch gì cả.
        // Trình biên dịch Java vẫn tha cho bạn, nó coi như bạn tự chịu trách nhiệm.
        myAccount.validateAmount(amount);

        // 2. GỌI HÀM CÓ CHECKED EXCEPTION
        // Nếu bạn gõ lệnh: myAccount.checkDailyLimit(amount); MÀ KHÔNG CÓ TRY-CATCH,
        // Java sẽ gạch chân màu đỏ ngay lập tức và cấm bạn chạy code!
        // Bắt buộc phải bao bọc nó lại như thế này:
        try {

            myAccount.checkDailyLimit(amount);

        } catch (LimitExceededException e) {
            // "Transaction blocked: " -> Giao dịch bị chặn:
            // (transaction = giao dịch, blocked = bị chặn)
            System.out.println("❌ TRANSACTION BLOCKED: " + e.getMessage());
        }

        System.out.println("=== THANK YOU ===");
    }
}