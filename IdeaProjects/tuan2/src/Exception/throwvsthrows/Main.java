package Exception.throwvsthrows;


import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        // Khởi tạo dịch vụ giao hàng đồ ăn (app)
        DeliveryService foodApp = new DeliveryService();

        System.out.println("=== FOOD DELIVERY APP ===");

        System.out.print("Enter your distance from restaurant (km): ");
        double userDistance = sc.nextDouble();

        System.out.println("---------------------------------");

        // KHU VỰC 1: GỌI HÀM KHÔNG CÓ 'THROWS'
        // Java KHÔNG ÉP bạn phải đặt hàm checkDistance vào trong try-catch.
        // Tuy nhiên, Coder có tâm vẫn sẽ chủ động bọc nó lại để lỡ khách nhập sai
        // thì app không bị văng lỗi đỏ lòm (Crash).
        try {
            foodApp.checkDistance(userDistance);

            // KHU VỰC 2: GỌI HÀM CÓ 'THROWS'
            // Java BẮT ÉP hàm confirmOrder() phải nằm trong try-catch.
            // Nếu bạn lôi hàm này ra khỏi khối try, dòng code sẽ bị gạch chân đỏ ngay lập tức!
            foodApp.confirmOrder();

        } catch (IllegalArgumentException e) {
            // Xử lý quả bom từ checkDistance ném ra (Khách nhập số km tào lao)
            System.out.println("❌ ORDER CANCELED (Distance Error): " + e.getMessage());

        } catch (Exception e) {
            // Xử lý quả bom từ confirmOrder ném ra (Đứt cáp quang rớt mạng)
            System.out.println("❌ ORDER CANCELED (Network Error): " + e.getMessage());

        } finally {
            System.out.println("---------------------------------");
            // "Closing app. Thank you!" -> Đóng ứng dụng. Cảm ơn quý khách!
            System.out.println("Closing app. Thank you!");
            sc.close();
        }
    }
}
