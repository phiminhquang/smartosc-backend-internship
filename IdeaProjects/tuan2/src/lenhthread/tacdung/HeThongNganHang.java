package lenhthread.tacdung;

// 1. Chỉ cần viết ĐÚNG 1 BẢN MÔ TẢ CÔNG VIỆC dùng chung
class NghiepVuNganHang implements Runnable {
    @Override
    public void run() {
        // TÁC DỤNG NỔI BẬT CỦA GETNAME NẰM Ở ĐÂY:
        // Lệnh này giúp "định vị" xem đoạn code này đang được chạy bởi Anh thợ (Thread) nào.
        String tenQuay = Thread.currentThread().getName();

        System.out.println("👨‍💼 " + tenQuay + ": Xin chào, mời khách hàng tiếp theo!");
        try {
            Thread.sleep(1000); // Giao dịch mất 1 giây
            System.out.println("✅ " + tenQuay + ": Đã xử lý xong giao dịch.");
        } catch (InterruptedException e) {}
    }
}

public class HeThongNganHang {
    public static void main(String[] args) {
        NghiepVuNganHang congViecChung = new NghiepVuNganHang();

        // 2. Thuê 3 nhân viên (3 Thread) và GẮN BIỂN TÊN cho họ lúc mới tạo
        Thread nhanVien1 = new Thread(congViecChung, "Quầy Số 1");
        Thread nhanVien2 = new Thread(congViecChung, "Quầy Số 2 (VIP)");
        Thread nhanVien3 = new Thread(congViecChung, "Quầy Số 3");

        // Kích hoạt
        nhanVien1.start();
        nhanVien2.start();
        nhanVien3.start();
    }
}
