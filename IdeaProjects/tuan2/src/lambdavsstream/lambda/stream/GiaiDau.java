package lambdavsstream.lambda.stream;

import java.util.Arrays;
import java.util.List;

class CauThu {
    String ten; int theLuc; boolean chanThuong;
    public CauThu(String ten, int theLuc, boolean chanThuong) {
        this.ten = ten; this.theLuc = theLuc; this.chanThuong = chanThuong;
    }
}

public class GiaiDau {
    public static void main(String[] args) {
        List<CauThu> doiHinh = Arrays.asList(
                new CauThu("Quang Hải", 80, false),
                new CauThu("Tiến Linh", 70, false),
                new CauThu("Văn Hậu", 40, true) // Đang chấn thương
        );

        // 1. Các lệnh kiểm tra Boolean (Trả về true/false)
        System.out.println("--- KIỂM TRA ĐỘI HÌNH ---");

        // Có BẤT KỲ (any) ai bị chấn thương không?
        boolean coNguoiDau = doiHinh.stream().anyMatch(c -> c.chanThuong);

        // TẤT CẢ (all) đều đủ thể lực (>50) đá trọn 90 phút chứ?
        boolean sanSang = doiHinh.stream().allMatch(c -> c.theLuc > 50);

        // KHÔNG CÓ AI (none) thể lực bằng 0 chứ?
        boolean khongAiNgat = doiHinh.stream().noneMatch(c -> c.theLuc == 0);

        System.out.println("Có người chấn thương: " + coNguoiDau);
        System.out.println("Tất cả đủ thể lực: " + sanSang);

        // 2. Lệnh max / min: Tìm người xuất sắc nhất
        System.out.println("\n--- TÌM KIẾM ---");
        doiHinh.stream()
                .max((c1, c2) -> Integer.compare(c1.theLuc, c2.theLuc))
                .ifPresent(c -> System.out.println("MVP Thể lực: " + c.ten)); // Lấy Quang Hải

        // 3. Lệnh reduce: Cỗ máy dồn toa (Tính tổng, tích,...)
        System.out.println("\n--- TÍNH TỔNG BẰNG REDUCE ---");
        // Bắt đầu từ 0, cứ gặp cầu thủ nào thì cộng thể lực của họ vào kho (tong)
        int tongTheLuc = doiHinh.stream()
                .map(c -> c.theLuc)
                .reduce(0, (tong, theLucHienTai) -> tong + theLucHienTai);
        System.out.println("Tổng chỉ số thể lực toàn đội: " + tongTheLuc);
    }
}
