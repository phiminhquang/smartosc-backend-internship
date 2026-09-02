package lenhthread.trandau;

public class BangDienTu implements Runnable {
    @Override
    public void run() {

            while (true){
                try {
                Thread.sleep(1000);
                System.out.println("Khan gia vui long khan truong vao san.");
            }
        catch (InterruptedException e) {
            break;
        }
            }
    }
}
