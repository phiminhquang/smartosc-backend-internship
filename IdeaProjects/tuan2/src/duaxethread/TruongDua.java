package duaxethread;

public class TruongDua {
    public static void main(String[] args) {
        System.out.println("====== CUOC DUA BAT DAU ======");

        // Tao 3 nhiem vu
        XeDua xe1 = new XeDua("Ferrari");
        XeDua xe2 = new XeDua("Porsche");
        XeDua xe3 = new XeDua("McLaren");

        // Giao nhiem vu cho 3 luong
        Thread luong1 = new Thread(xe1);
        Thread luong2 = new Thread(xe2);
        Thread luong3 = new Thread(xe3);

        // Kich hoat cho 3 xe chay cung luc
        luong1.start();
        luong2.start();
        luong3.start();
    }
}
