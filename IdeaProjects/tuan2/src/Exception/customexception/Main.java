package Exception.customexception;

import java.util.Scanner;
import java.util.InputMismatchException;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Khởi tạo cửa hàng với 5 sản phẩm ban đầu trong kho
        ShoppingCart myCart = new ShoppingCart(5);

        // keepRunning: Biến điều kiện để giữ vòng lặp tiếp tục chạy
        // (keep = duy trì/giữ, running = đang chạy)
        boolean keepRunning = true;

        System.out.println("=== ONLINE CLOTHING STORE ===");

        // Vòng lặp cho phép mua hàng nhiều lần
        while (keepRunning) {
            // In ra số lượng hàng còn lại trong kho ở mỗi vòng lặp
            System.out.println("\nCurrent warehouse stock: " + myCart.getStockAvailable());
            System.out.println("1. Buy items (Mua đồ)");
            System.out.println("2. Exit store (Thoát cửa hàng)");
            System.out.print("Choose an option (1-2): ");

            try {
                // Đọc lựa chọn của người dùng
                int option = sc.nextInt();

                if (option == 2) {
                    keepRunning = false; // Đổi biến thành false để thoát vòng lặp
                    System.out.println("Thank you for visiting! Goodbye.");
                    break;
                }

                if (option == 1) {
                    System.out.print("Enter quantity you want to buy: ");
                    int buyAmount = sc.nextInt();

                    // Gọi hàm checkout. Ở đây có thể nổ ra 3 loại lỗi khác nhau!
                    myCart.checkout(buyAmount);

                } else {
                    // Nếu gõ số khác 1 và 2
                    System.out.println("Invalid option! Please choose 1 or 2.");
                }

            } catch (InputMismatchException e) {
                // Đội gỡ mìn 1: Khách hàng ngáo ngơ gõ chữ lúc chọn menu hoặc nhập số lượng
                System.out.println("❌ ERROR: Please enter a valid integer number!");
                sc.nextLine(); // LỆNH QUAN TRỌNG: Dọn rác bộ nhớ Scanner để tránh vòng lặp vô tận

            } catch (OutOfStockException e) {
                // Đội gỡ mìn 2: Khách mua quá số lượng tồn kho (Lỗi do mình tự chế)
                System.out.println("❌ ORDER FAILED: " + e.getMessage());

            } catch (IllegalArgumentException e) {
                // Đội gỡ mìn 3: Khách cố tình nhập số âm hoặc số 0
                System.out.println("❌ INVALID QUANTITY: " + e.getMessage());

            } catch (Exception e) {
                // Trùm cuối: Phòng hờ tất cả các lỗi không xác định khác
                System.out.println("❌ UNKNOWN ERROR: " + e.getMessage());
            }
        }

        System.out.println("=== PROGRAM TERMINATED ==="); // Chương trình đã chấm dứt
        sc.close();
    }
}