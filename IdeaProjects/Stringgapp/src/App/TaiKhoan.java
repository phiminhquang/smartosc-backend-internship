package App;

public class TaiKhoan {
    private String Ten;
    private String Mk;
    public TaiKhoan(String ten, String mk){
        Ten = ten;
        Mk=mk;
    }
    public String getTen(){
        return Ten;
    }
    public String getMk(){
        return Mk;
    }
    public boolean kiemtra(String tennhapvao, String mknhapvao) {
        boolean dungTen = Ten.equals(tennhapvao);
        boolean dungMk = Mk.equals(mknhapvao);

        return dungTen && dungMk;
    }
}
