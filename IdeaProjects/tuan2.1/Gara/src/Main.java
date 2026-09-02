import gara.Gara;
import gara.HeThong;
import gara.KyThuatVien;
import gara.Xe;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        KyThuatVien ktv1 = new KyThuatVien(1,"PMQ",2);
        KyThuatVien ktv2 = new KyThuatVien(2,"BDQ",1);
        KyThuatVien ktv3 = new KyThuatVien(3,"T",3);
        Xe xe1 = new Xe(11,"Mer","Thanh");
        Xe xe2 = new Xe(22,"Lambo","Dung");
        Xe xe3 = new Xe(33,"Honda","Cuong");
        Xe xe4 = new Xe(44,"Vin","Quang");
        Gara gara1 = new Gara(111,"A",ktv1);
        gara1.addCar(xe1);
        gara1.addCar(xe2);
        Gara gara2 = new Gara(222,"B",ktv2);
        gara2.addCar(xe3);
        Gara gara3 = new Gara(333,"C",ktv3);
        gara3.addCar(xe4);
        List<Gara> garas = new ArrayList<>();
        garas.add(gara1);
        garas.add(gara2);
        garas.add(gara3);
        for (Gara gara : garas){
            System.out.println("Ten gara "+gara.getTenGara()+" | Ky thuat vien phu trach: "+gara.getA());
            System.out.println("Danh sach xe dang sua:");
            gara.showInforXe();
            System.out.println("-----------------------------");
        }
    }
}
