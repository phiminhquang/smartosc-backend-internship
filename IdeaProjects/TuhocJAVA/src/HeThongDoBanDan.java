// File: HeThongDoBanDan.java

class Diode {
    // Access Modifier 'private' giup khoa cac thuoc tinh, ngan chan can thiep sai lech tu ben ngoai (Encapsulation)
    private String loaiVatLieu;
    private double dienApNguong; // Kieu nguyen thuy luu thong so co dinh cua linh kien (V)
    private Double dienApDatVao; // Dung Wrapper class Double de nhan duoc gia tri 'null' (bieu dien khi chua cap nguon)

    // Constructor: Tu dong kich hoat de thiet lap vat lieu va dien ap nguong khi tao thiet bi moi tren Heap
    public Diode(String loaiVatLieuDauVao, double dienApNguongDauVao) {
        this.loaiVatLieu = loaiVatLieuDauVao;
        this.dienApNguong = dienApNguongDauVao;
        this.dienApDatVao = null; // Ban dau chua cap nguon nen de null (day la dac quyen cua Wrapper Class)
    }

    // Method 1: Cap dien ap vao cho diode (Thay doi trang thai cua linh kien)
    public void capNguon(double dienApMoi) {
        if (dienApMoi >= 0) {
            this.dienApDatVao = dienApMoi; // Tu dong dong hop (Autoboxing) tu kieu double thiet bi thong thuong sang Double
        }
    }

    // Method 2: Kiem tra trang thai dan thuan cua diode
    public boolean dangDanDong() {
        // Neu chua cap nguon (dienApDatVao dang la null) thi diode chac chan khong dan dong
        if (this.dienApDatVao == null) {
            return false;
        }
        // Neu dien ap dat vao lon hon hoac bang dien ap nguong thi diode thong mach
        return this.dienApDatVao >= this.dienApNguong;
    }

    // Method 3: Hien thi thong so hien tai cua diode de theo doi
    public void hienThiThongSo() {
        String trangThaiNguon = (this.dienApDatVao == null) ? "Chua cap nguon" : this.dienApDatVao + " V";
        System.out.println("Linh kien: Diode " + this.loaiVatLieu);
        System.out.println("- Dien ap nguong thiet ke: " + this.dienApNguong + " V");
        System.out.println("- Dien ap thuc te dat vao: " + trangThaiNguon);
        System.out.println("- Trang thai hoat dong: " + (this.dangDanDong() ? "Dang dan dien (Thong mach)" : "Khong dan dien (Phan cuc nguoc/Chua dan)"));
        System.out.println("-----------------------------------");
    }
}

public class HeThongDoBanDan {
    public static void main(String[] args) {
        // 1. Khoi tao doi tuong (Goi Constructor cap phat vung nho Heap)
        // Diode Silicon thuong co dien ap nguong thuan la 0.7V
        Diode diodeSi = new Diode("Silicon", 0.7);

        // 2. Kiem tra trang thai ban dau khi chua cam nguon dien
        System.out.println("--- Kiem tra trang thai diode vua han vao mach ---");
        diodeSi.hienThiThongSo();

        // 3. Cap nguon thap vao diode (0.5V < 0.7V) -> Diode van chua dan thuan
        System.out.println("--- Cap dien ap ban dau 0.5 V ---");
        diodeSi.capNguon(0.5);
        diodeSi.hienThiThongSo();

        // 4. Tang dien ap dat vao len 1.2V (1.2V >= 0.7V) -> Diode mo thong mach
        System.out.println("--- Tang dien ap dat vao len 1.2 V ---");
        diodeSi.capNguon(1.2);
        diodeSi.hienThiThongSo();
    }
}