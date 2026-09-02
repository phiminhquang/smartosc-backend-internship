package lenhthread.tacdung;

class XuLyThanhToan implements Runnable {
    @Override
    public void run() {
        System.out.println("💳 Đang kết nối tới cổng thanh toán ngân hàng...");
        try {
            // Giả lập ngân hàng bị treo, mất tới 20 giây mới phản hồi
            Thread.sleep(20000);
            System.out.println("✅ Thanh toán thành công!");
        } catch (InterruptedException e) {
            // Khi bị hàm main bắn tia interrupt(), nó sẽ văng vào đây
            System.out.println("🚨 Giao dịch thất bại: Hết thời gian chờ (Timeout)!");
            System.out.println("🔙 Đang hoàn tiền lại cho khách hàng...");
        }
    }
}

public class CongThanhToan {
    public static void main(String[] args) throws InterruptedException {
        Thread luongThanhToan = new Thread(new XuLyThanhToan());
        luongThanhToan.start();

        // Hàm main (Hệ thống giám sát) cho phép đợi tối đa 5 giây
        Thread.sleep(5000);

        // Kiểm tra xem luồng thanh toán còn đang sống (đang chạy) hay không
        if (luongThanhToan.isAlive()) {
            System.out.println("\n⚠️ Hệ thống: Ngân hàng phản hồi quá chậm!");
            // TÁC DỤNG NỔI BẬT CỦA INTERRUPT NẰM Ở ĐÂY:
            // Ép luồng thanh toán (đang bị kẹt ở sleep 20s) phải thức dậy ngay lập tức và ném ra Exception
            luongThanhToan.interrupt();
        }
    }
}
