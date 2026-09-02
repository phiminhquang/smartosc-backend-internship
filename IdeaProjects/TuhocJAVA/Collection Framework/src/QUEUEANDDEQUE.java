import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class QUEUEANDDEQUE {

    public static void main(String[] args) {

        // =====================================================
        // 1. LINKEDLIST QUEUE (FIFO)
        // =====================================================

        /*
         * Cấu trúc:
         * - Doubly Linked List
         *
         * Ưu điểm:
         * + Thêm/xóa đầu cuối O(1)
         * + Dễ triển khai Queue
         *
         * Nhược điểm:
         * - Tốn bộ nhớ hơn ArrayDeque
         * - Chậm hơn ArrayDeque trong đa số trường hợp
         *
         * Khi dùng:
         * + Queue đơn giản
         */

        Queue<String> linkedQueue = new LinkedList<>();

        linkedQueue.offer("Task 1");
        linkedQueue.offer("Task 2");
        linkedQueue.offer("Task 3");

        System.out.println("===== LinkedList Queue =====");
        System.out.println(linkedQueue);

        System.out.println("Peek: "
                + linkedQueue.peek());

        System.out.println("Poll: "
                + linkedQueue.poll());

        System.out.println(linkedQueue);


        // =====================================================
        // 2. ARRAYDEQUE
        // =====================================================

        /*
         * Cấu trúc:
         * - Circular Dynamic Array
         *
         * Ưu điểm:
         * + Add/remove đầu cuối O(1)
         * + Nhanh hơn LinkedList
         * + Thay thế Stack hiện đại
         *
         * Nhược điểm:
         * - Không truy cập index
         *
         * Khi dùng:
         * + Queue
         * + Stack
         * + Deque
         */

        Deque<String> arrayDeque =
                new ArrayDeque<>();

        arrayDeque.addFirst("Java");
        arrayDeque.addLast("Spring");
        arrayDeque.addLast("MySQL");

        System.out.println("\n===== ArrayDeque =====");
        System.out.println(arrayDeque);

        System.out.println(
                "Remove First: "
                        + arrayDeque.removeFirst());

        System.out.println(
                "Remove Last: "
                        + arrayDeque.removeLast());

        System.out.println(arrayDeque);


        // =====================================================
        // 3. PRIORITYQUEUE
        // =====================================================

        /*
         * Cấu trúc:
         * - Min Heap (mặc định)
         *
         * Ưu điểm:
         * + Luôn lấy được phần tử ưu tiên cao nhất
         * + Add/Poll O(log n)
         *
         * Nhược điểm:
         * - Không giữ thứ tự thêm vào
         * - Không phải FIFO
         *
         * Khi dùng:
         * + Scheduler
         * + Job Queue
         * + Dijkstra
         * + Task ưu tiên
         */

        Queue<Integer> priorityQueue =
                new PriorityQueue<>();

        priorityQueue.offer(30);
        priorityQueue.offer(10);
        priorityQueue.offer(20);
        priorityQueue.offer(5);

        System.out.println("\n===== PriorityQueue =====");
        System.out.println(priorityQueue);

        System.out.println(
                "Peek: "
                        + priorityQueue.peek());

        System.out.println(
                "Poll: "
                        + priorityQueue.poll());

        System.out.println(priorityQueue);


        // =====================================================
        // 4. DEQUE NHƯ STACK (LIFO)
        // =====================================================

        /*
         * Stack bằng ArrayDeque
         *
         * push()  -> addFirst()
         * pop()   -> removeFirst()
         * peek()  -> peekFirst()
         */

        Deque<Integer> stack =
                new ArrayDeque<>();

        stack.push(10);
        stack.push(20);
        stack.push(30);

        System.out.println("\n===== Stack bằng Deque =====");
        System.out.println(stack);

        System.out.println(
                "Pop: "
                        + stack.pop());

        System.out.println(stack);


        // =====================================================
        // 5. DEQUE HAI ĐẦU
        // =====================================================

        /*
         * Có thể thao tác ở cả đầu và cuối
         */

        Deque<Integer> deque =
                new ArrayDeque<>();

        deque.addFirst(10);
        deque.addLast(20);
        deque.addFirst(5);
        deque.addLast(30);

        System.out.println("\n===== Deque =====");
        System.out.println(deque);

        System.out.println(
                "First: "
                        + deque.peekFirst());

        System.out.println(
                "Last: "
                        + deque.peekLast());

        deque.removeFirst();
        deque.removeLast();

        System.out.println(deque);
    }
}