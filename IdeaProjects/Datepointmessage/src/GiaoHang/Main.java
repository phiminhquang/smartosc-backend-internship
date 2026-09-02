package GiaoHang;

import java.awt.Point;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        DateTimeFormatter nhap = DateTimeFormatter.ofPattern("d/M/yyyy");
        DateTimeFormatter xuat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        System.out.println("Nhap ngay lam don: ");
        String ngayLamDon = sc.nextLine();
        LocalDate ngay = LocalDate.parse(ngayLamDon,nhap);;
        // Tao object Date va truyen ngay lam don vao constructor
        Date donHang = new Date(ngay);

        // Nhap so ngay can giao hang
        System.out.print("Nhap so ngay giao du kien: ");
        int soNgayGiao = sc.nextInt();

        // Nhap toa do kho
        System.out.print("Nhap x cua kho: ");
        int xKho = sc.nextInt();

        System.out.print("Nhap y cua kho: ");
        int yKho = sc.nextInt();

        // Tao diem toa do kho
        Point kho = new Point(xKho, yKho);

        // Nhap toa do khach hang
        System.out.print("Nhap x cua khach: ");
        int xKhach = sc.nextInt();

        System.out.print("Nhap y cua khach: ");
        int yKhach = sc.nextInt();

        // Tao diem toa do khach hang
        Point khach = new Point(xKhach, yKhach);

        // Tao object tinh khoang cach giua kho va khach
        KhoangCach khoangCach = new KhoangCach(kho, khach);

        // In ket qua tren mot hang
        System.out.println(
                "Ngay lam don: " + donHang.getDate().format(xuat)
                        + " | Ngay giao du kien: " + donHang.ngayDuKien(soNgayGiao).format(xuat)
                        + " | Toa do kho: (" + kho.x + ", " + kho.y + ")"
                        + " | Toa do khach: (" + khach.x + ", " + khach.y + ")"
                        + " | Khoang cach: " + khoangCach.tinhKhoangCach()
        );

        // Dong Scanner sau khi dung xong
        sc.close();
    }
}