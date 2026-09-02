package runablevsextend;

// 3. LOP KICH HOAT CHUONG TRINH
public class Main {
    public static void main(String[] args) {
        System.out.println("==== MO DANH SACH BAN VE ====");

        // Uu diem 2: Chi tao DUNG 1 DOI TUONG cong viec (1 kho ve)
        HeThongBanVe khoVeChung = new HeThongBanVe();

        // Tao 3 luong (3 nhan vien) nhung cung tro vao 1 kho ve
        Thread nhanVien1 = new Thread(khoVeChung, "Alice");
        Thread nhanVien2 = new Thread(khoVeChung, "Bob");
        Thread nhanVien3 = new Thread(khoVeChung, "Charlie");

        // Kich hoat ca 3 nhan vien ban hang cung luc
        nhanVien1.start();
        nhanVien2.start();
        nhanVien3.start();
    }
}