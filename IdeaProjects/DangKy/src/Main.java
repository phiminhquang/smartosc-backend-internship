import DangKyKhoaHoc.Kenh;
import DangKyKhoaHoc.Mail;
import DangKyKhoaHoc.Sms;

public class Main {
    public static void main(String[] args) {
        Kenh a1 = new Mail("PMQ","Java");
        a1.phat();
        Kenh a2 = new Sms("BDQ","C++");
        a2.phat();
    }
}
