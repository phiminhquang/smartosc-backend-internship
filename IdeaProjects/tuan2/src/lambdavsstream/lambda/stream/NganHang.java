package lambdavsstream.lambda.stream;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.List;

class GiaoDich {
    String maGD; int soTien; boolean hopLe;
    public GiaoDich(String maGD, int soTien, boolean hopLe) {
        this.maGD = maGD; this.soTien = soTien; this.hopLe = hopLe;
    }
}

public class NganHang {
    public static void main(String[] args) {
        List<GiaoDich> lichSu = Arrays.asList(
                new GiaoDich("GD01", 500, true),
                new GiaoDich("GD02", 1200, true),
                new GiaoDich("GD03", 50000, true),  // Chuyển tiền to bất thường
                new GiaoDich("GD04", 100, false),   // LỖI MẠNG TẠI ĐÂY
                new GiaoDich("GD05", 300, true)
        );

        // 1. peek() - Cỗ máy camera giám sát (Debug)
        // Bản chất: Chụp lại ảnh phần tử lúc nó đi qua băng chuyền mà KHÔNG làm thay đổi nó.
        System.out.println("--- THEO DÕI BĂNG CHUYỀN ---");
        long soGiaoDichNho = lichSu.stream()
                .peek(gd -> System.out.println("Đang xét: " + gd.maGD)) // Đặt camera quay lén
                .filter(gd -> gd.soTien < 1000)
                .peek(gd -> System.out.println("--> Đã lọt qua màng lọc: " + gd.maGD)) // Camera thứ 2
                .count();

        // 2. findFirst() / findAny() - Chốt chặn chớp nhoáng
        // Bản chất: Khác với filter (chạy đến cuối), thằng này cứ tìm thấy 1 cái đúng ý là NGHỈ LUÔN, tắt băng chuyền!
        System.out.println("\n--- TÌM KẺ ĐÁNG NGỜ ĐẦU TIÊN ---");
        lichSu.stream()
                .filter(gd -> gd.soTien > 10000) // Tìm giao dịch > 10k
                .findFirst()                     // Tóm được GD03 là dừng luôn, không thèm xét GD04, GD05 nữa
                .ifPresent(gd -> System.out.println("CẢNH BÁO MÃ: " + gd.maGD));

        // 3. takeWhile() / dropWhile() - Cửa hải quan thông minh (Từ Java 9)
        // Khác filter ở chỗ: filter duyệt hết 100%. takeWhile thì duyệt từ đầu, cứ đúng điều kiện thì cho qua, sai 1 phát là ĐÓNG SẬP CỬA vĩnh viễn.
        System.out.println("\n--- SAO KÊ TRƯỚC LÚC SẬP MẠNG ---");
        lichSu.stream()
                .takeWhile(gd -> gd.hopLe) // Chỉ lấy các GD hợp lệ. Gặp GD04 (false) là cửa đóng sập, chặn luôn GD05 dù GD05 hợp lệ.
                .forEach(gd -> System.out.println("Ghi nhận: " + gd.maGD));

        // 4. summaryStatistics() - Báo cáo thống kê "Bấm nút ăn liền"
        System.out.println("\n--- BÁO CÁO CUỐI NGÀY ---");
        IntSummaryStatistics baoCao = lichSu.stream()
                .mapToInt(gd -> gd.soTien) // Ép băng chuyền về kiểu số nguyên (IntStream)
                .summaryStatistics();      // Tự động tính 5 chỉ số cực xịn

        System.out.println("Tổng số lượng: " + baoCao.getCount());
        System.out.println("Tổng tiền: " + baoCao.getSum());
        System.out.println("GD lớn nhất: " + baoCao.getMax());
        System.out.println("GD nhỏ nhất: " + baoCao.getMin());
        System.out.println("Trung bình: " + baoCao.getAverage());
    }
}
