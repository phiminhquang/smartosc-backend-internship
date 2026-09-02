package quanly;

import java.util.Scanner;

public class QuanLy {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a,b;;
        System.out.print("Nhap so may nung: ");
        a = sc.nextInt();
        System.out.print("Nhap so may lam lanh: ");
        b = sc.nextInt();
        sc.nextLine();
        HeThong[] ds = new HeThong[a+b];
        for (int i = 0; i < a; i++) {
            System.out.print("Ten may nung thu "+(i+1));
            String ten = sc.nextLine();
            System.out.print("Nhiet do may nung: ");
            int Nhiet = sc.nextInt();
            System.out.print("Moi lan tang bao nhieu do: ");
            int NhietMoiLan = sc.nextInt();
            System.out.print("So lan ban muon tang: ");
            int SoLan = sc.nextInt();
            sc.nextLine();
            ds[i]=new MayNung(ten,Nhiet,NhietMoiLan,SoLan);
        }
        for (int x = a; x < a+b; x++) {
            System.out.print("Ten may lam lanh thu "+(x-a+1));
            String ten = sc.nextLine();
            System.out.print("Nhiet do may lam lanh: ");
            int Nhiet = sc.nextInt();
            System.out.print("Moi lan giam bao nhieu do: ");
            int NhietMoiLan = sc.nextInt();
            System.out.print("So lan ban muon giam: ");
            int SoLan = sc.nextInt();
            sc.nextLine();
            ds[x]=new MayLamLanh(ten,Nhiet,NhietMoiLan,SoLan);
        }
        Quet quet = new Quet(ds);
        quet.HienThi();

        sc.close();
    }
}
