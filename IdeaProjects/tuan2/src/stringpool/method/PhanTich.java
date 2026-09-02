package stringpool.method;

public class PhanTich {
    private String Tinnhan;
    private String Tucantim;

    public PhanTich(String Tin, String Tucantim1) {
        Tinnhan = Tin;
        Tucantim = Tucantim1;
    }

    public void HienThi() {
        if (Tinnhan.isEmpty() || Tinnhan.isBlank()) {
            System.out.println("Chuỗi rỗng hoac chi co khoang trang");
            return;
        } else {
            System.out.println("chuoi ko rông");
        }
        System.out.println("Noi dung sau bo di khoan trang dau cuoi: " + Tinnhan.trim());
        System.out.println("Do dai truoc khi bo khoang trang" + Tinnhan.length() + "\n Do dai sau khi bo khoang trang: " + Tinnhan.trim().length());
        System.out.println("Noi dung viet hoa: " + Tinnhan.toUpperCase());
        System.out.println("Noi dung viet thuong: " + Tinnhan.toLowerCase());
        System.out.println("Noi dung co chua tu can tim : " + Tinnhan.contains(Tucantim));
        System.out.println("Noi dung co chua tu can tim  o vi tri thu: " + Tinnhan.indexOf(Tucantim));
        System.out.println("Noi dung co bat dau bang tu Dich vu: " + Tinnhan.startsWith("Dich vu"));
        System.out.println("Noi dung co ket thuc bang tu Bye: " + Tinnhan.endsWith("Bye"));
        System.out.println("Noi dung bi thay the tu te bang tu cai thien: " + Tinnhan.replace("te", "can cai thien hon"));
        System.out.println("Noi dung co phai la Dich Vu rat te phan biet hoa thuong : " + Tinnhan.equals("Dich Vu rat te"));
        System.out.println("Noi dung co phai la Dich Vu rat te ko phan biet hoa thuong : " + Tinnhan.equalsIgnoreCase("Dich Vu rat te"));
        System.out.println("Ky tu dau: " + Tinnhan.charAt(0));
        System.out.println("Ky tu cuoi: " + Tinnhan.charAt(Tinnhan.length() - 1));
        System.out.println("5 ky tu dau cua noi dung la: " + Tinnhan.substring(0, 4));
    }
}