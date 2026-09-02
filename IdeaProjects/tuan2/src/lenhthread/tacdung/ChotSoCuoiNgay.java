package lenhthread.tacdung;

class CaoDuLieuDoanhThu implements Runnable {
    private String tenSan;
    public int doanhThu = 0; // Biến lưu kết quả

    public CaoDuLieuDoanhThu(String tenSan) { this.tenSan = tenSan; }

    @Override
    public void run() {
        System.out.println("-> Đang lấy dữ liệu từ " + tenSan + "...");
        try {
            Thread.sleep(2000); // Giả lập mất 2s để cào dữ liệu
            this.doanhThu = (int) (Math.random() * 50) + 10; // Random từ 10 đến 60 triệu
            System.out.println("✅ " + tenSan + " báo cáo: " + doanhThu + " triệu.");
        } catch (InterruptedException e) {}
    }
}

public class ChotSoCuoiNgay {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==== BẮT ĐẦU CHỐT SỔ ====");

        CaoDuLieuDoanhThu shopee = new CaoDuLieuDoanhThu("Shopee");
        CaoDuLieuDoanhThu tiktok = new CaoDuLieuDoanhThu("TikTok Shop");

        Thread luong1 = new Thread(shopee);
        Thread luong2 = new Thread(tiktok);

        luong1.start();
        luong2.start();

        // TÁC DỤNG NỔI BẬT CỦA JOIN() NẰM Ở ĐÂY:
        // Khóa hàm main lại, bắt đợi 2 luồng kia chạy xong mới được đi tiếp.
        luong1.join();
        luong2.join();

        // NẾU KHÔNG CÓ 2 LỆNH JOIN TRÊN: Hàm main sẽ chạy thẳng xuống đây ngay lập tức.
        // Lúc đó shopee và tiktok chưa cào xong (đang ngủ 2s), doanhThu vẫn bằng 0!
        int tongTien = shopee.doanhThu + tiktok.doanhThu;
        System.out.println("==== TỔNG DOANH THU HÔM NAY: " + tongTien + " triệu ====");
    }
}
