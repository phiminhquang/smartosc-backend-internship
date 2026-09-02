package lenhthread.tacdung;

class TuDongLuu implements Runnable {
    @Override
    public void run() {
        while (true) { // Vòng lặp vô hạn
            try {
                Thread.sleep(3000); // Cứ 3 giây lưu 1 lần
                System.out.println("☁️ [System] Đã tự động lưu bản nháp dự án lên Cloud.");
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

public class TrinhSoanThao {
    public static void main(String[] args) throws InterruptedException {
        Thread luongAutoSave = new Thread(new TuDongLuu());

        // TÁC DỤNG NỔI BẬT CỦA SETDAEMON NẰM Ở ĐÂY:
        // Đánh dấu đây là luồng phụ tớ (ngầm). Khi hàm main xong việc, luồng này tự bị tiêu diệt.
        // NẾU KHÔNG CÓ LỆNH NÀY: Vòng lặp while(true) sẽ giữ cho chương trình chạy vĩnh viễn, không bao giờ tắt được!
        luongAutoSave.setDaemon(true);
        luongAutoSave.start();

        // Luồng chính: Người dùng đang gõ tài liệu
        System.out.println("✍️ Người dùng: Bắt đầu gõ các công thức toán học...");
        Thread.sleep(8000); // Gõ bài trong 8 giây

        System.out.println("❌ Người dùng: Đã soạn xong, ấn nút TẮT trình soạn thảo!");
        // Hàm main kết thúc tại đây -> Luồng Auto-Save bị hệ điều hành "bóp cổ" chết theo ngay lập tức.
    }
}
