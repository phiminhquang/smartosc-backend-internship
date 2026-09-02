package taikhoan1;

public class TaiKhoan1 {
    private String tenDangNhap;
    private String matKhau;
    private double soDu;
    private int soLanGiaoDich;

    public TaiKhoan1(String tenDangNhap, String matKhau, double soDu, int soLanGiaoDich) {
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.soDu = soDu;
        this.soLanGiaoDich = soLanGiaoDich;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public double getSoDu() {
        return soDu;
    }

    public int getSoLanGiaoDich() {
        return soLanGiaoDich;
    }

    public boolean kiemTraDangNhap(String tenNhap, String matKhauNhap) {
        return tenDangNhap.equals(tenNhap) && matKhau.equals(matKhauNhap);
    }

    public void napTien(double soTienNap) {
        if (soTienNap <= 0) {
            throw new IllegalArgumentException("So tien nap phai lon hon 0.");
        }

        soDu += soTienNap;
    }

    public void rutTien(double soTienRut) {
        if (soTienRut <= 0) {
            throw new IllegalArgumentException("So tien rut phai lon hon 0.");
        }

        if (soTienRut > soDu) {
            throw new IllegalArgumentException("So du khong du.");
        }

        soDu -= soTienRut;
        soLanGiaoDich++;
    }
}