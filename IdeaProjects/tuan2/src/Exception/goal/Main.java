package Exception.goal;

public class Main {
    public static void main(String[] args) {
        // Dữ liệu đầu vào bị nhiễu
        String[] banThang = {"2", "1", "không", "3", "lỗi mạng", "1"};

        // Khai báo biến tổng ở ngoài vòng lặp để giữ được giá trị cộng dồn
        int tongBanThang = 0;

        System.out.println("--- Bắt đầu xử lý dữ liệu ---");

        // Duyệt qua từng phần tử trong mảng
        for (int i = 0; i < banThang.length; i++) {
            try {
                // Cố gắng ép kiểu chuỗi thành số nguyên
                // Nếu banThang[i] là "không" hoặc "lỗi mạng", dòng này sẽ ném ra lỗi ngay lập tức
                int soBan = Integer.parseInt(banThang[i]);

                // Nếu ép kiểu thành công (không có lỗi), thì mới chạy đến dòng cộng dồn này
                tongBanThang += soBan;

            } catch (NumberFormatException e) {
                // Bắt lỗi ép kiểu sai định dạng
                System.out.println("Dữ liệu lỗi ('" + banThang[i] + "'), bỏ qua.");
            }
        }

        // In ra kết quả cuối cùng nằm ngoài vòng lặp
        System.out.println("-----------------------------");
        System.out.println("Tổng số bàn thắng hợp lệ của đội là: " + tongBanThang);
    }
}
