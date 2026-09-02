package quanly;

public abstract class HeThong {
    protected String Ten;
    protected int Nhiet;
    protected static int SoLanDo;
    protected int NhietMoiLan;
    protected int SoLan;
    public HeThong(String ten,int nhiet,int NhietMoilan1,int SoLan1){
        Ten=ten;
        Nhiet=nhiet;
        NhietMoiLan=NhietMoilan1;
        SoLan = SoLan1;
    }
    public abstract void CanhBao(String noidung);
    public abstract void Kiemtra();
    }
