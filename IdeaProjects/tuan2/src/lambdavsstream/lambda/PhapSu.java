package lambdavsstream.lambda;

public class PhapSu extends Tuong implements PhepThuat {

    public PhapSu(String ten, double hp, double satThuong) {
        super(ten, hp, satThuong);
    }

    @Override
    public void tanCong() {
        System.out.println(ten + " bắn cầu lửa gây " + satThuong + " sát thương!");
    }
}