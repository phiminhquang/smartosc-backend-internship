package lambdavsstream.lambda.stream;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class Phim {
    String ten; String theLoai; double diemDauGia;
    public Phim(String ten, String theLoai, double diemDauGia) {
        this.ten = ten; this.theLoai = theLoai; this.diemDauGia = diemDauGia;
    }
}

public class RapPhim {
    public static void main(String[] args) {
        List<Phim> danhSachPhim = Arrays.asList(
                new Phim("Mai", "Tình Cảm", 8.5),
                new Phim("Lật Mặt 7", "Gia Đình", 9.0),
                new Phim("Godzilla", "Hành Động", 7.5),
                new Phim("Dune 2", "Hành Động", 8.8),
                new Phim("Quỷ Cẩu", "Kinh Dị", 6.5)
        );

        // 1. limit(n) và skip(n): Phân trang (Lấy top 2 và 3, bỏ qua top 1)
        System.out.println("--- CẮT XÉN DATA ---");
        danhSachPhim.stream()
                .sorted((p1, p2) -> Double.compare(p2.diemDauGia, p1.diemDauGia)) // Xếp giảm dần
                .skip(1)  // Bỏ qua phim Top 1 (Lật Mặt 7)
                .limit(2) // Chỉ lấy đúng 2 phim tiếp theo (Dune 2, Mai)
                .forEach(p -> System.out.println(p.ten));

        // 2. map() và distinct(): Trích xuất và loại bỏ trùng lặp
        System.out.println("\n--- TÌM CÁC THỂ LOẠI PHIM (Không trùng) ---");
        danhSachPhim.stream()
                .map(p -> p.theLoai) // Biến ĐỐI TƯỢNG Phim thành CHUỖI theLoai
                .distinct()          // Cỗ máy lọc trùng: 2 chữ "Hành Động" sẽ bị gộp thành 1
                .forEach(System.out::println);

        // 3. Collectors.joining(): Nối chuỗi xịn sò
        System.out.println("\n--- GỬI TIN NHẮN QUẢNG CÁO ---");
        String sms = danhSachPhim.stream()
                .filter(p -> p.diemDauGia > 8.0) // Chỉ lấy phim hay
                .map(p -> p.ten)
                .collect(Collectors.joining(", ", "Hôm nay xem gì: ", "!")); // (Dấu phân cách, Mở bài, Kết bài)
        System.out.println(sms); // Hôm nay xem gì: Mai, Lật Mặt 7, Dune 2!
    }
}
