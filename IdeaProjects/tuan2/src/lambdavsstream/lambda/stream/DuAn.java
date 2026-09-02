package lambdavsstream.lambda.stream;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class LapTrinhVien {
    String ten; String chucVu; List<String> kyNang;
    public LapTrinhVien(String ten, String chucVu, List<String> kyNang) {
        this.ten = ten; this.chucVu = chucVu; this.kyNang = kyNang;
    }
}

public class DuAn {
    public static void main(String[] args) {
        List<LapTrinhVien> congTy = Arrays.asList(
                new LapTrinhVien("A", "Frontend", Arrays.asList("React", "CSS")),
                new LapTrinhVien("B", "Backend", Arrays.asList("Java", "SQL")),
                new LapTrinhVien("C", "Frontend", Arrays.asList("Vue", "HTML"))
        );

        // 1. groupingBy: Phân nhóm nhân viên theo chức vụ
        System.out.println("--- BÁO CÁO NHÂN SỰ ---");
        Map<String, List<LapTrinhVien>> nhomTheoChucVu = congTy.stream()
                .collect(Collectors.groupingBy(nv -> nv.chucVu));

        // Lấy danh sách team Frontend ra xem
        System.out.println("Team Frontend có: " + nhomTheoChucVu.get("Frontend").size() + " người");

        // 2. flatMap: Cỗ máy "Phẳng hóa" (Cực kỳ vi diệu)
        // Vấn đề: Mỗi LTV có 1 LIST kỹ năng. Nếu dùng map() bình thường, ta sẽ có List của List (Rất rối).
        // Giải pháp: Dùng flatMap để đập vỡ các List con, đổ toàn bộ kỹ năng ra một cái mâm chung!
        System.out.println("\n--- TỔNG HỢP KỸ NĂNG CÔNG TY ---");
        List<String> tatCaKyNang = congTy.stream()
                .flatMap(nv -> nv.kyNang.stream()) // Biến List con thành các dòng chảy nhỏ, gộp vào dòng chảy lớn
                .distinct()
                .collect(Collectors.toList());

        System.out.println("Bộ kỹ năng hiện có: " + tatCaKyNang); // [React, CSS, Java, SQL, Vue, HTML]
    }
}