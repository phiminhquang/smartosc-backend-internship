package lenhthread.nhiemvutaifile;


public class Main {
    public static void main(String[] args) {
        NhiemVuTaiFile congViec = new NhiemVuTaiFile();

        // Khởi tạo luồng và ĐẶT TÊN cho luồng
        Thread luong1 = new Thread(congViec, "GTA V");
        Thread luong2 = new Thread(congViec, "Fifa Online");

        // Bắt đầu chạy
        luong1.start();
        luong2.start();
    }
}
