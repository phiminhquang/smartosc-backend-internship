package Exception.customexception;

public class ShoppingCart {
    // stockAvailable: Số lượng hàng còn tồn trong kho
    private int stockAvailable;

    // Constructor: Hàm khởi tạo bắt buộc truyền số lượng kho ban đầu
    // (initial = ban đầu, stock = hàng tồn kho)
    public ShoppingCart(int initialStock) {
        this.stockAvailable = initialStock;
    }

    // checkout: Hàm thanh toán
    // quantityToBuy: Số lượng khách muốn mua
    public void checkout(int quantityToBuy) {
        System.out.println("[SYSTEM] Processing your order...");

        // Chặn lỗi nếu khách nhập số âm hoặc số 0
        if (quantityToBuy <= 0) {
            // IllegalArgumentException: Lỗi tham số truyền vào không hợp lệ
            throw new IllegalArgumentException("Quantity must be greater than zero!");
        }

        // Chặn lỗi nếu khách mua vượt quá số lượng trong kho
        if (quantityToBuy > stockAvailable) {
            // Tự tay ném quả bom OutOfStockException do mình chế ra
            throw new OutOfStockException("We only have " + stockAvailable + " items left in stock!");
        }

        // Nếu mọi thứ hợp lệ, tiến hành trừ kho
        stockAvailable = stockAvailable - quantityToBuy;
        System.out.println("[SYSTEM] Checkout successful! Remaining stock: " + stockAvailable);
    }

    // Getter: Hàm phụ để lấy số lượng kho hiện tại ra màn hình Main
    public int getStockAvailable() {
        return this.stockAvailable;
    }
}
