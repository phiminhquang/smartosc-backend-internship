public enum OrderStatus {

    PENDING(1, "Đang chờ"),
    PAID(2, "Đã thanh toán"),
    SHIPPING(3, "Đang giao"),
    COMPLETED(4, "Hoàn thành");

    private int code;
    private String description;

    OrderStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}