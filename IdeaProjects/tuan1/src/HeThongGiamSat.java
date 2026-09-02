// File: HeThongGiamSat.java

class LoNung1 {
    // Thuoc tinh hang so tinh (Dung chung toan he thong, khong the sua doi)
    public static final int NHIET_DO_TOI_HAN = 1200;

    // Thuoc tinh tinh (O nho dung chung cho tat ca cac doi tuong lo nung de thong ke)
    private static int tongSoLanDo = 0;

    // Cac thuoc tinh rieng cua doi tuong (Duoc bao mat bang private)
    private String tenLo;
    private double nhietDoHienTai;
    private boolean isAnToan;

    // Constructor: Khoi tao doi tuong lo nung moi tren Heap
    public LoNung1(String ten, double nhietDoBanDau) {
        this.tenLo = ten;
        this.nhietDoHienTai = nhietDoBanDau;
        this.isAnToan = true; // Mac dinh ban dau lo hoat dong an toan
    }

    // Phuong thuc noi bo dung de kiem tra logic va update bien thong ke
    private void kiemTraAnToan() {
        tongSoLanDo++; // Tang bien dem dung chung tren vung nho static

        // Dung dieu kien if/else de phan luong trang thai dua tren hang so
        if (this.nhietDoHienTai > NHIET_DO_TOI_HAN) {
            this.isAnToan = false;
        } else {
            this.isAnToan = true;
        }
    }

    // Phuong thuc in thong so ra console
    public void hienThiThongSo() {
        String chuoiTrangThai = this.isAnToan ? "An toan" : "NGUY HIEM (Qua nhiet!)";
        System.out.println("Lo: " + this.tenLo + " | Nhiet do: " + this.nhietDoHienTai + " C | Trang thai: " + chuoiTrangThai);
        System.out.println("Tong so lan kiem tra cua he thong: " + tongSoLanDo);
        System.out.println("-----------------------------------");
    }

    // Gia lap tang nhiet dung vong lap FOR (Biet truoc so buoc lap)
    public void giaLapTangNhietFor(double mucTang, int soBuoc) {
        for (int i = 0; i < soBuoc; i++) {
            this.nhietDoHienTai += mucTang; // Cong don nhiet do
            this.kiemTraAnToan();           // Cap nhat trang thai va bien static
            this.hienThiThongSo();          // In ket qua tung buoc len man hinh
        }
    }

    // Gia lap ha nhiet dung vong lap WHILE (Khong biet truoc so lan lap)
    public void giaLapHaNhietWhile(double mucHa) {
        // Vong lap tiep tuc chay mien la nhiet do van dang o tren muc gianh cho vung an toan
        while (this.nhietDoHienTai > 600) {
            this.nhietDoHienTai -= mucHa; // Tru bot nhiet do
            this.kiemTraAnToan();
            this.hienThiThongSo();
        }
        System.out.println("Qua trinh lam nguoi ket thuc an toan!");
    }
}

public class HeThongGiamSat {
    public static void main(String[] args) {
        // 1. Khoi tao doi tuong lo nung (O nho lk nam o Stack, thong tin doi tuong nam o Heap)
        LoNung1 loA = new LoNung1("Lo_A", 900.5);

        // 2. Hien thi trang thai ban dau
        System.out.println("--- Trang thai ban dau cua lo nung ---");
        loA.hienThiThongSo();

        // 3. Kich hoat vong lap FOR de gia lap qua trinh tang nhiet
        System.out.println("\n--- Bat dau qua trinh tang nhiet (Vong lap FOR) ---");
        loA.giaLapTangNhietFor(100.0, 4);

        // 4. Kich hoat vong lap WHILE de ph phun nito lam nguoi khan cap
        System.out.println("\n--- Kich hoat he thong lam nguoi khancap (Vong lap WHILE) ---");
        loA.giaLapHaNhietWhile(250.0);
    }
}
