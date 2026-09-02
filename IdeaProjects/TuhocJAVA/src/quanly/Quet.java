package quanly;

public class Quet {
    private HeThong[] ds; // mang chua tat ca may

    public Quet(HeThong[] ds) { // nhan ca mang may vao
        this.ds = ds;
    }

    public void HienThi() { // khong can truyen ds nua vi da luu o tren
        for (HeThong dss : ds) {
            dss.Kiemtra(); // moi may tu kiem tra theo loai cua no
            System.out.println("-------------------");
        }
    }
}