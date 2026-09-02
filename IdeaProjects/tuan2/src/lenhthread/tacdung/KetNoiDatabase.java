package lenhthread.tacdung;

import java.util.Random;

public class KetNoiDatabase implements Runnable {

    @Override
    public void run() {
        int soLanThu = 1;
        int gioiHanThu = 5; // Chỉ cho phép thử tối đa 5 lần
        boolean ketNoiThanhCong = false;
        Random rand = new Random();

        System.out.println("⏳ Bắt đầu tiến trình kết nối máy chủ...");

        while (soLanThu <= gioiHanThu && !ketNoiThanhCong) {
            System.out.println("\n-> Lần thử thứ " + soLanThu + "...");

            // Giả lập: random ra số từ 1 đến 10. Nếu > 8 thì thành công (Tỷ lệ 20%)
            int tiLe = rand.nextInt(10) + 1;

            if (tiLe > 8) {
                System.out.println("✅ KẾT NỐI MÁY CHỦ THÀNH CÔNG!");
                ketNoiThanhCong = true;
            } else {
                System.out.println("❌ Kết nối thất bại (Máy chủ bận).");
                System.out.println("🛑 Chờ 3 giây để thử lại...");

                try {
                    // TÁC DỤNG THỰC TẾ CỦA SLEEP NẰM Ở ĐÂY:
                    // Bắt luồng này "ngủ" 3 giây để giảm tải cho CPU, không spam máy chủ
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.out.println("Tiến trình kết nối bị ép buộc dừng!");
                    break;
                }
                soLanThu++;
            }
        }

        if (!ketNoiThanhCong) {
            System.out.println("\n🚨 CẢNH BÁO: Đã thử 5 lần nhưng mạng vẫn sập. Báo lỗi cho người dùng!");
        }
    }

    public static void main(String[] args) {
        // Khởi tạo và chạy luồng
        Thread luongKetNoi = new Thread(new KetNoiDatabase());
        luongKetNoi.start();
    }
}
