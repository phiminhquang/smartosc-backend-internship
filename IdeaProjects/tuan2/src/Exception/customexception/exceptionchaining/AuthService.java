package Exception.customexception.exceptionchaining;

import java.io.File;
import java.util.Scanner;
import java.io.FileNotFoundException;

public class AuthService {

    public void login(String username, String password) {
        System.out.println("[SYSTEM] Đang xác thực thông tin...");

        try {
            // CỐ TÌNH GÂY LỖI: Đọc một file không hề tồn tại trên máy
            Scanner fileScanner = new Scanner(new File("DuLieuBiMat.txt"));

        } catch (FileNotFoundException e) {
            // ❌ CÁCH LÀM TỒI (Junior):
            // throw new LoginFailedException("Đăng nhập thất bại!");
            // -> Mất sạch dấu vết cái file bị thiếu, sau này fix bug tìm mờ mắt!

            // ✅ CÁCH LÀM XỊN (Senior): Gói cái lỗi 'e' (FileNotFound) vào trong cái lỗi mới
            throw new LoginFailedException("Hệ thống đăng nhập đang bảo trì!", e);
        }
    }
}
