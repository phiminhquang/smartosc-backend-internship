import java.util.Arrays;

public class Mang {
    public static void main(String[] args) {

        // 1. Khai bao va gan san gia tri
        int[] numbers = {5, 2, 9, 1, 5, 6};

        System.out.println("Mang ban dau: " + Arrays.toString(numbers));

        // 2. length: so luong phan tu
        System.out.println("So phan tu: " + numbers.length);

        // 3. Lay phan tu theo vi tri
        System.out.println("Phan tu dau tien: " + numbers[0]);
        System.out.println("Phan tu cuoi: " + numbers[numbers.length - 1]);

        // 4. Doi gia tri mot phan tu
        numbers[1] = 20;
        System.out.println("Sau khi doi numbers[1] = 20: " + Arrays.toString(numbers));

        // 5. Duyet mang bang for
        System.out.println("\nDuyet bang for:");
        for (int i = 0; i < numbers.length; i++) {
            System.out.println("numbers[" + i + "] = " + numbers[i]);
        }

        // 6. Duyet mang bang for-each
        System.out.println("\nDuyet bang for-each:");
        for (int number : numbers) {
            System.out.println(number);
        }

        // 7. Tinh tong, tim min, max, dem so chan
        int tong = 0;
        int max = numbers[0];
        int min = numbers[0];
        int demChan = 0;

        for (int number : numbers) {
            tong += number;

            if (number > max) {
                max = number;
            }

            if (number < min) {
                min = number;
            }

            if (number % 2 == 0) {
                demChan++;
            }
        }

        System.out.println("\nTong = " + tong);
        System.out.println("Max = " + max);
        System.out.println("Min = " + min);
        System.out.println("So luong so chan = " + demChan);

        // 8. Arrays.fill()
        int[] fillDemo = new int[5];

        Arrays.fill(fillDemo, 7);
        System.out.println("\nFill toan bo = 7: " + Arrays.toString(fillDemo));

        Arrays.fill(fillDemo, 1, 4, 9);
        System.out.println("Fill tu 1 den truoc 4 = 9: " + Arrays.toString(fillDemo));

        // 9. Sao chep toan bo mang
        int[] copy1 = Arrays.copyOf(numbers, numbers.length);
        System.out.println("\ncopyOf: " + Arrays.toString(copy1));

        int[] copyDaiHon = Arrays.copyOf(numbers, 8);
        System.out.println("copyOf dai hon: " + Arrays.toString(copyDaiHon));

        // 10. Sao chep mot doan
        int[] copyDoan = Arrays.copyOfRange(numbers, 1, 4);
        System.out.println("copyOfRange tu 1 den truoc 4: " + Arrays.toString(copyDoan));

        // 11. clone()
        int[] cloneNumbers = numbers.clone();
        System.out.println("clone: " + Arrays.toString(cloneNumbers));

        // 12. System.arraycopy()
        int[] dich = new int[6];

        System.arraycopy(numbers, 1, dich, 0, 3);
        System.out.println("arraycopy: " + Arrays.toString(dich));

        // 13. So sanh noi dung hai mang
        System.out.println("\ncopy1 va numbers giong nhau: " + Arrays.equals(numbers, copy1));

        copy1[0] = 100;

        System.out.println("Sau khi doi copy1[0] = 100:");
        System.out.println("numbers: " + Arrays.toString(numbers));
        System.out.println("copy1: " + Arrays.toString(copy1));
        System.out.println("Hai mang con giong nhau: " + Arrays.equals(numbers, copy1));

        // 14. Sap xep tang dan
        int[] sapXep = numbers.clone();

        Arrays.sort(sapXep);
        System.out.println("\nMang sau khi sap xep: " + Arrays.toString(sapXep));

        // 15. binarySearch()
        int viTri = Arrays.binarySearch(sapXep, 5);
        System.out.println("Vi tri cua so 5 sau khi sap xep: " + viTri);

        int khongTimThay = Arrays.binarySearch(sapXep, 100);
        System.out.println("Tim so 100: " + khongTimThay);

        // 16. Dao nguoc mang
        int[] daoNguoc = numbers.clone();

        for (int i = 0; i < daoNguoc.length / 2; i++) {
            int tam = daoNguoc[i];
            daoNguoc[i] = daoNguoc[daoNguoc.length - 1 - i];
            daoNguoc[daoNguoc.length - 1 - i] = tam;
        }

        System.out.println("\nMang dao nguoc: " + Arrays.toString(daoNguoc));

        // 17. Stream
        int tongStream = Arrays.stream(numbers).sum();
        int maxStream = Arrays.stream(numbers).max().orElse(0);
        int minStream = Arrays.stream(numbers).min().orElse(0);
        double trungBinh = Arrays.stream(numbers).average().orElse(0);

        int[] soChan = Arrays.stream(numbers)
                .filter(x -> x % 2 == 0)
                .toArray();

        System.out.println("\nTong bang stream: " + tongStream);
        System.out.println("Max bang stream: " + maxStream);
        System.out.println("Min bang stream: " + minStream);
        System.out.println("Trung binh: " + trungBinh);
        System.out.println("Mang so chan: " + Arrays.toString(soChan));

        // 18. Mang 2 chieu
        int[][] matrix = {
                {1, 2, 3},
                {4, 5, 6}
        };

        System.out.println("\nMang 2 chieu: " + Arrays.deepToString(matrix));
        System.out.println("matrix[0][1] = " + matrix[0][1]);
        System.out.println("Duyet mang 2 chieu:");

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }

            System.out.println();
        }
    }
}