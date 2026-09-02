import java.util.Scanner;
public class hocnhapdulieu {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("nhap so lan do nhiet do");
        int n;
        do {
            n = sc.nextInt();
            if (n<=0) System.out.println("vui long nhap n>0");}
        while (n<=0);
        int[] mang = new int[n];
        double tong = 0;
        int min=0,max=0,lando1=0,cnt = 0;
        for (int i = 0; i < n; i++) {
            System.out.println("lan do thu "+(i+1));
            mang[i] = sc.nextInt();
            if(i==0) max=min=mang[i];
            tong += mang[i];
            if(min>mang[i]) {min = mang[i];}
            if(max<mang[i]) {max = mang[i];lando1=i+1;}
            if(mang[i]>=30) {cnt+=1;}
        }
        System.out.print("danh sach nhiet do da do duoc: ");
        for (int i = 0; i < n; i++) {
            System.out.println(mang[i]);
        }
        System.out.println("tong nhiet do: "+tong);
        System.out.println("trun binh nhiet do: "+(tong/n));
        System.out.println("nhiet do thap nhat, lon nhat va lan do co nhiet do lon nhat lan luot la : "+min+" va "+max+" o lan thu "+lando1);
        System.out.println("so lan do co nhiet do lon hon 30 la: "+cnt);
    }
}
