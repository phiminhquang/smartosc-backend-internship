// File: TramDoBJT.java

class GiamSatTramDo {

    public String thietLapDongDinhThien(int maThietLap) {
        switch (maThietLap) {
            case 1:
                return "Thap";
            case 2:
                return "Trung Binh";
            case 3:
                return "Lon";
            default:
                return "Ngat dong cap";
        }
    }

    public void quetTramDo(int maThietLap, int tongSoChip) {
        String dongDinhThien = thietLapDongDinhThien(maThietLap);
        System.out.println("--- Thiet lap: Cap dong dinh thien muc " + dongDinhThien + " ---");

        for (int i = 1; i <= tongSoChip; i++) {

            if (i == 4) {
                System.out.println("Chip so 4 khong co linh kien, bo qua!");
                continue;
            }

            try {
                int dongDauVao = (i == 3) ? 0 : 5;
                int heSoKhuechDai = 500 / dongDauVao;

                System.out.println("Chip so " + i + " : Do thong so khuech dai on dinh.");
            } catch (ArithmeticException e) {
                System.out.println("Canh bao: Chip so 3 bi chap mach (Loi chia cho 0)!");
            }
        }

        System.out.println("--- Quat tram do hoan tat an toan ---");
    }
}

public class TramDoBJT {
    public static void main(String[] args) {
        GiamSatTramDo tramDo = new GiamSatTramDo();

        // Chay thu nghiem voi ma thiet lap 2 (Trung Binh) va quet 6 chip BJT
        tramDo.quetTramDo(2, 6);
    }
}