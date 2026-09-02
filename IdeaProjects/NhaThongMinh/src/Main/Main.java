package Main;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        LinhKien[] Tong = new LinhKien[2];
        Tong[0]= new Diode("Si");
        Tong[1]= new BJTNPN("HLP");
        Tong[0].phat();
        Tong[1].phat();
    }
}
