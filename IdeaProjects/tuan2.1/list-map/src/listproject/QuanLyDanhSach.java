package listproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuanLyDanhSach {
    private List<String> danhSachTen;

    public QuanLyDanhSach() {
        danhSachTen = new ArrayList<>();
    }

    // add()
    public void themTen(String ten) {
        danhSachTen.add(ten);
    }

    // add(vi tri, gia tri)
    public void chenTen(int viTri, String ten) {
        if (viTri >= 0 && viTri <= danhSachTen.size()) {
            danhSachTen.add(viTri, ten);
        } else {
            System.out.println("Vi tri chen khong hop le");
        }
    }

    // get()
    public void layTen(int viTri) {
        if (viTri >= 0 && viTri < danhSachTen.size()) {
            System.out.println("Ten tai vi tri " + viTri + ": " + danhSachTen.get(viTri));
        } else {
            System.out.println("Vi tri khong hop le");
        }
    }

    // set()
    public void suaTen(int viTri, String tenMoi) {
        if (viTri >= 0 && viTri < danhSachTen.size()) {
            danhSachTen.set(viTri, tenMoi);
        } else {
            System.out.println("Vi tri khong hop le");
        }
    }

    // remove(vi tri)
    public void xoaTheoViTri(int viTri) {
        if (viTri >= 0 && viTri < danhSachTen.size()) {
            danhSachTen.remove(viTri);
        } else {
            System.out.println("Vi tri khong hop le");
        }
    }

    // remove(gia tri)
    public void xoaTheoTen(String ten) {
        if (danhSachTen.remove(ten)) {
            System.out.println("Da xoa " + ten);
        } else {
            System.out.println("Khong tim thay " + ten);
        }
    }

    // contains()
    public void kiemTraTen(String ten) {
        if (danhSachTen.contains(ten)) {
            System.out.println("Danh sach co ten: " + ten);
        } else {
            System.out.println("Danh sach khong co ten: " + ten);
        }
    }

    // indexOf()
    public void timViTriDauTien(String ten) {
        int viTri = danhSachTen.indexOf(ten);

        if (viTri == -1) {
            System.out.println("Khong tim thay " + ten);
        } else {
            System.out.println("Vi tri dau tien cua " + ten + ": " + viTri);
        }
    }

    // lastIndexOf()
    public void timViTriCuoiCung(String ten) {
        int viTri = danhSachTen.lastIndexOf(ten);

        if (viTri == -1) {
            System.out.println("Khong tim thay " + ten);
        } else {
            System.out.println("Vi tri cuoi cung cua " + ten + ": " + viTri);
        }
    }

    // size()
    public void demSoLuong() {
        System.out.println("So phan tu trong List: " + danhSachTen.size());
    }

    // isEmpty()
    public void kiemTraRong() {
        if (danhSachTen.isEmpty()) {
            System.out.println("Danh sach dang rong");
        } else {
            System.out.println("Danh sach khong rong");
        }
    }

    // for co chi so
    public void hienThiBangFor() {
        System.out.println("Danh sach bang for:");

        for (int i = 0; i < danhSachTen.size(); i++) {
            System.out.println("Vi tri " + i + ": " + danhSachTen.get(i));
        }
    }

    // for-each
    public void hienThiBangForEach() {
        System.out.println("Danh sach bang for-each:");

        for (String ten : danhSachTen) {
            System.out.println(ten);
        }
    }

    // Collections.sort()
    public void sapXepTangDan() {
        Collections.sort(danhSachTen);
    }

    // Collections.reverse()
    public void daoNguoc() {
        Collections.reverse(danhSachTen);
    }

    // clear()
    public void xoaTatCa() {
        danhSachTen.clear();
    }

    public void hienThiDanhSach() {
        System.out.println("Danh sach hien tai: " + danhSachTen);
    }
}