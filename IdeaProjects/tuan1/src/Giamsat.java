class Lonung{
    public String tenlo;
    public boolean trangthai;
    public int to;
    public static final int max = 2000;
    public static int solan = 0;
    Lonung(String ten,int nhiet){
        tenlo=ten;
        to=nhiet;
        trangthai=true;
    }
    private boolean kiemtra(int nhietdothucte) {
        solan++;
        if(nhietdothucte>=max){return false;}
        return true;
    }
    public void hienthi(int to){
        String trangthai1=kiemtra(to)?"an toan":"ko an toan ";
        System.out.println("Lo "+tenlo+" trang thai " + trangthai1 + " o lan " +solan);
    }
    public void tangnhiet(int tang,int lan){
        for (int i = 0; i < lan; i++) {
            to+=tang;
            hienthi(to);
        }
    }
    public void giamnhiet(int giam,int giammaylan){
        while(to>600){
           to-=giam;
           hienthi(to);
        }
        System.out.println("ha nhiet thanh cong o lan " + solan);
    }
}
public class Giamsat {
    public static void main(String[] args) {
        Lonung a = new Lonung("A", 1000);
        a.tangnhiet(300, 4);
        a.giamnhiet(500, 7);
    }
}

