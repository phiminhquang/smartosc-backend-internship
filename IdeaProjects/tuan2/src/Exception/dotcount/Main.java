package Exception.dotcount;

class Connect implements Runnable{
    private int dotCount;
    public Connect(int i){
        dotCount = i;
    }

    @Override
    public void run() {
        for (int i = 0; i < dotCount; i++) {
            try {
                // print: Chỉ in ra, KHÔNG xuống dòng
                System.out.print(". ");

                // Thread = Luồng (tiến trình đang chạy của chương trình)
                // sleep = Ngủ / Tạm dừng
                // 1000 milliseconds (mili-giây) = 1 second (giây)
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                // Interrupted = Bị ngắt quãng, bị can thiệp
                // Exception = Ngoại lệ (Lỗi)
                // "Error: Connection interrupted!" -> Lỗi: Kết nối bị ngắt quãng!
                System.out.println("Error: Connection interrupted!");
                break;
            }
        }
    }
}

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // "Connecting to server..." -> Đang kết nối đến máy chủ...
        // println (print line): In ra màn hình và tự động xuống dòng
        System.out.println("Connecting to server...");

        // Variable: dotCount -> Biến: Số lượng dấu chấm
        // 'dot' = dấu chấm, 'count' = số lượng / đếm
        int dotCount = 5;
        Connect d = new Connect(dotCount);
        Thread thread = new Thread(d);
        thread.start();
        // "\n" (new line) = ký tự đặc biệt để ép xuống dòng
        // "Response successful!" -> Phản hồi thành công!
        Thread.sleep(3000);
        if (thread.isAlive()){
            thread.interrupt();
        }else {
        System.out.println("\nResponse successful!");
    }
}
}