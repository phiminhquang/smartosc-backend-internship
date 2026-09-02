package quanlyhoso;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        QuanLyHoSo ql = new QuanLyHoSo();

        Path duongDan = Path.of("Data.txt");

        try {
            List<String> danhSachDong = Files.readAllLines(duongDan);

            for (String dong : danhSachDong) {
                String[] thongTin = dong.split(";");

                String ma = thongTin[0];
                String ten = thongTin[1];
                int tuoi = Integer.parseInt(thongTin[2]);
                String gioiTinh = thongTin[3];
                String queQuan = thongTin[4];

                HoSo hoSo = new HoSo(ma, ten, tuoi, gioiTinh, queQuan);

                ql.themHoSo(hoSo);
            }

        } catch (IOException e) {
            System.out.println("Khong doc duoc file Data.txt");
        }

        int chon;

        do {
            System.out.println("\n========= QUAN LY HO SO =========");
            System.out.println("1. Hien thi danh sach");
            System.out.println("2. Tim theo ten");
            System.out.println("3. Tim theo ma");
            System.out.println("4. Them ho so");
            System.out.println("5. Xoa ho so theo ma");
            System.out.println("6. Sua que quan theo ma");
            System.out.println("7. Hien thi nguoi trong do tuoi lao dong");
            System.out.println("8. Dem so ho so");
            System.out.println("9. Kiem tra danh sach rong");
            System.out.println("10. Sap xep theo tuoi");
            System.out.println("11. Xoa tat ca");
            System.out.println("0. Thoat");

            System.out.print("Nhap lua chon: ");
            chon = Integer.parseInt(sc.nextLine());

            switch (chon) {
                case 1:
                    ql.hienThiDanhSach();
                    break;

                case 2:
                    System.out.print("Nhap ten can tim: ");
                    String tenCanTim = sc.nextLine();

                    ql.timTheoTen(tenCanTim);
                    break;

                case 3:
                    System.out.print("Nhap ma can tim: ");
                    String maCanTim = sc.nextLine();

                    ql.timTheoMa(maCanTim);
                    break;

                case 4:
                    System.out.print("Nhap ma: ");
                    String ma = sc.nextLine();

                    System.out.print("Nhap ten: ");
                    String ten = sc.nextLine();

                    System.out.print("Nhap tuoi: ");
                    int tuoi = Integer.parseInt(sc.nextLine());

                    System.out.print("Nhap gioi tinh: ");
                    String gioiTinh = sc.nextLine();

                    System.out.print("Nhap que quan: ");
                    String queQuan = sc.nextLine();

                    HoSo hoSoMoi = new HoSo(ma, ten, tuoi, gioiTinh, queQuan);

                    ql.themHoSo(hoSoMoi);
                    break;

                case 5:
                    System.out.print("Nhap ma can xoa: ");
                    String maCanXoa = sc.nextLine();

                    ql.xoaTheoMa(maCanXoa);
                    break;

                case 6:
                    System.out.print("Nhap ma can sua: ");
                    String maCanSua = sc.nextLine();

                    System.out.print("Nhap que quan moi: ");
                    String queQuanMoi = sc.nextLine();

                    ql.suaQueQuan(maCanSua, queQuanMoi);
                    break;

                case 7:
                    ql.hienThiNguoiLaoDong();
                    break;

                case 8:
                    ql.demSoHoSo();
                    break;

                case 9:
                    ql.kiemTraDanhSachRong();
                    break;

                case 10:
                    ql.sapXepTheoTuoi();
                    System.out.println("Da sap xep theo tuoi tang dan");
                    break;

                case 11:
                    ql.xoaTatCa();
                    System.out.println("Da xoa toan bo danh sach");
                    break;

                case 0:
                    System.out.println("Da thoat chuong trinh");
                    break;

                default:
                    System.out.println("Lua chon khong hop le");
            }

        } while (chon != 0);

        sc.close();
    }
}