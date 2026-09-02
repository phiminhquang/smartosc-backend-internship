import java.util.Scanner;

public class NhapMangChuoi {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Nhap so nguoi: ");
        int n = sc.nextInt();
        sc.nextLine(); // đọc bỏ dấu Enter còn lại

        String[] danhSachTen = new String[n];

        for (int i = 0; i < n; i++) {
            System.out.print("Nhap ten nguoi thu " + (i + 1) + ": ");
            danhSachTen[i] = sc.nextLine();
        }

        System.out.println("Danh sach:");

        for (int i = 0; i < n; i++) {
            System.out.println(danhSachTen[i]);
        }

        sc.close();
    }
}