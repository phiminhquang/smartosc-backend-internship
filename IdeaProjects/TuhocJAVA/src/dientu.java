interface Bophatth{
    void phat();
}
class Lkien{
    String ten;
    public Lkien(String tenlk){
        ten = tenlk;
    }
    public void hienthi (){
        System.out.println("ten linh kien là:"+ ten);
    }
}
class machphatvt extends Lkien implements Bophatth {
    double tanso;

    public machphatvt(String tenlk, double tso) {
        super(tenlk);
        tanso = tso;
    }

    @Override
    public void phat() {
        System.out.println("mạch" + ten + "voi tan so" + tanso);
    }
    public void phat(String thongdiep) {
        System.out.println("mạch"+ten+"phat thông điêp:"+thongdiep);
    }
    public void phat(double cuongdo) {
        System.out.println("mạch"+ten+"đang phat với cương dọ"+cuongdo+"Db");
    }
}
public class dientu {
    public static void main(String[] args) {
        machphatvt mach = new machphatvt("RF", 30);
        mach.hienthi();
        mach.phat();
        mach.phat(3.4);
        mach.phat("chanbomdi");
    }
}