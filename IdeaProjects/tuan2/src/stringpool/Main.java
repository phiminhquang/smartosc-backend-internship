package stringpool;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // ==================================================
        // PHAN 1: STRING LITERAL VA STRING POOL
        // ==================================================

        System.out.println("===== PHAN 1: STRING POOL =====");

        // "kien" duoc dua vao String Pool
        String a = "kien";

        // Java thay "kien" da co trong String Pool
        // nen b dung lai object cua a
        String b = "kien";

        System.out.println("a = " + a);
        System.out.println("b = " + b);

        // == so sanh 2 bien co tro cung 1 object hay khong
        System.out.println("a == b: " + (a == b));

        // equals() so sanh noi dung String
        System.out.println("a.equals(b): " + a.equals(b));

        /*
         * Ket qua:
         *
         * a == b: true
         * a.equals(b): true
         *
         * Vi a va b cung tro toi object "kien" trong String Pool.
         */


        // ==================================================
        // PHAN 2: NEW STRING
        // ==================================================

        System.out.println();
        System.out.println("===== PHAN 2: NEW STRING =====");

        // new String("kien") ep Java tao object String moi
        // Object moi nay khong dung chung object "kien" cua a
        String c = new String("kien");

        System.out.println("a = " + a);
        System.out.println("c = " + c);

        // a va c co noi dung giong nhau
        // nhung la 2 object khac nhau
        System.out.println("a == c: " + (a == c));

        // equals() chi so sanh noi dung
        System.out.println("a.equals(c): " + a.equals(c));

        /*
         * Ket qua:
         *
         * a == c: false
         * a.equals(c): true
         *
         * Vi:
         * a tro toi "kien" trong String Pool.
         * c tro toi object moi duoc tao boi new String("kien").
         */


        // ==================================================
        // PHAN 3: STRING NHAP TU BAN PHIM
        // ==================================================

        System.out.println();
        System.out.println("===== PHAN 3: NHAP STRING TU BAN PHIM =====");

        // Tao tai khoan co san
        TaiKhoan taiKhoan = new TaiKhoan("kien", "123");

        System.out.print("Nhap ten dang nhap: ");
        String tenNhap = sc.nextLine();

        System.out.print("Nhap mat khau: ");
        String matKhauNhap = sc.nextLine();

        System.out.println();
        System.out.println("===== KIEM TRA TEN DANG NHAP =====");

        // tenNhap la String nguoi dung vua nhap
        // Noi dung co the la "kien"
        // nhung khong nen dung == de kiem tra
        System.out.println("tenNhap == \"kien\": " + (tenNhap == "kien"));

        // equals so sanh noi dung nen dung trong dang nhap
        System.out.println("tenNhap.equals(\"kien\"): " + tenNhap.equals("kien"));

        System.out.println();
        System.out.println("===== KET QUA DANG NHAP =====");

        // Day la cach dung dung trong thuc te
        if (taiKhoan.kiemTraDangNhap(tenNhap, matKhauNhap)) {
            System.out.println("Dang nhap thanh cong.");
        } else {
            System.out.println("Dang nhap that bai.");
        }

        sc.close();
    }
}