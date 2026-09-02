package DangKyKhoaHoc;

import java.security.PrivateKey;

public abstract class Kenh {
    protected String Ten;
    protected String tenKhoaHoc;
    protected String noiDung;
    public Kenh(String Ten1,String  tenKhoaHoc1,String noiDung1){
        Ten=Ten1;
        tenKhoaHoc=tenKhoaHoc1;
        noiDung=noiDung1;
    }
        public abstract void phat();
}
