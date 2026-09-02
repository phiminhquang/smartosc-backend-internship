package Exception.ex.ex1;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BankAccount a = new BankAccount(1000);
        int b = 0;
        while (true) {
            try {
                System.out.print("Nhap so tien ban muon rut: ");
                b = sc.nextInt();
                a.withdraw(b);
                break;
            } catch (InputMismatchException e) {
                System.out.println("Vui long nhap so");
                sc.nextLine();
            } catch (InvalidAmountException e) {
                System.out.println("LỖI: " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.println("LỖI: " + e.getMessage());
            } catch (Exception e){
                System.out.println("Loi ko xac dinh");
            } finally {
                System.out.println("GIAO DỊCH KET THUC. NGẮT KẾT NỐI SEVER.");
            }
        }
    }
}
