package Stringapp;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Tao tai khoan co san
        TaiKhoan taiKhoan = new TaiKhoan("admin", "123456");
        String tenNhap;
        String matKhauNhap;;

        do {
            // Nhap thong tin dang nhap
            System.out.print("Nhap ten dang nhap: ");
            tenNhap = sc.nextLine();

            System.out.print("Nhap mat khau: ");

            matKhauNhap = sc.nextLine();

            if (!taiKhoan.dangNhap(tenNhap, matKhauNhap)) {
                System.out.println("Dang nhap that bai");
            }
        }
        while (!taiKhoan.dangNhap(tenNhap, matKhauNhap));

        System.out.println("Dang nhap thanh cong.");

        // Nhap tin nhan
        System.out.print("Nhap noi dung tin nhan: ");
        String noiDung = sc.nextLine();

        System.out.print("Nhap tu can tim: ");
        String tuCanTim = sc.nextLine();

        // Tao object TinNhan
        TinNhan tinNhan = new TinNhan(noiDung);

        // Goi ham xu ly String
        tinNhan.phanTich(tuCanTim);

        sc.close();
    }
}