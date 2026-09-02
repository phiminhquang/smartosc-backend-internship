package DangKyKhoaHoc;

public class Mail extends  Kenh {
    public Mail(String Ten,String tenKhoaHoc){
        super(Ten,tenKhoaHoc,"Email: ");
    }

    @Override
    public void phat() {
        System.out.println(noiDung+ " ban co ten "+Ten+" da dang ky thanh cong khoa hoc "+tenKhoaHoc);
    }
}
