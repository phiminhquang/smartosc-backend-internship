package Exception.clothingstore;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        PaymentSystem posMachine = new PaymentSystem(); // Máy POS tính tiền

        // Array (Mảng) mô phỏng database kho hàng của Shop Quần Áo
        // (inventory = hàng tồn kho, items = các món đồ)
        // Vị trí (Index): 0 = Áo thun, 1 = Quần Jeans, 2 = Áo khoác sơ mi
        String[] inventoryItems = {"Basic T-Shirt", "Denim Jeans", "Flannel Shirt"};
        int[] itemPrices = {150000, 300000, 250000};

        System.out.println("=== MEN'S CLOTHING STORE ===");

        // 1. Máy thu ngân bắt buộc phải mở kết nối trước khi tính tiền
        posMachine.openConnection();

        System.out.println("----------------------------");

        // 2. KHU VỰC CÀI MÌN (TRY-CATCH-FINALLY)
        try {
            // Nhập ID món đồ (Dạng chuỗi String để thử thách ép kiểu)
            // "Enter item ID (0-2): " -> Nhập mã sản phẩm (từ 0 đến 2)
            System.out.print("Enter item ID (0-2): ");
            String idInput = sc.nextLine();

            // RỦI RO 1: Ép kiểu. Nhỡ thu ngân gõ "abc" thay vì số? -> Nổ NumberFormatException
            int itemId = Integer.parseInt(idInput);

            // RỦI RO 2: Lấy dữ liệu mảng. Nhỡ thu ngân gõ số 5 (trong khi mảng chỉ có 0,1,2)?
            // -> Nổ ArrayIndexOutOfBoundsException
            String selectedItem = inventoryItems[itemId];
            int selectedPrice = itemPrices[itemId];

            System.out.println("-> You selected: " + selectedItem + " - Price: " + selectedPrice + " VND");

            // RỦI RO 3: Khách đi theo nhóm và muốn chia đều tiền hóa đơn
            // "How many people are splitting the bill?: " -> Có bao nhiêu người muốn chia tiền?
            // (split = chia ra, bill = hóa đơn)
            System.out.print("How many people are splitting the bill?: ");
            int splitWays = sc.nextInt();

            // RỦI RO 3: Nhỡ khách trêu thu ngân, nhập 0 người? -> Nổ ArithmeticException
            int pricePerPerson = selectedPrice / splitWays;

            System.out.println("-> Success! Each person pays: " + pricePerPerson + " VND");

        } catch (NumberFormatException e) {
            // Đội gỡ mìn 1: Xử lý lỗi gõ chữ thay vì gõ số
            // "Invalid input! ID must be a number." -> Đầu vào không hợp lệ! ID phải là số.
            System.out.println("❌ ERROR: Invalid input! ID must be a number.");

        } catch (ArrayIndexOutOfBoundsException e) {
            // Đội gỡ mìn 2: Xử lý lỗi nhập ID không có trong kho (nhập số < 0 hoặc > 2)
            // "Item not found in inventory!" -> Không tìm thấy món đồ trong kho!
            System.out.println("❌ ERROR: Item not found in inventory! We only have IDs 0, 1, 2.");

        } catch (ArithmeticException e) {
            // Đội gỡ mìn 3: Xử lý lỗi chia hóa đơn cho 0 người
            // "Ghost customer? Cannot divide by zero!" -> Khách ma à? Không thể chia cho 0!
            System.out.println("❌ ERROR: Ghost customer? Cannot divide by zero!");

        } finally {
            // ĐỘI DỌN DẸP HIỆN TRƯỜNG
            // Dù thu ngân thao tác đúng hay thao tác sai gây nổ banh xác ở trên,
            // thì máy POS bắt buộc phải được đóng kết nối để khỏi treo hệ thống ngân hàng.
            System.out.println("----------------------------");
            posMachine.closeConnection();
            sc.close(); // Đóng luôn cả Scanner để giải phóng RAM
            System.out.println("=== TRANSACTION COMPLETED ==="); // Giao dịch hoàn tất
        }
    }
}