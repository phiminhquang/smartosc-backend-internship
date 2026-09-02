package stringpool;

public class TaiKhoan {
    private String tenDangNhap;
    private String matKhau;

    public TaiKhoan(String tenDangNhap, String matKhau) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    // Dùng equals để so sánh NỘI DUNG String
    public boolean kiemTraDangNhap(String tenNhap, String matKhauNhap) {
        return tenDangNhap.equals(tenNhap) && matKhau.equals(matKhauNhap);
    }
}