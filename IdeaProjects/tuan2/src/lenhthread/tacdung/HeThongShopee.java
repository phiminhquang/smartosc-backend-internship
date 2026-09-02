package lenhthread.tacdung;

class XuLyDonHang implements Runnable {
    @Override
    public void run() {
        String loaiKhach = Thread.currentThread().getName();
        System.out.println("📦 Bắt đầu đóng gói đơn cho: " + loaiKhach);

        // Vòng lặp đếm đến 2 TỶ để mô phỏng một tác vụ tính toán cực kỳ nặng cho CPU
        long bienDem = 0;
        for (long i = 0; i < 2000000000L; i++) {
            bienDem++;
        }

        System.out.println("🎉 HOÀN THÀNH ĐƠN CHO: " + loaiKhach);
    }
}

public class HeThongShopee {
    public static void main(String[] args) {
        XuLyDonHang congViec = new XuLyDonHang();

        Thread khachThuong = new Thread(congViec, "Khách Hàng Bình Thường");
        Thread khachVIP = new Thread(congViec, "Khách Hàng VIP (Thẻ Đen)");

        // TÁC DỤNG NỔI BẬT CỦA SETPRIORITY NẰM Ở ĐÂY:
        // Điểm 10 (MAX) cho VIP, Điểm 1 (MIN) cho Khách Thường.
        khachVIP.setPriority(Thread.MAX_PRIORITY);
        khachThuong.setPriority(Thread.MIN_PRIORITY);

        // Cho xuất phát cùng một lúc
        khachThuong.start();
        khachVIP.start();
    }
}