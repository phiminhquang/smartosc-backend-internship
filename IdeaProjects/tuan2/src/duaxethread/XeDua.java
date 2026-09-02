package duaxethread;

import java.util.Random;

public class XeDua implements Runnable {
    private String tenXe;

    // Hàm khởi tạo để đặt tên cho xe
    public XeDua(String tenXe) {
        this.tenXe = tenXe;
    }

    @Override
    public void run() {
        int quangDuong = 0;
        Random rand = new Random();

        // Xe chạy cho đến khi đạt mốc 100km
        while (quangDuong < 100) {
            // Mỗi nhịp tăng ngẫu nhiên từ 1 đến 5 km
            int buocChay = rand.nextInt(5) + 1;
            quangDuong += buocChay;

            if (quangDuong > 100) {
                quangDuong = 100;
            }

            // Ve thanh tien trinh cho dep mat
            veThanhTienTrinh(quangDuong);

            // Cho luong ngu (nghi ngoi) tu 100ms den 300ms de mo phong toc do
            try {
                int thoiGianNghi = rand.nextInt(200) + 100;
                Thread.sleep(thoiGianNghi);
            } catch (InterruptedException e) {
                System.out.println(tenXe + " bi xe khac tong, dung cuoc choi!");
            }
        }
        System.out.println("\n🏁🏁🏁 [" + tenXe + "] DA VE DICH! 🏁🏁🏁");
    }

    // Ham phu ho tro ve do hoa console
    private void veThanhTienTrinh(int quangDuong) {
        StringBuilder duongDua = new StringBuilder("|");
        int phanTram = quangDuong / 2; // 100km tuong ung 50 ky tu

        for (int i = 0; i < 50; i++) {
            if (i < phanTram) duongDua.append("=");
            else if (i == phanTram) duongDua.append(">");
            else duongDua.append(" ");
        }
        duongDua.append("| ").append(quangDuong).append("km");
        System.out.println(tenXe + ":\t" + duongDua.toString());
    }
}