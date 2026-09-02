package lambdavsstream.lambda.ver2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// ==========================================
// 1. CLASS DỮ LIỆU CƠ BẢN
// ==========================================
class DonHang {
    private String maDon;
    private double tongTien;
    private boolean daThanhToan;

    public DonHang(String maDon, double tongTien, boolean daThanhToan) {
        this.maDon = maDon;
        this.tongTien = tongTien;
        this.daThanhToan = daThanhToan;
    }

    public String getMaDon() { return maDon; }
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }
    public boolean isDaThanhToan() { return daThanhToan; }

    @Override
    public String toString() {
        return "Đơn " + maDon + " | " + tongTien + " VNĐ | " + (daThanhToan ? "Đã trả" : "Chưa trả");
    }
}

// ==========================================
// 2. FUNCTIONAL INTERFACE (Đợi sẵn Lambda)
// ==========================================
@FunctionalInterface
interface KhuyenMai {
    // Hàm nhận vào 1 đơn hàng, tính toán và trả về số tiền được giảm
    double tinhTienGiam(DonHang donHang);
}

// ==========================================
// 3. CHƯƠNG TRÌNH CHÍNH (Sân khấu của Lambda)
// ==========================================
public class Main {
    public static void main(String[] args) {
        List<DonHang> danhSach = new ArrayList<>();
        danhSach.add(new DonHang("DH01", 500000, true));
        danhSach.add(new DonHang("DH02", 1500000, false));
        danhSach.add(new DonHang("DH03", 200000, true));
        danhSach.add(new DonHang("DH04", 3000000, false));

        System.out.println("--- 1. LAMBDA 2 THAM SỐ (Sắp xếp đơn hàng) ---");
        // Tình huống: Sắp xếp đơn hàng theo giá trị giảm dần.
        // Cấu trúc: (tham_số_1, tham_số_2) -> biểu_thức
        Collections.sort(danhSach, (d1, d2) -> Double.compare(d2.getTongTien(), d1.getTongTien()));
        danhSach.forEach(d -> System.out.println(d));


        System.out.println("\n--- 2. LAMBDA CÓ KHỐI LỆNH PHỨC TẠP (Áp dụng Khuyến Mãi) ---");
        // Tình huống: Tạo ra một mã giảm giá "Ngày Lễ" có logic phức tạp (Dùng ngoặc nhọn {} và chữ return).
        KhuyenMai maNgayLe = don -> {
            if (don.getTongTien() > 2000000) {
                return don.getTongTien() * 0.2; // Giảm 20% cho đơn > 2 triệu
            } else if (don.getTongTien() > 1000000) {
                return don.getTongTien() * 0.1; // Giảm 10% cho đơn > 1 triệu
            }
            return 0; // Không giảm
        };

        // Áp dụng thử mã giảm giá cho đơn đầu tiên trong danh sách (Đơn DH04 - 3 triệu)
        DonHang donVip = danhSach.get(0);
        double tienGiam = maNgayLe.tinhTienGiam(donVip);
        donVip.setTongTien(donVip.getTongTien() - tienGiam);
        System.out.println("Đã giảm " + tienGiam + " VNĐ cho " + donVip.getMaDon());


        System.out.println("\n--- 3. LAMBDA 1 THAM SỐ + STREAM API (Lọc dữ liệu) ---");
        // Tình huống: Báo cáo các đơn CHƯA thanh toán và tính tổng nợ
        // Cấu trúc: tham_số -> biểu_thức
        double tongNo = danhSach.stream()
                .filter(d -> !d.isDaThanhToan())          // Lọc: Giữ lại những đơn chưa thanh toán
                .peek(d -> System.out.println("Đang hối thúc: " + d.getMaDon())) // peek: Duyệt qua để in ra
                .mapToDouble(d -> d.getTongTien())        // Trích xuất: Lấy ra cột tiền
                .sum();                                   // Tính tổng
        System.out.println("=> TỔNG TIỀN ĐANG BỊ NỢ: " + tongNo + " VNĐ");


        System.out.println("\n--- 4. LAMBDA 0 THAM SỐ (Chạy ngầm / Đa luồng) ---");
        // Tình huống: Gửi email xác nhận cho khách chạy ở một luồng (Thread) riêng để không làm lag hệ thống.
        // Cấu trúc: () -> biểu_thức
        Runnable guiEmailTask = () -> {
            try {
                System.out.println("[Hệ thống ngầm] Đang chuẩn bị gửi email...");
                Thread.sleep(1500); // Giả lập tốn 1.5s để gửi mạng
                System.out.println("[Hệ thống ngầm] Đã gửi email thành công!");
            } catch (InterruptedException e) { }
        };

        // Khởi động luồng chạy ngầm
        new Thread(guiEmailTask).start();
        System.out.println("(Hàm main đã kết thúc, nhưng email vẫn đang được gửi ngầm...)");
    }
}
