import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import java.util.List;

public class LIST {

    public static void main(String[] args) {

        // =========================
        // 1. ARRAYLIST
        // =========================

        /*
         * Cấu trúc:
         * - Dynamic Array (Mảng động)
         *
         * Ưu điểm:
         * + Truy cập phần tử bằng index rất nhanh O(1)
         * + Duyệt dữ liệu nhanh
         * + Tốn ít bộ nhớ hơn LinkedList
         *
         * Nhược điểm:
         * - Thêm/xóa ở giữa danh sách chậm O(n)
         * - Phải dời (shift) các phần tử
         *
         * Khi dùng:
         * + Danh sách chủ yếu đọc dữ liệu
         * + Truy cập bằng index nhiều
         */

        List<String> arrayList = new ArrayList<>();

        arrayList.add("Java");
        arrayList.add("Spring");
        arrayList.add("MySQL");

        System.out.println("=== ArrayList ===");
        System.out.println(arrayList);

        System.out.println("Phan tu thu 2: " + arrayList.get(1));

        arrayList.remove("Spring");

        System.out.println("Sau khi xoa:");
        System.out.println(arrayList);


        // =========================
        // 2. LINKEDLIST
        // =========================

        /*
         * Cấu trúc:
         * - Doubly Linked List (Danh sách liên kết đôi)
         *
         * Ưu điểm:
         * + Thêm/xóa đầu hoặc cuối nhanh
         * + Thêm/xóa giữa danh sách tốt hơn ArrayList
         *
         * Nhược điểm:
         * - Truy cập theo index chậm O(n)
         * - Tốn nhiều bộ nhớ hơn
         *
         * Khi dùng:
         * + Thường xuyên thêm/xóa dữ liệu
         * + Queue, Stack, Deque
         */

        LinkedList<String> linkedList = new LinkedList<>();

        linkedList.add("Java");
        linkedList.add("Spring");
        linkedList.add("MySQL");

        System.out.println("\n=== LinkedList ===");
        System.out.println(linkedList);

        linkedList.addFirst("HTML");
        linkedList.addLast("Docker");

        System.out.println("Sau khi them dau/cuoi:");
        System.out.println(linkedList);

        linkedList.removeFirst();

        System.out.println("Sau khi xoa dau:");
        System.out.println(linkedList);


        // =========================
        // 3. VECTOR
        // =========================

        /*
         * Cấu trúc:
         * - Dynamic Array giống ArrayList
         *
         * Ưu điểm:
         * + Thread-safe (synchronized)
         * + Dùng được trong môi trường đa luồng
         *
         * Nhược điểm:
         * - Chậm hơn ArrayList do synchronized
         * - Hiện nay ít dùng
         *
         * Khi dùng:
         * + Hệ thống cũ (legacy system)
         * + Cần đồng bộ hóa sẵn
         */

        Vector<String> vector = new Vector<>();

        vector.add("Java");
        vector.add("Spring");
        vector.add("MySQL");

        System.out.println("\n=== Vector ===");
        System.out.println(vector);

        vector.remove("Spring");

        System.out.println("Sau khi xoa:");
        System.out.println(vector);
    }
}