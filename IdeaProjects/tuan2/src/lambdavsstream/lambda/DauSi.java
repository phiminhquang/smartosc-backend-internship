package lambdavsstream.lambda;

public class DauSi extends Tuong {

    public DauSi(String ten, double hp, double satThuong) {
        super(ten, hp, satThuong);
    }

    @Override
    public void tanCong() {
        System.out.println(ten + " chém cận chiến gây " + satThuong + " sát thương!");
    }
}
