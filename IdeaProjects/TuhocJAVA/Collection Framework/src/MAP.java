import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Map;

public class MAP {

    public static void main(String[] args) {

        // =====================================================
        // 1. HASHMAP
        // =====================================================

        /*
         * Cấu trúc:
         * - Hash Table
         *
         * Ưu điểm:
         * + Put/Get/Remove rất nhanh O(1)
         * + Hiệu năng tốt nhất trong các Map
         * + Cho phép 1 key null
         *
         * Nhược điểm:
         * - Không đảm bảo thứ tự
         *
         * Khi dùng:
         * + Map mặc định trong hầu hết dự án Java
         */

        Map<Integer, String> hashMap =
                new HashMap<>();

        hashMap.put(1, "Java");
        hashMap.put(2, "Spring");
        hashMap.put(3, "MySQL");

        System.out.println("===== HashMap =====");
        System.out.println(hashMap);

        System.out.println("Get key=2: "
                + hashMap.get(2));

        hashMap.remove(1);

        System.out.println(hashMap);


        // =====================================================
        // 2. LINKEDHASHMAP
        // =====================================================

        /*
         * Cấu trúc:
         * - Hash Table + Linked List
         *
         * Ưu điểm:
         * + Giữ nguyên thứ tự thêm vào
         * + Hiệu năng gần HashMap
         *
         * Nhược điểm:
         * - Tốn bộ nhớ hơn HashMap
         *
         * Khi dùng:
         * + Cần giữ insertion order
         */

        Map<Integer, String> linkedHashMap =
                new LinkedHashMap<>();

        linkedHashMap.put(3, "MySQL");
        linkedHashMap.put(1, "Java");
        linkedHashMap.put(2, "Spring");

        System.out.println("\n===== LinkedHashMap =====");
        System.out.println(linkedHashMap);


        // =====================================================
        // 3. TREEMAP
        // =====================================================

        /*
         * Cấu trúc:
         * - Red Black Tree
         *
         * Ưu điểm:
         * + Tự động sắp xếp theo key
         * + Hỗ trợ firstKey(), lastKey()
         *
         * Nhược điểm:
         * - Put/Get/Remove O(log n)
         * - Chậm hơn HashMap
         *
         * Khi dùng:
         * + Cần dữ liệu luôn được sắp xếp
         */

        TreeMap<Integer, String> treeMap =
                new TreeMap<>();

        treeMap.put(3, "MySQL");
        treeMap.put(1, "Java");
        treeMap.put(2, "Spring");

        System.out.println("\n===== TreeMap =====");
        System.out.println(treeMap);

        System.out.println(
                "First Key: "
                        + treeMap.firstKey());

        System.out.println(
                "Last Key: "
                        + treeMap.lastKey());


        // =====================================================
        // 4. HASHTABLE
        // =====================================================

        /*
         * Cấu trúc:
         * - Hash Table
         *
         * Ưu điểm:
         * + Thread-safe (synchronized)
         *
         * Nhược điểm:
         * - Chậm hơn HashMap
         * - Không cho phép null key
         * - Legacy class
         *
         * Khi dùng:
         * + Hệ thống cũ
         */

        Hashtable<Integer, String> hashtable =
                new Hashtable<>();

        hashtable.put(1, "Java");
        hashtable.put(2, "Spring");

        System.out.println("\n===== Hashtable =====");
        System.out.println(hashtable);


        // =====================================================
        // DUYỆT MAP
        // =====================================================

        System.out.println("\n===== Duyệt HashMap =====");

        for (Map.Entry<Integer, String> entry
                : hashMap.entrySet()) {

            System.out.println(
                    "Key = " + entry.getKey()
                            + ", Value = "
                            + entry.getValue());
        }
    }
}