import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Set;

public class SET {

    public static void main(String[] args) {

        // =========================
        // 1. HASHSET
        // =========================

        /*
         * Cấu trúc:
         * - Hash Table
         *
         * Ưu điểm:
         * + Thêm, xóa, tìm kiếm rất nhanh O(1)
         * + Hiệu năng tốt nhất trong các Set
         *
         * Nhược điểm:
         * - Không đảm bảo thứ tự phần tử
         * - Thứ tự có thể thay đổi sau mỗi lần chạy
         *
         * Khi dùng:
         * + Kiểm tra phần tử có tồn tại hay không
         * + Loại bỏ dữ liệu trùng lặp
         */

        Set<String> hashSet = new HashSet<>();

        hashSet.add("Java");
        hashSet.add("Spring");
        hashSet.add("MySQL");
        hashSet.add("Java"); // Không được thêm

        System.out.println("=== HashSet ===");
        System.out.println(hashSet);

        System.out.println("Co Java? " +
                hashSet.contains("Java"));

        hashSet.remove("Spring");

        System.out.println(hashSet);


        // =========================
        // 2. LINKEDHASHSET
        // =========================

        /*
         * Cấu trúc:
         * - Hash Table + Linked List
         *
         * Ưu điểm:
         * + Nhanh gần như HashSet
         * + Giữ nguyên thứ tự chèn (insertion order)
         *
         * Nhược điểm:
         * - Tốn bộ nhớ hơn HashSet
         * - Chậm hơn HashSet một chút
         *
         * Khi dùng:
         * + Muốn loại bỏ dữ liệu trùng
         * + Vẫn cần giữ thứ tự thêm vào
         */

        Set<String> linkedHashSet =
                new LinkedHashSet<>();

        linkedHashSet.add("Java");
        linkedHashSet.add("Spring");
        linkedHashSet.add("MySQL");

        System.out.println("\n=== LinkedHashSet ===");
        System.out.println(linkedHashSet);


        // =========================
        // 3. TREESET
        // =========================

        /*
         * Cấu trúc:
         * - Red-Black Tree
         *
         * Ưu điểm:
         * + Tự động sắp xếp dữ liệu
         * + Hỗ trợ tìm phần tử lớn nhất, nhỏ nhất
         *
         * Nhược điểm:
         * - Chậm hơn HashSet
         * - Add/Search/Delete: O(log n)
         *
         * Khi dùng:
         * + Cần dữ liệu luôn được sắp xếp
         * + Xử lý ranking, leaderboard,...
         */

        TreeSet<String> treeSet =
                new TreeSet<>();

        treeSet.add("Spring");
        treeSet.add("Java");
        treeSet.add("MySQL");

        System.out.println("\n=== TreeSet ===");
        System.out.println(treeSet);

        System.out.println("Phan tu dau tien: "
                + treeSet.first());

        System.out.println("Phan tu cuoi cung: "
                + treeSet.last());
    }
}