package GiaoHang;

import java.awt.Point;

public class KhoangCach {
    private Point a;
    private Point b;

    public KhoangCach(Point a, Point b) {
        this.a = a;
        this.b = b;
    }

    public void setA(Point a) {
        this.a = a;
    }

    public void setB(Point b) {
        this.b = b;
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }

    public double tinhKhoangCach() {
        return a.distance(b);
    }
}