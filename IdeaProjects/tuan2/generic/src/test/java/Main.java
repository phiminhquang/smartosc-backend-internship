import model.Book;
import model.Phone;
import repository.Warehouse;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- HỆ THỐNG QUẢN LÝ KHO HÀNG --- \n");

        // 1. Khởi tạo kho chuyên chứa ĐIỆN THOẠI
        Warehouse<Phone> phoneWarehouse = new Warehouse<>();
        phoneWarehouse.addProduct(new Phone("P01", "iPhone 15 Pro", 1200, "8GB"));
        phoneWarehouse.addProduct(new Phone("P02", "Samsung S24 Ultra", 1100, "12GB"));

        // Kiểm tra an toàn kiểu dữ liệu (Type Safety): 
        // Dòng dưới này nếu bỏ comment sẽ LỖI ngay vì kho phone không được nhét Book
        // phoneWarehouse.addProduct(new Book("B01", "Dế Mèn Phiêu Lưu Ký", 5, "Tô Hoài"));

        System.out.println(">> Tổng giá trị kho điện thoại: " + phoneWarehouse.calculateTotalValue() + " USD\n");


        // 2. Khởi tạo kho chuyên chứa SÁCH
        Warehouse<Book> bookWarehouse = new Warehouse<>();
        bookWarehouse.addProduct(new Book("B01", "Đắc Nhân Tâm", 10, "Dale Carnegie"));
        bookWarehouse.addProduct(new Book("B02", "Nhà Giả Kim", 8, "Paulo Coelho"));

        System.out.println(">> Tổng giá trị kho sách: " + bookWarehouse.calculateTotalValue() + " USD\n");


        // 3. 🌟 SỬ DỤNG WILDCARD HÀM TIỆN ÍCH
        System.out.println("--- IN CHI TIẾT CÁC KHO ---");
        printWarehouseDetails(phoneWarehouse);
        printWarehouseDetails(bookWarehouse);
    }

    // Hàm static sử dụng Wildcard <?> để in ra bất kỳ kho hàng nào
    public static void printWarehouseDetails(Warehouse<?> warehouse) {
        System.out.println("Danh sách sản phẩm trong kho:");
        for (Object item : warehouse.getStorage()) {
            System.out.println(" - " + item);
        }
        System.out.println("---------------------------------");
    }
}