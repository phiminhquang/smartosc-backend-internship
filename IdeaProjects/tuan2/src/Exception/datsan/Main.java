package Exception.datsan;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Nhap ten san ban muon dat: ");
        String b = sc.nextLine(); // Khai báo và gán luôn trên 1 dòng

        while (true) {
            try {
                System.out.print("Nhap so gio ban muon dat: ");
                double a = sc.nextDouble(); // Khai báo ngay tại đây

                // KHI ĐÃ CHẠY ĐẾN ĐÂY, TỨC LÀ CHẮC CHẮN 'a' ĐÃ LÀ SỐ.
                // Ta khởi tạo object và in ra luôn tại đây!
                DatSan c = new DatSan(b, a);
                c.show();

                // Mọi việc xong xuôi thì break để kết thúc chương trình
                break;

            } catch (InputMismatchException e) {
                System.out.println("Loi: Vui long nhap so!");
                sc.nextLine(); // Vẫn phải có dòng này để hút rác bộ nhớ nhé
            }
        }
    }
}