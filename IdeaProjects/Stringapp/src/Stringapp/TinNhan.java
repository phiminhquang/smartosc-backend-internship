package Stringapp;

public class TinNhan {
    private String noiDung;

    public TinNhan(String noiDung) {
        this.noiDung = noiDung;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public void phanTich(String tuCanTim) {
        String noiDungDaTrim = noiDung.trim();
        String tuTimDaTrim = tuCanTim.trim();

        System.out.println("\n----- PHAN TICH TIN NHAN -----");

        // isEmpty(): kiem tra chuoi co rong hoan toan khong
        System.out.println("Chuoi rong: " + noiDung.isEmpty());

        // isBlank(): rong hoac chi co khoang trang
        System.out.println("Chuoi blank: " + noiDung.isBlank());

        // trim(): xoa khoang trang dau va cuoi
        System.out.println("Noi dung sau trim: " + noiDungDaTrim);

        // length(): dem so ky tu
        System.out.println("Do dai ban dau: " + noiDung.length());
        System.out.println("Do dai sau trim: " + noiDungDaTrim.length());

        // Khong duoc dung charAt, substring neu chuoi rong
        if (noiDungDaTrim.isEmpty()) {
            System.out.println("Tin nhan khong co noi dung de phan tich.");
            return;
        }

        // toUpperCase() va toLowerCase()
        System.out.println("Viet hoa: " + noiDungDaTrim.toUpperCase());
        System.out.println("Viet thuong: " + noiDungDaTrim.toLowerCase());

        // contains(): kiem tra co chua tu can tim khong
        System.out.println("Co chua \"" + tuTimDaTrim + "\": " + noiDungDaTrim.contains(tuTimDaTrim));

        // indexOf(): tim vi tri xuat hien dau tien
        System.out.println("Vi tri cua \"" + tuTimDaTrim + "\": " + noiDungDaTrim.indexOf(tuTimDaTrim));

        // startsWith() va endsWith()
        System.out.println("Bat dau bang \"Xin\": " + noiDungDaTrim.startsWith("Xin"));

        System.out.println("Ket thuc bang \"Java\": " + noiDungDaTrim.endsWith("Java"));

        // replace(): thay noi dung
        System.out.println("Thay Java thanh C++: " + noiDungDaTrim.replace("Java", "C++"));

        // equals(): so sanh co phan biet hoa thuong
        System.out.println("Bang \"Xin chao Java\": " + noiDungDaTrim.equals("Xin chao Java"));

        // equalsIgnoreCase(): so sanh khong phan biet hoa thuong
        System.out.println("Bang \"XIN CHAO JAVA\": " + noiDungDaTrim.equalsIgnoreCase("XIN CHAO JAVA"));

        // charAt(): lay ky tu theo vi tri
        System.out.println("Ky tu dau: " + noiDungDaTrim.charAt(0));

        System.out.println("Ky tu cuoi: " + noiDungDaTrim.charAt(noiDungDaTrim.length() - 1));

        // substring(): cat chuoi
        if (noiDungDaTrim.length() >= 4) {
            System.out.println("4 ky tu dau: " + noiDungDaTrim.substring(0, 4));
        }
    }
}