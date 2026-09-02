package Exception.datdonhang;

public class Main {

    public static void main(String[] args) {

        // 1. Khởi tạo máy chủ Game
        GameServer server = new GameServer();

        System.out.println("=== HỆ THỐNG ĐĂNG KÝ GAME ===");

        try {
            // 2. Gọi hàm tạo nhân vật
            // VÙNG TEST: Bạn hãy thay đổi các con số ở đây để xem chương trình nhảy vào catch nào nhé!
            // - Đổi tên thành "Bo" (2 ký tự) -> Nhảy vào catch 1
            // - Đổi số người thành 150 -> Nhảy vào catch 2
            // - Để "Yasuo" và 50 người -> Chạy thành công, bỏ qua catch

            server.createCharacter("yasua", 100);

        } catch (InvalidNameException e) {
            // Bắt riêng quả bom tàng hình (Lỗi tên người chơi nhập sai logic)
            System.out.println("❌ LỖI TẠO NHÂN VẬT: " + e.getMessage());
            System.out.println("💡 Gợi ý: Hãy nghĩ ra một cái tên thật ngầu và dài hơn 3 ký tự nhé!");

        } catch (ServerFullException e) {
            // Bắt riêng quả bom hạng nặng (Lỗi môi trường server)
            System.out.println("❌ LỖI HỆ THỐNG: " + e.getMessage());
            System.out.println("💡 Gợi ý: Hãy rủ bạn bè sang cụm Server 2 chơi tạm nhé.");

        } catch (Exception e) {
            // Trùm cuối: Thói quen cực tốt của dân Pro Coder để tóm gọn mọi lỗi không lường trước được
            System.out.println("❌ LỖI KHÔNG XÁC ĐỊNH: Vui lòng thử lại sau.");

        } finally {
            // 3. Khối lệnh luôn luôn được thực thi dù thành công hay nổ tung
            System.out.println("----------------------------------------");
            System.out.println("[SYSTEM] Đã ngắt kết nối với máy chủ đăng ký.");
        }
    }
}
