package lenhthread.trandau;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        BangDienTu a = new BangDienTu();
        Thread a1 = new Thread(a);
        a1.setDaemon(true);
        a1.start();
        SoatVe congThuong = new SoatVe();
        SoatVe congVip = new SoatVe();
        Thread thuong = new Thread(congThuong,"thuong");
        Thread vip = new Thread(congVip,"VIP");
        thuong.setPriority(Thread.MIN_PRIORITY);
        vip.setPriority(Thread.MAX_PRIORITY);
        thuong.start();
        vip.start();
        Thread.sleep(2000);
        if(thuong.isAlive()){
            thuong.interrupt();
        }
        if (vip.isAlive()){
            vip.interrupt();
        }
        thuong.join();
        vip.join();
        System.out.println("TUÝT TUÝT! Trận đấu giữa FC Barcelona và Villarreal chính thức bắt đầu!");
    }
}
