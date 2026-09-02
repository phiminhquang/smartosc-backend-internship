package quanly;

public class MayNung extends HeThong {
    private static final int MAX = 2000;
    public MayNung(String ten,int nhiet,int NhietMoilan1,int SoLan1){
        super(ten,nhiet,NhietMoilan1,SoLan1);
    }
    @Override
    public void CanhBao(String noidung) {
        System.out.println("May nung: "+ Ten + noidung);
    }
    @Override
    public void Kiemtra() {
        for (int i = 0; i < SoLan; i++) {
            SoLanDo++;
            Nhiet += NhietMoiLan;
            if (Nhiet < 0) {
                CanhBao(" nhiet do am la khong hop le");
                return;
            }
            if (Nhiet > MAX) {
                CanhBao(" lam nung qua muc o lan do thu "+SoLanDo);
            }
            else {
            CanhBao(" hoat dong binh thuong o lan do thu "+SoLanDo);
        }
    }
    }
}
