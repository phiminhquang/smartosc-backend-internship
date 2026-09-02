package lenhthread.nhiemvutaifile;


// BƯỚC 1: ĐỊNH NGHĨA CÔNG VIỆC TẢI FILE
 public class NhiemVuTaiFile implements Runnable {
    @Override
    public void run() {
        // Lấy tên của luồng hiện tại để biết đang tải game gì
        String tenGame = Thread.currentThread().getName();
        System.out.println("-> Bắt đầu tải: " + tenGame);

        try {
            for (int i = 1; i <= 5; i++) {
                System.out.println(tenGame + " đang tải... " + (i * 20) + "%");
                // Lệnh sleep: Bắt luồng tạm dừng 0.5s để mô phỏng tốc độ mạng
                Thread.sleep(500);
            }
            System.out.println("✅ Tải xong: " + tenGame);
        } catch (InterruptedException e) {
            System.out.println("❌ " + tenGame + " bị hủy giữa chừng!");
        }
    }
}



