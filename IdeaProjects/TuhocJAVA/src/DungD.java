interface KenhThongBao {
    void gui(String noiDung);
}

class EmailThongBao implements KenhThongBao {
    public void gui(String noiDung) {
        System.out.println("Gui Email: " + noiDung);
    }
}

class SmsThongBao implements KenhThongBao {
    public void gui(String noiDung) {
        System.out.println("Gui SMS: " + noiDung);
    }
}

class DangKyKhoaHocD {
    private KenhThongBao kenhThongBao;

    public DangKyKhoaHocD(KenhThongBao kenhThongBao) {
        this.kenhThongBao = kenhThongBao;
    }

    public void dangKy(String tenHocSinh, String tenKhoaHoc) {
        System.out.println(
                tenHocSinh + " da dang ky khoa hoc " + tenKhoaHoc
        );

        kenhThongBao.gui(
                "Dang ky thanh cong khoa hoc " + tenKhoaHoc
        );
    }
}

public class DungD {
    public static void main(String[] args) {
        KenhThongBao email = new EmailThongBao();

        DangKyKhoaHocD dangKyEmail =
                new DangKyKhoaHocD(email);

        dangKyEmail.dangKy("BDQ", "Java OOP");

        System.out.println();

        KenhThongBao sms = new SmsThongBao();

        DangKyKhoaHocD dangKySms =
                new DangKyKhoaHocD(sms);

        dangKySms.dangKy("PMQ", "Java OOP");
    }
}