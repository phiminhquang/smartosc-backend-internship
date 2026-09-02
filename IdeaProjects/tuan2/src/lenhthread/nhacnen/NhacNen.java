package lenhthread.nhacnen;

class NhacNen implements Runnable {
    @Override
    public void run() {
        while (true) { // Vòng lặp vô hạn
            System.out.println("🎵 (Nhạc nền đang phát...)");
            try { Thread.sleep(400); } catch (InterruptedException e) {}
        }
    }
}