public class Main {

    public static void main(String[] args) {

        OrderStatus status = OrderStatus.PAID;

        System.out.println(status);

        System.out.println(status.getCode());

        System.out.println(status.getDescription());

        System.out.println("---------------------");

        for (OrderStatus s : OrderStatus.values()) {

            System.out.println("Tên: " + s);

            System.out.println("Code: " + s.getCode());

            System.out.println("Mô tả: " + s.getDescription());

            System.out.println("---------------------");

        }

    }

}