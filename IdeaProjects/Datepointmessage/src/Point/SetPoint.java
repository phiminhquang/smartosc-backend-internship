package Point;

import java.awt.*;

public class SetPoint {
    private Point a = new Point(2, 3);
    private Point b = new Point(3, 5);

    public void show() {
        System.out.println("Toa do ban dau: (" + a.x + ", " + a.y + ")");

        a.translate(5, -2);

        System.out.println("Toa do sau khi dich: (" + a.x + ", " + a.y + ")");

        System.out.println("Khoang cach: (" + a.distance(b) +  ")");

        System.out.println(a.equals(b));
    }
}