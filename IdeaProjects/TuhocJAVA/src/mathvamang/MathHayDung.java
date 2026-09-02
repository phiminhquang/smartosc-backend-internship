public class MathHayDung {
    public static void main(String[] args) {

        int a = -15;
        int b = 8;
        double x = 3.6;
        double y = -3.6;

        // 1. Math.abs(): lấy giá trị tuyệt đối
        System.out.println("abs(-15) = " + Math.abs(a));
        System.out.println("abs(-3.6) = " + Math.abs(y));

        // 2. Math.max(): lấy số lớn hơn giữa 2 số
        System.out.println("max(-15, 8) = " + Math.max(a, b));

        // 3. Math.min(): lấy số nhỏ hơn giữa 2 số
        System.out.println("min(-15, 8) = " + Math.min(a, b));

        // 4. Math.pow(a, b): a mũ b
        System.out.println("2 mu 3 = " + Math.pow(2, 3));
        System.out.println("5 mu 2 = " + Math.pow(5, 2));

        // 5. Math.sqrt(): căn bậc 2
        System.out.println("Can bac 2 cua 25 = " + Math.sqrt(25));

        // 6. Math.cbrt(): căn bậc 3
        System.out.println("Can bac 3 cua 27 = " + Math.cbrt(27));

        // 7. Math.round(): làm tròn gần nhất
        System.out.println("round(3.6) = " + Math.round(x));
        System.out.println("round(3.2) = " + Math.round(3.2));
        System.out.println("round(-3.6) = " + Math.round(y));

        // 8. Math.floor(): luôn làm tròn xuống
        System.out.println("floor(3.6) = " + Math.floor(x));
        System.out.println("floor(-3.6) = " + Math.floor(y));

        // 9. Math.ceil(): luôn làm tròn lên
        System.out.println("ceil(3.6) = " + Math.ceil(x));
        System.out.println("ceil(-3.6) = " + Math.ceil(y));

        // 10. (int) Math.floor(): floor trả về double, ép về int nếu cần
        int lamTronXuong = (int) Math.floor(x);
        System.out.println("(int) floor(3.6) = " + lamTronXuong);

        // 11. (int): chỉ bỏ phần thập phân, tiến về 0
        System.out.println("(int) 3.6 = " + (int) x);
        System.out.println("(int) -3.6 = " + (int) y);

        // 12. Math.random(): tạo số ngẫu nhiên từ 0.0 đến nhỏ hơn 1.0
        double soNgauNhien = Math.random();
        System.out.println("So ngau nhien tu 0.0 den < 1.0 = " + soNgauNhien);

        // 13. Tạo số nguyên ngẫu nhiên từ 1 đến 10
        int randomTu1Den10 = (int) (Math.random() * 10) + 1;
        System.out.println("So ngau nhien tu 1 den 10 = " + randomTu1Den10);

        // 14. Công thức tổng quát tạo số ngẫu nhiên từ min đến max
        int min = 5;
        int max = 20;
        int randomTu5Den20 = (int) (Math.random() * (max - min + 1)) + min;
        System.out.println("So ngau nhien tu 5 den 20 = " + randomTu5Den20);

        // 15. Math.PI: số Pi
        double banKinh = 5;
        double dienTichHinhTron = Math.PI * Math.pow(banKinh, 2);
        double chuViHinhTron = 2 * Math.PI * banKinh;

        System.out.println("PI = " + Math.PI);
        System.out.println("Dien tich hinh tron r = 5: " + dienTichHinhTron);
        System.out.println("Chu vi hinh tron r = 5: " + chuViHinhTron);

        // 16. Math.hypot(a, b): tính cạnh huyền tam giác vuông
        double canhHuyen = Math.hypot(3, 4);
        System.out.println("Canh huyen tam giac 3 - 4 = " + canhHuyen);

        // 17. Math.signum(): xem số âm, dương hay bằng 0
        System.out.println("signum(10) = " + Math.signum(10));
        System.out.println("signum(-10) = " + Math.signum(-10));
        System.out.println("signum(0) = " + Math.signum(0));

        // 18. Math.toRadians(): đổi độ sang radian
        double gocDo = 90;
        double gocRadian = Math.toRadians(gocDo);
        System.out.println("90 do doi sang radian = " + gocRadian);

        // 19. Math.toDegrees(): đổi radian sang độ
        System.out.println("PI / 2 doi sang do = " + Math.toDegrees(Math.PI / 2));

        // 20. sin, cos, tan dùng radian
        System.out.println("sin(90 do) = " + Math.sin(Math.toRadians(90)));
        System.out.println("cos(0 do) = " + Math.cos(Math.toRadians(0)));
        System.out.println("tan(45 do) = " + Math.tan(Math.toRadians(45)));

        // 21. Math.log(): log cơ số e
        System.out.println("log(e) = " + Math.log(Math.E));

        // 22. Math.log10(): log cơ số 10
        System.out.println("log10(100) = " + Math.log10(100));

        // 23. Math.exp(x): e mũ x
        System.out.println("e mu 1 = " + Math.exp(1));
    }
}