package lenhthread.nhacnen;

import lenhthread.nhiemvutaifile.NhiemVuTaiFile;

public class Main {
    public static void main(String[] args) {
        // 1. Tạo luồng nhạc nền
        Thread luongNhac = new Thread(new NhacNen());
        luongNhac.setDaemon(true); // Lệnh setDaemon: Biến thành luồng ngầm (Bắt buộc gọi TRƯỚC start)
        luongNhac.start();

        // 2. Tạo luồng tải game
        NhiemVuTaiFile congViec = new NhiemVuTaiFile();
        Thread luong1 = new Thread(congViec, "GTA V (VIP)");
        Thread luong2 = new Thread(congViec, "Fifa Online");

        // Lệnh setPriority: Ưu tiên luồng 1 chạy trước (Tùy thuộc HĐH xử lý)
        luong1.setPriority(Thread.MAX_PRIORITY); // Điểm 10
        luong2.setPriority(Thread.MIN_PRIORITY); // Điểm 1

        luong1.start();
        luong2.start();
    }
}
