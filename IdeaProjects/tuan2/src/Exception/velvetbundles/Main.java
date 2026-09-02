package Exception.velvetbundles;

import java.util.Scanner;
import java.util.InputMismatchException;

public class Main {
    public static void main(String[] args) {
        // Scanner: Dùng để đọc dữ liệu đầu vào từ bàn phím
        Scanner sc = new Scanner(System.in);

        try {
            // println: Print line - In ra màn hình và tự động xuống dòng
            // "--- HANDMADE MATERIALS CALCULATOR ---" -> Máy tính tiền nguyên liệu handmade
            // (materials = nguyên vật liệu, calculator = máy tính)
            System.out.println("--- HANDMADE MATERIALS CALCULATOR ---");

            // print: In ra màn hình nhưng KHÔNG xuống dòng (để số người dùng nhập nằm ngay cạnh chữ)
            // "Enter the total amount (VND): " -> Nhập tổng số tiền (VND):
            // (enter = nhập vào, total = tổng, amount = số lượng/số tiền)
            System.out.print("Enter the total amount (VND): ");
            int totalAmount = sc.nextInt();

            // "Enter the quantity of velvet bundles: " -> Nhập số lượng bó kẽm nhung:
            // (quantity = số lượng, velvet = vải nhung, bundle = bó/gói)
            System.out.print("Enter the quantity of velvet bundles: ");
            int quantity = sc.nextInt();

            // Variable: unitPrice -> Đơn giá
            // (unit = đơn vị, price = giá tiền)
            // Thực hiện phép chia: Đơn giá = Tổng tiền / Số lượng
            int unitPrice = totalAmount / quantity;

            // "Success! The unit price per bundle is: " -> Thành công! Đơn giá mỗi bó là:
            // (success = thành công, per = mỗi/trên một)
            System.out.println("Success! The unit price per bundle is: " + unitPrice + " VND");

        } catch (ArithmeticException e) {
            // Arithmetic = Toán học, Exception = Ngoại lệ (Lỗi)
            // Nếu 'quantity' (số lượng) = 0, lỗi sẽ tự động văng vào khối catch này.

            // "Error: Cannot divide by zero!" -> Lỗi: Không thể chia cho 0!
            // (error = lỗi, cannot = không thể, divide = chia, zero = số không)
            System.out.println("Error: Cannot divide by zero!");

        } catch (InputMismatchException e) {
            // Input = Đầu vào, Mismatch = Không khớp
            // Đề phòng người dùng gõ chữ thay vì gõ số (như ôn tập Bài 1)

            // "Error: Please enter a valid integer!" -> Lỗi: Vui lòng nhập một số nguyên hợp lệ!
            // (please = làm ơn/vui lòng, valid = hợp lệ, integer = số nguyên)
            System.out.println("Error: Please enter a valid integer!");
        }
    }
}
