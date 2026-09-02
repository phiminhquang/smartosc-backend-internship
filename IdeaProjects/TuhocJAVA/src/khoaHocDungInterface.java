interface KenhThongBao1{
    void kenhGui(String noidung);
}
class GuiEmail implements KenhThongBao1{
    public void kenhGui(String noidung){
        System.out.println("Gửi Email: "+noidung);
    }
}
class guiSms implements KenhThongBao1{
    public void kenhGui(String noidung){
        System.out.println("Gửi Sms: "+noidung);
    }
}
class dangky{
    private KenhThongBao1 kenh;
    private String ten;
    private String tenKhoaHoc;
    public dangky(KenhThongBao1 kenh1,String ten,String tenKhoaHoc){

        kenh=kenh1;
        this.ten = ten;
        this.tenKhoaHoc=tenKhoaHoc;
    }
    public void hienthi(){
        kenh.kenhGui("học viên "+ten+" đã đăng ký thành công khoá "+tenKhoaHoc);
    }
}
public class khoaHocDungInterface {
    public static void main(String[] args) {
        KenhThongBao1 email = new GuiEmail();
        dangky svien1 = new dangky(email,"PMQ","JAVA");
        svien1.hienthi();

    }
}
