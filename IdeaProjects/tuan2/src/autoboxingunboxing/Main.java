import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {

        // =========================================================
        // PHAN 1: PHAN BIET int VA Integer
        // =========================================================

        System.out.println("===== PHAN 1: int va Integer =====");

        // int la kieu du lieu nguyen thuy
        int a = 10;

        // Integer la object wrapper cua int
        Integer b = 20;

        System.out.println("a = " + a);
        System.out.println("b = " + b);

        /*
         * int:
         * - la kieu du lieu co ban
         * - khong phai object
         * - khong the dung truc tiep trong ArrayList, HashMap
         *
         * Integer:
         * - la object
         * - dung de luu so nguyen trong ArrayList, HashMap
         */


        // =========================================================
        // PHAN 2: AUTOBOXING
        // =========================================================

        System.out.println();
        System.out.println("===== PHAN 2: AUTOBOXING =====");

        int soNguyen = 100;

        // Java tu dong doi int -> Integer
        Integer soNguyenObject = soNguyen;

        System.out.println("soNguyen = " + soNguyen);
        System.out.println("soNguyenObject = " + soNguyenObject);

        /*
         * Dong nay:
         *
         * Integer soNguyenObject = soNguyen;
         *
         * Java tu hieu nhu:
         *
         * Integer soNguyenObject = Integer.valueOf(soNguyen);
         *
         * int -> Integer
         * Day la AUTOBOXING.
         */


        // =========================================================
        // PHAN 3: UNBOXING
        // =========================================================

        System.out.println();
        System.out.println("===== PHAN 3: UNBOXING =====");

        Integer diem = 8;

        // Java tu dong doi Integer -> int
        int diemSo = diem;

        System.out.println("diem = " + diem);
        System.out.println("diemSo = " + diemSo);

        /*
         * Dong nay:
         *
         * int diemSo = diem;
         *
         * Java tu hieu nhu:
         *
         * int diemSo = diem.intValue();
         *
         * Integer -> int
         * Day la UNBOXING.
         */


        // =========================================================
        // PHAN 4: AUTOBOXING TRONG ARRAYLIST
        // =========================================================

        System.out.println();
        System.out.println("===== PHAN 4: AUTOBOXING TRONG ARRAYLIST =====");

        // ArrayList khong the viet ArrayList<int>
        // Chi duoc viet ArrayList<Integer>
        ArrayList<Integer> danhSachSo = new ArrayList<>();

        int so1 = 10;
        int so2 = 20;
        int so3 = 30;

        // int -> Integer
        // Java tu dong boxing khi dua vao ArrayList
        danhSachSo.add(so1);
        danhSachSo.add(so2);
        danhSachSo.add(so3);

        System.out.println("Danh sach so: " + danhSachSo);

        /*
         * Cac dong nay:
         *
         * danhSachSo.add(so1);
         * danhSachSo.add(so2);
         *
         * Java tu hieu nhu:
         *
         * danhSachSo.add(Integer.valueOf(so1));
         * danhSachSo.add(Integer.valueOf(so2));
         *
         * Day la AUTOBOXING.
         */


        // =========================================================
        // PHAN 5: UNBOXING KHI LAY TU ARRAYLIST
        // =========================================================

        System.out.println();
        System.out.println("===== PHAN 5: UNBOXING KHI LAY TU ARRAYLIST =====");

        // get(0) tra ve Integer
        // Nhung minh gan vao int
        // Java tu dong Integer -> int
        int giaTriDauTien = danhSachSo.get(0);

        System.out.println("Gia tri dau tien = " + giaTriDauTien);

        /*
         * Dong nay:
         *
         * int giaTriDauTien = danhSachSo.get(0);
         *
         * Java tu hieu nhu:
         *
         * Integer tam = danhSachSo.get(0);
         * int giaTriDauTien = tam.intValue();
         *
         * Day la UNBOXING.
         */


        // =========================================================
        // PHAN 6: TINH TONG TU ARRAYLIST
        // =========================================================

        System.out.println();
        System.out.println("===== PHAN 6: TINH TONG =====");

        int tong = 0;

        for (Integer so : danhSachSo) {
            // so la Integer
            // tong la int
            // Khi cong, Java tu dong unboxing Integer -> int
            tong += so;
        }

        System.out.println("Tong = " + tong);

        /*
         * O dong:
         *
         * tong += so;
         *
         * so la Integer
         * tong la int
         *
         * Java tu dong lay gia tri int ben trong Integer de cong.
         */


        // =========================================================
        // PHAN 7: AUTOBOXING / UNBOXING TRONG HASHMAP
        // =========================================================

        System.out.println();
        System.out.println("===== PHAN 7: HASHMAP =====");

        // Key la String
        // Value la Integer
        HashMap<String, Integer> soLanDangNhap = new HashMap<>();

        int soLanBanDau = 0;

        // int -> Integer
        // Java tu dong boxing so 0 thanh Integer 0
        soLanDangNhap.put("kien", soLanBanDau);

        System.out.println("So lan dang nhap cua kien: " + soLanDangNhap.get("kien"));

        /*
         * Dong nay:
         *
         * soLanDangNhap.put("kien", soLanBanDau);
         *
         * HashMap can Integer
         * soLanBanDau la int
         *
         * Java tu dong int -> Integer
         * Day la AUTOBOXING.
         */


        // =========================================================
        // PHAN 8: TANG GIA TRI TRONG HASHMAP
        // =========================================================

        System.out.println();
        System.out.println("===== PHAN 8: TANG SO LAN DANG NHAP =====");

        // get("kien") tra ve Integer
        // Gan vao int nen Java unboxing
        int soLan = soLanDangNhap.get("kien");

        // Tang len 1
        soLan++;

        // put vao Map
        // int -> Integer nen Java autoboxing
        soLanDangNhap.put("kien", soLan);

        System.out.println("So lan dang nhap moi cua kien: " + soLanDangNhap.get("kien"));

        /*
         * Luong chay:
         *
         * Integer -> int
         * int soLan = soLanDangNhap.get("kien");
         *
         * Tang:
         * soLan++;
         *
         * int -> Integer
         * soLanDangNhap.put("kien", soLan);
         *
         * Day la vi du ro nhat ve:
         * Unboxing + xu ly + Autoboxing
         */


        // =========================================================
        // PHAN 9: LOI KHI UNBOXING NULL
        // =========================================================

        System.out.println();
        System.out.println("===== PHAN 9: CAN THAN VOI null =====");

        Integer x = null;

        // Neu bo comment dong duoi thi se loi NullPointerException
        // int y = x;

        /*
         * Vi Java se co gang lam:
         *
         * int y = x.intValue();
         *
         * Nhung x = null
         * Nen khong co object Integer de goi intValue().
         */


        // Cach viet an toan:

        if (x != null) {
            int y = x;
            System.out.println("y = " + y);
        } else {
            System.out.println("x dang la null, khong the unboxing sang int.");
        }


        // =========================================================
        // PHAN 10: KET LUAN
        // =========================================================

        System.out.println();
        System.out.println("===== KET LUAN =====");

        System.out.println("Autoboxing: int -> Integer");
        System.out.println("Unboxing: Integer -> int");
        System.out.println("Hay gap trong ArrayList, HashMap, Set.");
    }
}