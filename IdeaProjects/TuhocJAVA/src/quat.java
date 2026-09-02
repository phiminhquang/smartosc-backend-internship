class QuatLamMat {
    private String tenQuat;
    private int tocDo;
    private boolean dangBat;

    public QuatLamMat(String tenQuat) {
        this.tenQuat = tenQuat;
        this.tocDo = 0;
        this.dangBat = false;
    }

    public void batQuat() {
        if (!dangBat) {
            dangBat = true;
            tocDo = 1;
            System.out.println("Đã bật quạt.");
        } else {
            System.out.println("Quạt đang bật rồi.");
        }
    }

    public void tatQuat() {
        if (dangBat) {
            dangBat = false;
            tocDo = 0;
            System.out.println("Đã tắt quạt.");
        } else {
            System.out.println("Quạt đang tắt rồi.");
        }
    }

    public void tangTocDo() {
        if (!dangBat) {
            System.out.println("Không thể tăng tốc vì quạt đang tắt.");
        } else if (tocDo < 3) {
            tocDo++;
            System.out.println("Tốc độ hiện tại: " + tocDo);
        } else {
            System.out.println("Quạt đang ở tốc độ tối đa.");
        }
    }

    public void giamTocDo() {
        if (!dangBat) {
            System.out.println("Quạt đang tắt.");
        } else if (tocDo > 1) {
            tocDo--;
            System.out.println("Tốc độ hiện tại: " + tocDo);
        } else {
            System.out.println("Quạt đang ở tốc độ thấp nhất.");
        }
    }

    public void hienThiTrangThai() {
        System.out.println("Tên quạt: " + tenQuat);
        System.out.println("Trạng thái: " + (dangBat ? "Đang bật" : "Đang tắt"));
        System.out.println("Tốc độ: " + tocDo);
    }
}
public class quat {
    public static void main(String[] args) {
        QuatLamMat quat1 = new QuatLamMat("Quạt phòng ngủ");

        quat1.hienThiTrangThai();

        quat1.batQuat();
        quat1.tangTocDo();
        quat1.tangTocDo();

        quat1.giamTocDo();
        quat1.hienThiTrangThai();

        quat1.tatQuat();
    }
}