package listproject;

public class Main {
    public static void main(String[] args) {
        QuanLyDanhSach ql = new QuanLyDanhSach();

        // 1. add()
        ql.themTen("An");
        ql.themTen("Chien");
        ql.themTen("Huyen");
        ql.themTen("Quan");

        ql.hienThiDanhSach();

        // 2. add(vi tri, gia tri)
        ql.chenTen(1, "Dung");
        ql.hienThiDanhSach();

        // 3. get()
        ql.layTen(0);
        ql.layTen(2);

        // 4. set()
        ql.suaTen(0, "Binh");
        ql.hienThiDanhSach();

        // 5. size()
        ql.demSoLuong();

        // 6. contains()
        ql.kiemTraTen("Quan");
        ql.kiemTraTen("Long");

        // 7. indexOf() va lastIndexOf()
        ql.themTen("Chien");
        ql.hienThiDanhSach();

        ql.timViTriDauTien("Chien");
        ql.timViTriCuoiCung("Chien");

        // 8. remove theo vi tri
        ql.xoaTheoViTri(1);
        ql.hienThiDanhSach();

        // 9. remove theo gia tri
        ql.xoaTheoTen("Huyen");
        ql.hienThiDanhSach();

        // 10. isEmpty()
        ql.kiemTraRong();

        // 11. duyet List bang for
        ql.hienThiBangFor();

        // 12. duyet List bang for-each
        ql.hienThiBangForEach();

        // 13. sort()
        ql.sapXepTangDan();
        System.out.println("Sau khi sap xep:");
        ql.hienThiDanhSach();

        // 14. reverse()
        ql.daoNguoc();
        System.out.println("Sau khi dao nguoc:");
        ql.hienThiDanhSach();

        // 15. clear()
        ql.xoaTatCa();
        ql.hienThiDanhSach();

        // 16. kiem tra rong sau clear
        ql.kiemTraRong();
    }
}