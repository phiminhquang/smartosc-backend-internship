package quanlyhoso;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuanLyHoSo {
    private List<HoSo> danhSach;

    public QuanLyHoSo() {
        danhSach = new ArrayList<>();
    }

    public void themHoSo(HoSo hoSo) {
        danhSach.add(hoSo);
    }

    public void hienThiDanhSach() {
        if (danhSach.isEmpty()) {
            System.out.println("Danh sach dang rong");
            return;
        }

        System.out.format("%-10s%-20s%-10s%-15s%-15s%n", "MA", "HO TEN", "TUOI", "GIOI TINH", "QUE QUAN");

        for (HoSo hoSo : danhSach) {
            hoSo.hienThi();
        }
    }

    public void timTheoTen(String tenCanTim) {
        boolean timThay = false;

        System.out.format("%-10s%-20s%-10s%-15s%-15s%n", "MA", "HO TEN", "TUOI", "GIOI TINH", "QUE QUAN");

        for (HoSo hoSo : danhSach) {
            if (hoSo.getTen().toLowerCase().contains(tenCanTim.toLowerCase())) {
                hoSo.hienThi();
                timThay = true;
            }
        }

        if (!timThay) {
            System.out.println("Khong tim thay ho so");
        }
    }

    public void timTheoMa(String maCanTim) {
        boolean timThay = false;

        for (HoSo hoSo : danhSach) {
            if (hoSo.getMa().equalsIgnoreCase(maCanTim)) {
                System.out.format("%-10s%-20s%-10s%-15s%-15s%n", "MA", "HO TEN", "TUOI", "GIOI TINH", "QUE QUAN");
                hoSo.hienThi();
                timThay = true;
                break;
            }
        }

        if (!timThay) {
            System.out.println("Khong tim thay ho so");
        }
    }

    public void xoaTheoMa(String maCanXoa) {
        boolean timThay = false;

        for (int i = 0; i < danhSach.size(); i++) {
            if (danhSach.get(i).getMa().equalsIgnoreCase(maCanXoa)) {
                danhSach.remove(i);
                timThay = true;
                System.out.println("Da xoa ho so");
                break;
            }
        }

        if (!timThay) {
            System.out.println("Khong tim thay ho so de xoa");
        }
    }

    public void suaQueQuan(String maCanSua, String queQuanMoi) {
        boolean timThay = false;

        for (HoSo hoSo : danhSach) {
            if (hoSo.getMa().equalsIgnoreCase(maCanSua)) {
                hoSo.setQueQuan(queQuanMoi);
                timThay = true;
                System.out.println("Da sua que quan");
                break;
            }
        }

        if (!timThay) {
            System.out.println("Khong tim thay ho so de sua");
        }
    }

    public void hienThiNguoiLaoDong() {
        boolean timThay = false;

        System.out.format("%-10s%-20s%-10s%-15s%-15s%n", "MA", "HO TEN", "TUOI", "GIOI TINH", "QUE QUAN");

        for (HoSo hoSo : danhSach) {
            if (hoSo.getTuoi() >= 18 && hoSo.getTuoi() <= 60) {
                hoSo.hienThi();
                timThay = true;
            }
        }

        if (!timThay) {
            System.out.println("Khong co ho so trong do tuoi lao dong");
        }
    }

    public void demSoHoSo() {
        System.out.println("So ho so hien co: " + danhSach.size());
    }

    public void kiemTraDanhSachRong() {
        if (danhSach.isEmpty()) {
            System.out.println("Danh sach dang rong");
        } else {
            System.out.println("Danh sach khong rong");
        }
    }

    public void sapXepTheoTuoi() {
        danhSach.sort(Comparator.comparingInt(HoSo::getTuoi));
    }

    public void xoaTatCa() {
        danhSach.clear();
    }
}