package Stringapp;

public class TaiKhoan {
    private String tenDangNhap;
    private String matKhau;

    public TaiKhoan(String tenDangNhap, String matKhau) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
    }

    public boolean dangNhap(String tenNhap, String matKhauNhap) {
        // Ten dang nhap khong phan biet hoa thuong
        boolean dungTen = tenDangNhap.equalsIgnoreCase(tenNhap.trim());

        // Mat khau phan biet hoa thuong
        boolean dungMatKhau = matKhau.equals(matKhauNhap);

        return dungTen && dungMatKhau;
    }
}