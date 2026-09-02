class diode1{
    private String loaivatlieu;
    private double dienapnguong;
    Double dienapdauvao;
    public diode1(String ten,double nguong){
        loaivatlieu = ten;
        dienapnguong = nguong;
        dienapdauvao = null;
    }
    public void capnguon(double vao){
        dienapdauvao = vao;
    }
    public boolean on (){
        if(dienapdauvao == null){
            return false;
        }
        return dienapdauvao >= dienapnguong;
    }
    public void hienthi (){
        System.out.println("linh kien ten la:"+loaivatlieu);
        System.out.println("dien ap nguong"+dienapnguong);
        System.out.println("dien ap thuc te"+dienapdauvao);
        System.out.println((on()? "dan":"ko dan"));
    }
}
public class Bandan {
    public static void main(String[] args) {
        diode1 si = new diode1("silic",0.7);
        si.hienthi();
        si.capnguon(0.8);
        si.hienthi();
        si.capnguon(0.6);
        si.hienthi();
    }
}
