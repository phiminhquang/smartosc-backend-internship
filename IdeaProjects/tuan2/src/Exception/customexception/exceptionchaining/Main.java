package Exception.customexception.exceptionchaining;

public class Main {
    public static void main(String[] args) {
        AuthService auth = new AuthService();

        System.out.println("=== ỨNG DỤNG ĐĂNG NHẬP ===");

        try {
            auth.login("admin", "123456");

        } catch (LoginFailedException e) {
            // In ra thông báo thân thiện cho khách hàng xem
            System.out.println("❌ THÔNG BÁO CHO KHÁCH: " + e.getMessage());

            System.out.println("\n----------------------------------");
            System.out.println("🔍 LOG CHO DEVELOPER ĐỂ FIX BUG:");
            // KHI DEVELOPER ĐIỀU TRA: In ra toàn bộ gia phả lỗi
            e.printStackTrace();
        }
    }
}
