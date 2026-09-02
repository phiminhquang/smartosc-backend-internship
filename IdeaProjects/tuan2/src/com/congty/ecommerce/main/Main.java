package com.congty.ecommerce.main;

// Import các class từ package khác vào để xài
import com.congty.ecommerce.model.Voucher;
import com.congty.ecommerce.repository.Database;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        Database db = new Database();
        double donHangGoc = 200000;

        System.out.println("=== KỊCH BẢN 1: KHÁCH KHÔNG NHẬP MÃ (HOẶC MÃ SAI) ===");
        Optional<Voucher> hopVoucher1 = db.findVoucher("MA_TAO_LAO");
        Voucher voucherApDung1 = hopVoucher1.orElse(new Voucher("DEFAULT", 0, true));
        System.out.println("Tiền thanh toán: " + (donHangGoc - voucherApDung1.getDiscountAmount()));


        System.out.println("\n=== KỊCH BẢN 2: THÔNG BÁO TẶNG QUÀ NẾU TRÚNG MÃ VIP ===");
        Optional<Voucher> hopVoucher2 = db.findVoucher("TET2026");
        hopVoucher2.ifPresent(v -> System.out.println("🎉 Chúc mừng! Bạn đã áp dụng thành công mã: " + v.getCode()));


        System.out.println("\n=== KỊCH BẢN 3: SỨC MẠNH TỐI THƯỢNG (KẾT HỢP FILTER & MAP) ===");
        Optional<Voucher> hopVoucher3 = db.findVoucher("HE2025");
        double tienGiam = hopVoucher3
                .filter(v -> v.isActive())
                .map(v -> v.getDiscountAmount())
                .orElse(0.0);

        System.out.println("Thanh toán cuối cùng: " + (donHangGoc - tienGiam));


        System.out.println("\n=== KỊCH BẢN 4: NGHIỆP VỤ BẮT BUỘC CHÍNH XÁC ===");
        try {
            Voucher voucherNoiBo = db.findVoucher("SAI_MA_NHAN_VIEN")
                    .orElseThrow(() -> new IllegalArgumentException("❌ BÁO ĐỘNG: Mã nội bộ không hợp lệ. Giao dịch bị hủy!"));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}