package stringpool.method;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Thiet lap tai khoan bang phim Enter.");
        sc.nextLine();
        System.out.print("Quy khach vui long dat ten tai khoan: ");
        String Name = sc.nextLine();
        System.out.print("Quy khach vui long dat mat khau: ");
        String Mk = sc.nextLine();
        TaiKhoan a1 = new TaiKhoan(Name,Mk);
        System.out.println("Thong tin cua ban: \nTai khoan: "+a1.getTen()+"\nMat khau: "+a1.getMk()+"\nBam Enter de xac nhan");
        sc.nextLine();
        String nhaptk;
        String nhapmk;
        do {
            System.out.print("Vui long nhap ten: ");
            nhaptk = sc.nextLine();
            System.out.print("Vui long nhap mat khau: ");
            nhapmk = sc.nextLine();
            if (!a1.kiemtra(nhaptk,nhapmk)){
                System.out.println("Dang nhap that bai vui long dang nhap lai");
            }
        }
        while (!a1.kiemtra(nhaptk,nhapmk));
        System.out.print("Dang nhap thanh cong nhap noi dung ban muon gui: ");
        String noidung = sc.nextLine();
        System.out.println("-------------------------------------------------");
        System.out.println("Phan tich noi dung:");
        PhanTich phanTich = new PhanTich(noidung,"t");
        phanTich.HienThi();
    }
}