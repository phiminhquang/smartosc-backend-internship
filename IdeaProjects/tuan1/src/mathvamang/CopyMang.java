package mathvamang;

import java.util.Arrays;

public class CopyMang {
    public static void main(String[] args) {

        int[] mangNguon = {10, 20, 30, 40, 50};
        int[] mangDich = new int[6];

        System.arraycopy(mangNguon, 1, mangDich, 2, 3);

        System.out.println(Arrays.toString(mangNguon));
        System.out.println(Arrays.toString(mangDich));
    }
}