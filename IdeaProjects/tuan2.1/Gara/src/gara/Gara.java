package gara;

import java.util.ArrayList;
import java.util.List;

public class Gara {
    private int id;
    private String tenGara;
    private KyThuatVien a;
    List<Xe> bangDanhSachXe;;
    public  Gara(int id1,String tenGara1,KyThuatVien a1){
        id = id1;
        tenGara = tenGara1;
        a = a1;
    }
    public void addCar(Xe them){
        if(bangDanhSachXe==null){
            bangDanhSachXe = new ArrayList<>();
        }
        bangDanhSachXe.add(them);
    }
    public int getId(){
        return id;
    }
    public String getTenGara(){
        return tenGara;
    }
    public String getA(){
        return a.ten;
    }
    public void showInforXe(){
        for (Xe a : bangDanhSachXe){
            System.out.println("ID: "+ a.id+" | Ten chu: "+a.tenChu+" | Hang xe: "+a.ten);
        }
    }
}
