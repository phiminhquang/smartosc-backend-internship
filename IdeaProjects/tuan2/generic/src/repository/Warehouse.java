package repository;

import model.Product;
import java.util.ArrayList;
import java.util.List;

// T extends Product đảm bảo kho này chỉ chứa sản phẩm kế thừa từ Product
public class Warehouse<T extends Product> {
    private List<T> storage = new ArrayList<>();

    // Thêm sản phẩm vào kho
    public void addProduct(T product) {
        storage.add(product);
        System.out.println("📦 Đã nhập kho: " + product.getName());
    }

    // Lấy toàn bộ danh sách sản phẩm trong kho
    public List<T> getStorage() {
        return storage;
    }

    // 🌟 ĐÂY LÀ GENERIC METHOD: Tính tổng tiền của kho
    // Hàm này duyệt qua mọi sản phẩm và gọi được .getPrice() vì T đã extends Product
    public double calculateTotalValue() {
        double total = 0;
        for (T item : storage) {
            total += item.getPrice(); // Gọi được hàm của lớp cha Product
        }
        return total;
    }
}