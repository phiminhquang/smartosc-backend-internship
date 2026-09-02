package Exception.caclenhe;

public class Main {
    public static void main(String[] args) {

        System.out.println("=== WELCOME TO JAVA BANK ATM ===");

        // Giả lập thẻ ATM của khách hàng.
        // Biến này đáng lẽ phải chứa thông tin thẻ, nhưng do khách chưa đút thẻ vào
        // nên nó mang giá trị rỗng (null).
        // (customer = khách hàng, card = thẻ)
        String customerCard = null;

        try {
            System.out.println("[SYSTEM] Reading card data...");

            // RỦI RO CHẾT NGƯỜI: Cố tình gọi hàm .length() từ một biến null.
            // Máy tính sẽ hoảng loạn: "Cái thẻ không tồn tại thì lấy đâu ra độ dài mà đếm?"
            // -> Lập tức nổ tung ra lỗi NullPointerException!
            int cardLength = customerCard.length();

            System.out.println("Card is valid!"); // Dòng này không bao giờ được chạy

        } catch (NullPointerException e) {

            System.out.println("\n❌ LỖI RỒI! BẮT ĐẦU TRA KHẢO HỒ SƠ 'e':\n");

            // ---------------------------------------------------------
            // LỆNH 1: e.getMessage() (Lấy lý do trọng tâm)
            // ---------------------------------------------------------
            System.out.println("--- 1. Lệnh e.getMessage() ---");
            // "Error message is: " -> Thông điệp lỗi là:
            System.out.println("Error message is: " + e.getMessage());

            // ---------------------------------------------------------
            // LỆNH 2: e.toString() (Lấy Tên lỗi + Lý do)
            // ---------------------------------------------------------
            System.out.println("\n--- 2. Lệnh e.toString() ---");
            // "String representation is: " -> Biểu diễn dạng chuỗi là:
            System.out.println("String representation is: " + e.toString());

            // ---------------------------------------------------------
            // LỆNH 3: e.getStackTrace() (Lấy mảng lịch sử sập nguồn)
            // ---------------------------------------------------------
            System.out.println("\n--- 3. Lệnh e.getStackTrace() ---");
            // Vì nó trả về 1 mảng (Array), ta lấy phần tử đầu tiên [0]
            // để biết đích xác dòng code nào vừa giết chết chương trình.
            System.out.println("Error happened at file: " + e.getStackTrace()[0].getFileName());
            System.out.println("Error happened at line: " + e.getStackTrace()[0].getLineNumber());

            // ---------------------------------------------------------
            // LỆNH 4: e.printStackTrace() (In ra toàn bộ dấu vết)
            // Lệnh này không dùng trong System.out.print() được vì bản thân nó
            // đã là một hàm tự động in ra màn hình (bằng chữ đỏ) rồi.
            // ---------------------------------------------------------
            System.out.println("\n--- 4. Lệnh e.printStackTrace() ---");
            e.printStackTrace();
        }

        System.out.println("\n=== THANK YOU FOR USING JAVA BANK ===");
    }
}
