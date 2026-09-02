package DangKyKhoaHoc;

public class Sms extends  Kenh {
    public Sms(String Ten,String tenKhoaHoc){
        super(Ten,tenKhoaHoc,"Sms: ");
    }

    @Override
    public void phat() {
        System.out.println(noiDung+ "ban co ten "+Ten+" da dang ky thanh cong khoa hoc "+tenKhoaHoc);
    }
}
