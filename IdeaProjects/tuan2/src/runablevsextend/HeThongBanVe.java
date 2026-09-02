package runablevsextend;

// 2. LOP NGHIEP VU: Vua ke thua lop cha, vua trien khai Runnable
// Neu ban dung "extends Thread", ban se KHONG THE "extends DichVuNen" duoc nua!
class HeThongBanVe extends DichVuNen implements Runnable {

    // Tai nguyen chia se chung: Kho ve chi co dung 5 ve
    private int soVeConLai = 5;

    @Override
    public void run() {
        String tenNhanVien = Thread.currentThread().getName();
        ghiLog("Nhan vien " + tenNhanVien + " da dang nhap he thong.");

        // Ban ve cho den khi het
        while (soVeConLai > 0) {
            // Su dung tu khoa synchronized de dong bo hoa
            // Khoa tai nguyen lai, bat cac luong phai xep hang, khong cho 2 nguoi cung ban 1 ve
            synchronized (this) {
                if (soVeConLai > 0) {
                    System.out.println("-> " + tenNhanVien + " dang xuat ve so: " + soVeConLai);
                    soVeConLai--; // Giam so ve di 1
                    System.out.println("\t(Kho con lai: " + soVeConLai + " ve)");
                } else {
                    System.out.println("-> " + tenNhanVien + " thong bao: VE DA BAN HET!");
                }
            }

            // Mo phong thoi gian in ve va thanh toan (tu 0.1s den 0.5s)
            try {
                Thread.sleep((long) (Math.random() * 500));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
