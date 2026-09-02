package quanlyhoso;

public class HoSo {
    private String ma;
    private String ten;
    private int tuoi;
    private String gioiTinh;
    private String queQuan;

    public HoSo(String ma, String ten, int tuoi, String gioiTinh, String queQuan) {
        this.ma = ma;
        this.ten = ten;
        this.tuoi = tuoi;
        this.gioiTinh = gioiTinh;
        this.queQuan = queQuan;
    }

    public String getMa() {
        return ma;
    }

    public String getTen() {
        return ten;
    }

    public int getTuoi() {
        return tuoi;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public String getQueQuan() {
        return queQuan;
    }

    public void setQueQuan(String queQuan) {
        this.queQuan = queQuan;
    }

    public void hienThi() {
        System.out.format("%-10s%-20s%-10d%-15s%-15s%n", ma, ten, tuoi, gioiTinh, queQuan);
    }
}