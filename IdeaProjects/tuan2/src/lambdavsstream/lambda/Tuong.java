package lambdavsstream.lambda;

public abstract class Tuong {
    protected String ten;
    protected double hp;
    protected double satThuong;

    public Tuong(String ten, double hp, double satThuong) {
        this.ten = ten;
        this.hp = hp;
        this.satThuong = satThuong;
    }

    // Phương thức trừu tượng
    public abstract void tanCong();

    // Getter
    public String getTen() { return ten; }
    public double getHp() { return hp; }
    public double getSatThuong() { return satThuong; }
}
