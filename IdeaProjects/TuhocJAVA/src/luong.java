import java.util.ArrayList;
import java.util.List;

interface TinhLuong {
    double tinhLuong();
}

abstract class NhanVien implements TinhLuong {
    private int id;
    private String ten;
    private String email;

    public NhanVien(int id, String ten, String email) {
        this.id = id;
        this.ten = ten;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getTen() {
        return ten;
    }

    public String getEmail() {
        return email;
    }
}

class NhanVienChinhThuc extends NhanVien {
    private double luongCoBan;
    private double tienThuong;

    public NhanVienChinhThuc(int id, String ten, String email, double luongCoBan, double tienThuong) {
        super(id, ten, email);
        this.luongCoBan = luongCoBan;
        this.tienThuong = tienThuong;
    }

    public double tinhLuong() {
        return luongCoBan + tienThuong;
    }
}

class NhanVienBanThoiGian extends NhanVien {
    private double soGio;
    private double luongMotGio;

    public NhanVienBanThoiGian(int id, String ten, String email, double soGio, double luongMotGio) {
        super(id, ten, email);
        this.soGio = soGio;
        this.luongMotGio = luongMotGio;
    }

    public double tinhLuong() {
        return soGio * luongMotGio;
    }
}

class CuaHang {
    private String tenCuaHang;
    private List<NhanVien> danhSachNhanVien = new ArrayList<>();

    public CuaHang(String tenCuaHang) {
        this.tenCuaHang = tenCuaHang;
    }

    public void themNhanVien(NhanVien nhanVien) {
        danhSachNhanVien.add(nhanVien);
    }

    public void hienThi() {
        System.out.println("Bang luong cua cua hang: " + tenCuaHang);

        for (NhanVien nv : danhSachNhanVien) {
            System.out.println("ID: " + nv.getId());
            System.out.println("Ten: " + nv.getTen());
            System.out.println("Email: " + nv.getEmail());
            System.out.println("Luong: " + nv.tinhLuong());
            System.out.println();
        }
    }
}

public class luong {
    public static void main(String[] args) {
        NhanVien quang = new NhanVienChinhThuc(1, "Quang", "quang@gmail.com", 10000000, 2000000);
        NhanVien bdq = new NhanVienBanThoiGian(2, "BDQ", "bdq@gmail.com", 80, 50000);

        CuaHang cuaHang = new CuaHang("SmartOSC");

        cuaHang.themNhanVien(quang);
        cuaHang.themNhanVien(bdq);

        cuaHang.hienThi();
    }
}