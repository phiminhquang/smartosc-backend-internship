// File: ThietBiDienTu.java

// 1. Tao Interface de dat ra tieu chuan phat tin hieu
interface BoPhatTinHieu {
    void phatTinHieu(); // Phuong thuc rong, se duoc override o cac class con
}

// 2. Tao Class cha triu tuong hoac co ban
class LinhKien1 {
    String tenLinhKien;

    public LinhKien1(String ten) {
        this.tenLinhKien = ten;
    }

    // Phuong thuc hien thi thong tin co ban
    public void hienThi() {
        System.out.println("Day la: " + tenLinhKien);
    }
}

// 3. Tao Class con ke thừa tu LinhKien va trien khai tu Interface BoPhatTinHieu
class MachPhatVoTuyen extends LinhKien1 implements BoPhatTinHieu {
    double tanSo;

    public MachPhatVoTuyen(String ten, double tanSo) {
        super(ten); // Goi ham khoi tao cua Class cha (LinhKien)
        this.tanSo = tanSo;
    }

    // --- DAY LA OVERRIDING (Ghi de tu Interface) ---
    @Override
    public void phatTinHieu() {
        System.out.println(tenLinhKien + " dang phat song vo tuyen o tan so " + tanSo + " MHz");
    }

    // --- DAY LA OVERLOADING (Nap chong phuong thuc trong cung class) ---
    // Ham phat tin hieu co ban khong tham so (da ghi de o tren)

    // Ham phat tin hieu kem theo thong diep chu
    public void phatTinHieu(String thongDiep) {
        System.out.println(tenLinhKien + " phat tin hieu chu: " + thongDiep);
    }

    // Ham phat tin hieu kem theo mang du lieu so (data dang byte)
    public void phatTinHieu(int cuongDo) {
        System.out.println(tenLinhKien + " phat tin hieu voi cuong do: " + cuongDo + " dBm");
    }
}

// 4. Class chinh de chay chuong trinh
public class ThietBiDienTu {
    public static void main(String[] args) {
        // Khoi tao doi tuong tu Class con
        MachPhatVoTuyen machVhf = new MachPhatVoTuyen("Mach phat VHF", 150.5);

        // Chay phuong thuc ke thua tu class cha
        machVhf.hienThi();

        System.out.println("--- Kiem tra Overriding ---");
        // Goi ham da duoc override tu interface
        machVhf.phatTinHieu();

        System.out.println("--- Kiem tra Overloading ---");
        // Goi cac ham nap chong bang cach truyen tham so khac nhau
        machVhf.phatTinHieu("Hello Transceiver"); // Truyen vao String
        machVhf.phatTinHieu(20);                  // Truyen vao int
    }
}