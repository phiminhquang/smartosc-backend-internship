package lenhthread.trandau;

import java.util.Random;

public class SoatVe implements Runnable {
    @Override
    public void run() {
        String tenCong = Thread.currentThread().getName();
        System.out.println("Cong cua quy khach la: "+tenCong);
        System.out.println("5 quy khach tiep theo cua "+tenCong+" vui long di theo chi dan de vao san");
        for (int i = 0; i < 5; i++) {
            try {
                System.out.println("Xin moi quy khach thu "+(i+1)+" cua cong "+tenCong+" vao san");
                double thoiGianCho = Math.random()*1000  + 500;
                Thread.sleep((long) thoiGianCho);
            }
            catch (InterruptedException e){
                System.out.println("Cong "+tenCong+" bi dong khan cap! Cac khach hang con lai khong duong vao san.");
                break;
            }
        }
    }
}
