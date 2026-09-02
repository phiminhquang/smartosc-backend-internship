package Exception;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int luaChon = -1;

        do {
            System.out.println();
            System.out.println("===== LUYEN EXCEPTION HANDLING =====");
            System.out.println("1. InputMismatchException");
            System.out.println("2. NumberFormatException");
            System.out.println("3. IllegalArgumentException");
            System.out.println("4. ArithmeticException");
            System.out.println("5. NullPointerException");
            System.out.println("6. IOException");
            System.out.println("0. Thoat");
            System.out.print("Nhap lua chon: ");

            try {
                // nextInt chi nhan so nguyen
                // Neu nhap abc thi se loi InputMismatchException
                luaChon = sc.nextInt();

                // Xoa phim Enter con du sau nextInt()
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Lua chon phai la so nguyen.");

                // Xoa du lieu sai, vi du abc, con trong Scanner
                sc.nextLine();

                luaChon = -1;
            }

            switch (luaChon) {
                case 1:
                    viDuInputMismatchException(sc);
                    break;

                case 2:
                    viDuNumberFormatException(sc);
                    break;

                case 3:
                    viDuIllegalArgumentException(sc);
                    break;

                case 4:
                    viDuArithmeticException(sc);
                    break;

                case 5:
                    viDuNullPointerException();
                    break;

                case 6:
                    viDuIOException();
                    break;

                case 0:
                    System.out.println("Da thoat chuong trinh.");
                    break;

                default:
                    if (luaChon != -1) {
                        System.out.println("Lua chon khong hop le.");
                    }
                    break;
            }

        } while (luaChon != 0);

        sc.close();
    }

    // =========================================================
    // 1. InputMismatchException
    // Xay ra khi Scanner dang can so nhung nguoi dung nhap chu
    // =========================================================
    public static void viDuInputMismatchException(Scanner sc) {
        System.out.println();
        System.out.println("===== INPUT MISMATCH EXCEPTION =====");

        System.out.print("Nhap tuoi cua ban: ");

        try {
            // Neu nhap 20 -> chay binh thuong
            // Neu nhap abc -> loi InputMismatchException
            int tuoi = sc.nextInt();

            sc.nextLine();

            System.out.println("Tuoi cua ban la: " + tuoi);
        } catch (InputMismatchException e) {
            System.out.println("Loi: Tuoi phai la so nguyen.");

            // Xoa abc khoi Scanner
            sc.nextLine();
        }

        /*
         * Vi du:
         *
         * Nhap tuoi: abc
         *
         * nextInt() khong doc duoc abc
         * -> tao InputMismatchException
         * -> nhay vao catch
         * -> chuong trinh khong bi dung
         */
    }

    // =========================================================
    // 2. NumberFormatException
    // Xay ra khi doi String sang so bang parseInt / parseDouble
    // =========================================================
    public static void viDuNumberFormatException(Scanner sc) {
        System.out.println();
        System.out.println("===== NUMBER FORMAT EXCEPTION =====");

        System.out.print("Nhap diem: ");

        // nextLine() luon lay du lieu dang String
        String duLieuNhap = sc.nextLine();

        try {
            // Neu nhap "8.5" -> doi thanh double 8.5
            // Neu nhap "abc" -> loi NumberFormatException
            double diem = Double.parseDouble(duLieuNhap);

            System.out.println("Diem cua ban la: " + diem);
        } catch (NumberFormatException e) {
            System.out.println("Loi: Diem phai la so.");

            /*
             * Vi du:
             *
             * duLieuNhap = "abc"
             *
             * Double.parseDouble("abc")
             * -> khong doi duoc
             * -> NumberFormatException
             */
        }
    }

    // =========================================================
    // 3. IllegalArgumentException
    // Dung khi du lieu la so, nhung khong dung quy tac minh dat ra
    // =========================================================
    public static void viDuIllegalArgumentException(Scanner sc) {
        System.out.println();
        System.out.println("===== ILLEGAL ARGUMENT EXCEPTION =====");

        System.out.print("Nhap so tien muon nap: ");

        try {
            double soTienNap = sc.nextDouble();
            sc.nextLine();

            napTien(soTienNap);

            System.out.println("Nap tien thanh cong.");
        } catch (InputMismatchException e) {
            System.out.println("Loi: So tien phai la so.");
            sc.nextLine();
        } catch (IllegalArgumentException e) {
            // e.getMessage() lay noi dung trong throw
            System.out.println("Loi: " + e.getMessage());
        }
    }

    // Ham nay khong nhap Scanner
    // No chi nhan soTienNap va kiem tra quy tac
    public static void napTien(double soTienNap) {
        // -50000 van la so
        // Nhung no khong hop le theo quy tac nap tien
        if (soTienNap <= 0) {
            throw new IllegalArgumentException("So tien nap phai lon hon 0.");
        }

        System.out.println("Da nap " + soTienNap + " VND");
    }

    // =========================================================
    // 4. ArithmeticException
    // Xay ra khi chia so nguyen cho 0
    // =========================================================
    public static void viDuArithmeticException(Scanner sc) {
        System.out.println();
        System.out.println("===== ARITHMETIC EXCEPTION =====");

        System.out.print("Nhap so a: ");

        try {
            int a = sc.nextInt();
            sc.nextLine();

            System.out.print("Nhap so b: ");
            int b = sc.nextInt();
            sc.nextLine();

            // Neu b = 0 thi loi ArithmeticException
            int ketQua = a / b;

            System.out.println("Ket qua = " + ketQua);
        } catch (InputMismatchException e) {
            System.out.println("Loi: Ban phai nhap so nguyen.");
            sc.nextLine();
        } catch (ArithmeticException e) {
            System.out.println("Loi: Khong the chia cho 0.");
        }

        /*
         * Vi du:
         *
         * a = 10
         * b = 0
         *
         * 10 / 0
         * -> ArithmeticException
         */
    }

    // =========================================================
    // 5. NullPointerException
    // Xay ra khi dung object dang la null
    // =========================================================
    public static void viDuNullPointerException() {
        System.out.println();
        System.out.println("===== NULL POINTER EXCEPTION =====");

        String ten = null;

        try {
            // ten dang la null
            // Nen khong the goi ten.length()
            int doDai = ten.length();

            System.out.println("Do dai ten: " + doDai);
        } catch (NullPointerException e) {
            System.out.println("Loi: Bien ten dang la null, khong the goi length().");
        }

        /*
         * String ten = null;
         *
         * ten.length()
         * -> ten khong tro toi object String nao
         * -> NullPointerException
         */
    }

    // =========================================================
    // 6. IOException
    // Xay ra khi doc/ghi file bi loi
    // =========================================================
    public static void viDuIOException() {
        System.out.println();
        System.out.println("===== IO EXCEPTION =====");

        // Day la file ma co the chua ton tai
        Path path = Path.of("khong_co_file_nay.txt");

        try {
            // Neu file khong ton tai thi se co IOException
            List<String> danhSachDong = Files.readAllLines(path);

            System.out.println("Noi dung file:");

            for (String dong : danhSachDong) {
                System.out.println(dong);
            }
        } catch (IOException e) {
            System.out.println("Loi: Khong the doc file.");
            System.out.println("Co the file chua ton tai hoac duong dan sai.");
        }
    }
}
