package Exception.customexception.gomloi;

public class Main {
    public static void main(String[] args) {

        int userAge = -5;
        int quantity = -10;

        try {
            // Biến errorMessage đóng vai trò là "Cái túi rác"
            // Ban đầu túi rỗng (chuỗi rỗng "")
            String errorMessage = "";

            System.out.println("[SYSTEM] Validating data...");

            // BƯỚC 1: KIỂM TRA TỪNG LỖI NHƯNG KHÔNG DÙNG 'THROW'
            if (userAge < 0) {
                // Thay vì ném bom, ta chỉ việc nhét thêm câu chữ vào túi rác
                // (Dùng \n để xuống dòng cho đẹp)
                errorMessage = errorMessage + "- Age cannot be negative!\n";
            }

            if (quantity < 0) {
                // Chương trình vẫn chạy mượt mà xuống đây và nhét tiếp lỗi số 2 vào túi
                errorMessage = errorMessage + "- Quantity cannot be negative!\n";
            }

            // BƯỚC 2: TỔNG KẾT VÀ NÉM BOM CỤM
            // isEmpty() = có trống rỗng hay không?
            // Dấu chấm than (!) = Phủ định (Không trống rỗng)
            if (!errorMessage.isEmpty()) {
                // Nếu túi rác có chứa lỗi, lúc này ta mới gom tất cả lại và THROW 1 lần duy nhất!
                throw new ValidationException("Data validation failed with the following errors:\n" + errorMessage);
            }

            // Nếu túi rác rỗng (tức là không có lỗi nào), dòng này mới được chạy
            System.out.println("[SYSTEM] All data is valid! Proceeding...");

        } catch (ValidationException e) {
            // Bắt cái túi rác và in ra toàn bộ hậu quả
            System.out.println("❌ ERROR REPORT:\n" + e.getMessage());
        }
    }
}
