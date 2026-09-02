package quanly;

public class MayLamLanh extends HeThong {
    private static final int MIN = 300;

    public MayLamLanh(String ten, int nhiet, int NhietMoilan1, int SoLan1) {
        super(ten, nhiet, NhietMoilan1, SoLan1);
    }

    @Override
    public void CanhBao(String noidung) {
        System.out.println("May lam lanh: " + Ten + noidung);
    }

    @Override
    public void Kiemtra() {
        for (int i = 0; i < SoLan; i++) {
            SoLanDo++;
            Nhiet-=NhietMoiLan;
            if (Nhiet < 0) {
                CanhBao(" nhiet do am la khong hop le");
                return;
            }
            if (Nhiet < MIN) {
                CanhBao( " lam lanh qua muc");
            }
            else {
            CanhBao( " may hoat dong binh thuong o lan thu: "+SoLanDo);
        }
    }
}
}