package search;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Search {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Path duongDan = Path.of("Data.txt");

        while (true) {
            System.out.print("Nhap ten ban muon tim kiem: ");
            boolean f = true;
            String Search;
            Search = sc.nextLine();
            try {
                List<String> danhSach = Files.readAllLines(duongDan);
                System.out.format("%-25s%-10s%-15s%-15s%-15s%n", "HO TEN", "TUOI", "GIOI TINH", "QUE QUAN","TINH TRANG");

                for (String HoSoTungNguoi : danhSach) {
                    String[] thongTin = HoSoTungNguoi.split(";");
                    String ten = thongTin[0];
                    int tuoi = Integer.parseInt(thongTin[1]);
                    String gioiTinh = thongTin[2];
                    String queQuan = thongTin[3];
                    HoSo a = new HoSo(ten, tuoi, gioiTinh, queQuan);
                    if (ten.contains(Search)) {
                        f = false;
                        System.out.format("%-25s%-10d%-15s%-15s%-15s%n", a.getTen(), a.getTuoi(), a.getGioiTinh(), a.getQueQuan(),a.doTuoiLaoDong());
                    }
                }
                if (f) {
                    System.out.println("Khong tim thay ket qua tra ve");
                    System.out.println("Vui long nhap lai ten ban muon tim kiem");
                }
            }
            catch(IOException e){
                    System.out.println("Duong dan khong ton tai");
                }

            }
        }
    }