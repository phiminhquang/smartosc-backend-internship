package search;

public class HoSo {
    private String ten;
    private int tuoi;
    private String gioiTinh;
    private String queQuan;
    public HoSo(String ten1,int tuoi1,String gioiTinh1,String queQuan1){
        ten = ten1;
        tuoi = tuoi1;
        queQuan = queQuan1;
        gioiTinh = gioiTinh1;
    }
    public String doTuoiLaoDong(){
        if(tuoi>=18&&tuoi<=30){
            return  "Trong do tuoi lao dong";
        }
        else {
            return "Khong trong do tuoi lao dong";
        }
    }
    public String getGioiTinh(){
        return gioiTinh;
    }
    public String getTen(){
        return ten;
    }
    public int getTuoi(){
        return tuoi;
    }
    public String getQueQuan(){
        return queQuan;
    }
}
