// File: ChayHeThong.java

class HeThongQuet {

    // Chuc nang 1: Cau hinh nguon bang switch-case
    public double thietLapDienAp(int maLinhKien) {
        switch (maLinhKien) {
            case 1:
                return 0.7; // Silicon
            case 2:
                return 0.3; // Germanium
            case 3:
                return 1.2; // GaAs
            default:
                return 0.0; // Khong hop le
        }
    }

    // Chuc nang 2: Quet lo hang dung for, continue, try-catch va toan tu ba ngoi
    public void kiemTraLoHang(int maLinhKien, int tongSoLinhKien) {
        double dienAp = thietLapDienAp(maLinhKien);
        System.out.println("--- Bat dau quet lo hang (Dien ap thiet lap: " + dienAp + "V) ---");

        for (int i = 1; i <= tongSoLinhKien; i++) {

            // Xu ly ky thuat 1: Neu la vi tri so 3 thi bo qua bang 'continue'
            if (i == 3) {
                System.out.println("Linh kien so 3 bi trong, bo qua!");
                continue;
            }

            // Xu ly ky thuat 2: Do kiem cac linh kien con lai va bao ve bang try-catch
            try {
                // Toan tu ba ngoi: Neu i == 5 thi tro khang bang 0 (ngan mach), nguoc lai bang 10
                int troKhang = (i == 5) ? 0 : 10;

                // Tinh toan dong dien (Neu troKhang == 0 se gay ra loi chia cho 0)
                int dongDien = 100 / troKhang;

                System.out.println("Linh kien so " + i + " hoat dong tot.");
            } catch (ArithmeticException y) {
                // Bat dung loi chia cho 0 cua linh kien so 5 de mach khong bi sap
                System.out.println("Canh bao: Linh kien so 5 bi ngan mach (Loi chia cho 0)!");
            }
        }

        System.out.println("--- Qua trinh quet hoan tat an toan ---");
    }
}

public class ChayHeThong {
    public static void main(String[] args) {
        HeThongQuet heThong = new HeThongQuet();

        // Quet lo hang chip Silicon (ma 1) voi 6 linh kien
        heThong.kiemTraLoHang(1, 6);
    }
}