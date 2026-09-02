package lambdavsstream.lambda;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // 1. Tạo đội hình
        List<Tuong> doiHinh = new ArrayList<>();
        doiHinh.add(new PhapSu("Veigar", 400, 150));
        doiHinh.add(new DauSi("Garen", 900, 80));
        doiHinh.add(new PhapSu("Ahri", 450, 120));
        doiHinh.add(new DauSi("Yasuo", 0, 100)); // Đã bay màu

        System.out.println("--- BẮT ĐẦU TRẬN ĐẤU ---");

        // Dùng Lambda cho Functional Interface (Vẫn giữ lại vì nó tiện)
        HieuUng cuongNo = t -> System.out.println("🔥 " + t.getTen() + " nhận hiệu ứng Cuồng Nộ (x2 Sát thương)!");

        // ====================================================
        // XỬ LÝ THEO CÁCH TRUYỀN THỐNG (VÒNG LẶP FOR + IF)
        // ====================================================
        System.out.println("\n--- BÁO CÁO ĐỘI HÌNH ---");

        // Bước 1: Lọc ra những tướng còn sống
        List<Tuong> tuongConSong = new ArrayList<>();
        for (Tuong t : doiHinh) {
            if (PhepThuat.conSong(t)) { // Gọi hàm static từ Interface
                tuongConSong.add(t);
            }
        }

        // Bước 2: Tìm Pháp sư yếu máu (HP < 500) và buff máu
        System.out.println("\n--- KÍCH HOẠT HỒI MÁU ---");
        for (Tuong t : tuongConSong) {
            if (t instanceof PhapSu) {
                if (t.getHp() < 500) {
                    // Ép kiểu để dùng hàm default
                    ((PhapSu) t).hoiMauTuDong(t);
                }
            }
        }

        // Bước 3: Áp dụng Lambda hiệu ứng Cuồng nộ cho tướng sát thương cao nhất
        System.out.println("\n--- BUFF ĐẶC BIỆT ---");
        Tuong boss = null;
        for (Tuong t : tuongConSong) {
            // Logic tìm Max truyền thống
            if (boss == null || t.getSatThuong() > boss.getSatThuong()) {
                boss = t;
            }
        }

        // Nếu tìm thấy boss thì buff cuồng nộ
        if (boss != null) {
            cuongNo.apDung(boss);
        }
    }
}
