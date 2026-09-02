class quet{
    public int thietlapdienap(int malinhkien){
        switch (malinhkien){
            case 1:
                return 7;
            case 2:
                return 3;
            case 3:
                return 12;
            default:
                return 0;
        }
    }
    public void kiemtra(int malinhkien,int tong){
            int dienap = thietlapdienap(malinhkien);
            for (int i = 0; i < tong; i++) {
                if (i == 3) continue;
                try {
                    int R = (i == 5) ? 0 : 10;
                    int dong = dienap / R;
                    System.out.println("linh kien so " + i +" hoat dong tot voi dong dien " + dong);
                }
                catch (ArithmeticException e) {
                    System.out.println("linh kien so "+i+" dang bi ngan mach");
                }
            }
        }
}

public class kiemtralohang {
    public static void main(String[] args) {
        quet silic = new quet();
        silic.kiemtra(1, 6);
    }
}
