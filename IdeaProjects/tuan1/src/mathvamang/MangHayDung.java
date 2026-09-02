package mathvamang;

import java.util.Arrays;

public class MangHayDung {
    public static void main(String[] args) {

        // 1. Khai báo mảng và gán sẵn giá trị
        int[] numbers = {5, 2, 9, 1, 5, 6};

        // Arrays.toString(): in mảng 1 chiều đẹp hơn
        System.out.println("Mang ban dau: " + Arrays.toString(numbers));

        // 2. length: số lượng phần tử trong mảng
        System.out.println("So phan tu: " + numbers.length);

        // 3. Lấy phần tử theo vị trí
        // Chỉ số mảng bắt đầu từ 0
        System.out.println("Phan tu dau tien: " + numbers[0]);
        System.out.println("Phan tu cuoi cung: " + numbers[numbers.length - 1]);

        // 4. Sửa giá trị một phần tử
        numbers[1] = 20;
        System.out.println("Sau khi sua numbers[1]: " + Arrays.toString(numbers));

        // 5. Duyệt mảng bằng for bình thường
        System.out.println("Duyet mang bang for:");
        for (int i = 0; i < numbers.length; i++) {
            System.out.println("numbers[" + i + "] = " + numbers[i]);
        }

        // 6. Duyệt mảng bằng for-each
        // Dùng khi chỉ cần lấy giá trị, không cần biết vị trí i
        System.out.println("Duyet mang bang for-each:");
        for (int number : numbers) {
            System.out.println(number);
        }

        // 7. Tính tổng các phần tử
        int tong = 0;

        for (int number : numbers) {
            tong += number;
        }

        System.out.println("Tong cac phan tu: " + tong);

        // 8. Tính trung bình
        double trungBinh = (double) tong / numbers.length;
        System.out.println("Trung binh: " + trungBinh);

        // 9. Tìm số lớn nhất và nhỏ nhất
        int max = numbers[0];
        int min = numbers[0];

        for (int number : numbers) {
            if (number > max) {
                max = number;
            }

            if (number < min) {
                min = number;
            }
        }

        System.out.println("So lon nhat: " + max);
        System.out.println("So nho nhat: " + min);

        // 10. Sắp xếp mảng tăng dần
        Arrays.sort(numbers);
        System.out.println("Mang sau khi sort tang dan: " + Arrays.toString(numbers));

        // 11. Tìm vị trí phần tử bằng Arrays.binarySearch()
        // Lưu ý: phải sort mảng trước khi dùng binarySearch()
        int viTri = Arrays.binarySearch(numbers, 5);
        System.out.println("Vi tri cua so 5: " + viTri);

        // Nếu không tìm thấy, kết quả trả về là số âm
        int viTriKhongCo = Arrays.binarySearch(numbers, 100);
        System.out.println("Tim so 100: " + viTriKhongCo);

        // 12. Sao chép toàn bộ mảng
        int[] banSao = Arrays.copyOf(numbers, numbers.length);
        System.out.println("Ban sao cua mang: " + Arrays.toString(banSao));

        // 13. Sao chép một đoạn mảng
        // copyOfRange(mang, viTriBatDau, viTriKetThuc)
        // viTriKetThuc không được lấy
        int[] mangCon = Arrays.copyOfRange(numbers, 1, 4);
        System.out.println("Mang con tu vi tri 1 den truoc 4: " + Arrays.toString(mangCon));

        // 14. So sánh 2 mảng
        System.out.println("numbers co bang banSao khong: " + Arrays.equals(numbers, banSao));

        // 15. Thay toàn bộ phần tử bằng một giá trị
        int[] mangMoi = new int[5];

        // Ban đầu: [0, 0, 0, 0, 0]
        System.out.println("Mang moi: " + Arrays.toString(mangMoi));

        Arrays.fill(mangMoi, 10);
        System.out.println("Sau khi fill 10: " + Arrays.toString(mangMoi));

        // 16. clone(): tạo một bản sao mảng
        int[] cloneMang = numbers.clone();
        System.out.println("Mang clone: " + Arrays.toString(cloneMang));

        // 17. Kiểm tra việc clone là mảng riêng
        cloneMang[0] = 999;

        System.out.println("numbers: " + Arrays.toString(numbers));
        System.out.println("cloneMang sau khi sua: " + Arrays.toString(cloneMang));

        // 18. Mảng 2 chiều: 2 hàng, 3 cột
        int[][] bangDiem = {
                {8, 7, 9},
                {6, 10, 8}
        };

        // deepToString(): in mảng nhiều chiều
        System.out.println("Mang 2 chieu: " + Arrays.deepToString(bangDiem));

        // 19. Lấy phần tử trong mảng 2 chiều
        System.out.println("Phan tu hang 0 cot 1: " + bangDiem[0][1]);
        System.out.println("Phan tu hang 1 cot 2: " + bangDiem[1][2]);

        // 20. Duyệt mảng 2 chiều
        System.out.println("Duyet mang 2 chieu:");

        for (int i = 0; i < bangDiem.length; i++) {
            for (int j = 0; j < bangDiem[i].length; j++) {
                System.out.println("bangDiem[" + i + "][" + j + "] = " + bangDiem[i][j]);
            }
        }

        // 21. Tính tổng toàn bộ phần tử mảng 2 chiều
        int tongBangDiem = 0;

        for (int i = 0; i < bangDiem.length; i++) {
            for (int j = 0; j < bangDiem[i].length; j++) {
                tongBangDiem += bangDiem[i][j];
            }
        }

        System.out.println("Tong mang 2 chieu: " + tongBangDiem);
    }
}