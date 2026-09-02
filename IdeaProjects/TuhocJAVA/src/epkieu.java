import java.util.Scanner;

public class epkieu {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Nhập tên: ");
        String ten = sc.nextLine();

        System.out.print("Nhập tuổi: ");
        int tuoi = sc.nextInt();

        System.out.print("Nhập điện áp: ");
        double dienAp = sc.nextDouble();

        System.out.println("Tên: " + ten);
        System.out.println("Tuổi: " + tuoi);
        System.out.println("Điện áp: " + dienAp);

        sc.close();
    }
}