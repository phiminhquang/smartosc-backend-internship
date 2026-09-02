
package taikhoan1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
//7/6/2026 BUOI SANG HỌC ĐƯỢC GHI LẠI VÀO FILE TXT,VIET DUOC 1 PJ HOAN CHINH VE THIET LAP TAI KHOAN NGAN HANG BASIC
// VÀ ỨNG DỤNG ĐƯỢC MAP VÀO BAI TAP

public class Main {
    public static final String TEN_FILE = "data.txt";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        HashMap<String, TaiKhoan1> danhSachTaiKhoan = new HashMap<>();

        docDuLieuTuFile(danhSachTaiKhoan);

        TaiKhoan1 taiKhoanDangNhap = null;
        int luaChon = -1;

        do {
            if (taiKhoanDangNhap == null) {
                System.out.println("===== HE THONG TAI KHOAN =====");
                System.out.println("1. Tao tai khoan");
                System.out.println("2. Dang nhap");
                System.out.println("0. Thoat");
            } else {
                System.out.println("3. Dang xuat");
                System.out.println("4. Nap tien");
                System.out.println("5. Rut tien");
                System.out.println("6. Xem so du");
                System.out.println("7. Xem so lan rut tien");
                System.out.println("8. Xoa tai khoan");
                System.out.println("0. Thoat");
            }

            try {
                luaChon = sc.nextInt();
                sc.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Lua chon phai la so.");
                sc.nextLine();
                luaChon = -1;
            }

            switch (luaChon) {
                case 1: {
                    if (taiKhoanDangNhap != null) {
                        System.out.println("Ban dang dang nhap. Hay dang xuat truoc.");
                        break;
                    }

                    boolean taoThanhCong = false;

                    while (!taoThanhCong) {
                        System.out.print("Nhap ten dang nhap: ");
                        String tenDangNhap = sc.nextLine();

                        if (tenDangNhap.isEmpty()) {
                            System.out.println("Ten dang nhap khong duoc de trong.");
                            continue;
                        }

                        if (tenDangNhap.contains("|")) {
                            System.out.println("Ten dang nhap khong duoc chua dau |");
                            continue;
                        }

                        if (danhSachTaiKhoan.containsKey(tenDangNhap)) {
                            System.out.println("Ten dang nhap da ton tai. Vui long nhap lai.");
                            continue;
                        }

                        String matKhau;

                        while (true) {
                            System.out.print("Nhap mat khau: ");
                            matKhau = sc.nextLine();

                            if (matKhau.isEmpty()) {
                                System.out.println("Mat khau khong duoc de trong.");
                                continue;
                            }

                            if (matKhau.contains("|")) {
                                System.out.println("Mat khau khong duoc chua dau |");
                                continue;
                            }

                            break;
                        }

                        TaiKhoan1 taiKhoanMoi = new TaiKhoan1(tenDangNhap, matKhau, 0, 0);

                        danhSachTaiKhoan.put(tenDangNhap, taiKhoanMoi);
                        luuDuLieuVaoFile(danhSachTaiKhoan);

                        System.out.println("Tao tai khoan thanh cong.");

                        taoThanhCong = true;
                    }

                    break;
                }

                case 2: {
                    if (taiKhoanDangNhap != null) {
                        System.out.println("Ban da dang nhap roi. Hay dang xuat truoc.");
                        break;
                    }

                    boolean dangNhapThanhCong = false;

                    while (!dangNhapThanhCong) {
                        System.out.print("Nhap ten dang nhap: ");
                        String tenNhap = sc.nextLine();

                        System.out.print("Nhap mat khau: ");
                        String matKhauNhap = sc.nextLine();

                        TaiKhoan1 taiKhoanCanKiemTra = danhSachTaiKhoan.get(tenNhap);

                        if (taiKhoanCanKiemTra == null) {
                            System.out.println("Khong tim thay tai khoan. Vui long nhap lai.");
                        } else if (!taiKhoanCanKiemTra.kiemTraDangNhap(tenNhap, matKhauNhap)) {
                            System.out.println("Mat khau khong dung. Vui long nhap lai.");
                        } else {
                            taiKhoanDangNhap = taiKhoanCanKiemTra;

                            System.out.println("Dang nhap thanh cong.");
                            System.out.println("Xin chao " + taiKhoanDangNhap.getTenDangNhap());

                            dangNhapThanhCong = true;
                        }
                    }

                    break;
                }

                case 3: {
                    if (taiKhoanDangNhap == null) {
                        System.out.println("Hien tai chua co tai khoan nao dang nhap.");
                    } else {
                        System.out.println("Da dang xuat tai khoan " + taiKhoanDangNhap.getTenDangNhap());
                        taiKhoanDangNhap = null;
                    }

                    break;
                }

                case 4: {
                    if (taiKhoanDangNhap == null) {
                        System.out.println("Ban can dang nhap truoc.");
                        break;
                    }

                    System.out.print("Nhap so tien muon nap: ");

                    try {
                        double soTienNap = sc.nextDouble();
                        sc.nextLine();

                        taiKhoanDangNhap.napTien(soTienNap);

                        luuDuLieuVaoFile(danhSachTaiKhoan);

                        System.out.println("Nap tien thanh cong.");
                        System.out.println("So du hien tai: " + taiKhoanDangNhap.getSoDu() + " VND");
                    } catch (InputMismatchException e) {
                        System.out.println("So tien phai la so.");
                        sc.nextLine();
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }

                    break;
                }

                case 5: {
                    if (taiKhoanDangNhap == null) {
                        System.out.println("Ban can dang nhap truoc.");
                        break;
                    }

                    System.out.print("Nhap so tien muon rut: ");

                    try {
                        double soTienRut = sc.nextDouble();
                        sc.nextLine();

                        taiKhoanDangNhap.rutTien(soTienRut);

                        luuDuLieuVaoFile(danhSachTaiKhoan);

                        System.out.println("Rut tien thanh cong.");
                        System.out.println("So du con lai: " + taiKhoanDangNhap.getSoDu() + " VND");
                    } catch (InputMismatchException e) {
                        System.out.println("So tien phai la so.");
                        sc.nextLine();
                    } catch (IllegalArgumentException e) {
                        System.out.println(e.getMessage());
                    }

                    break;
                }

                case 6: {
                    if (taiKhoanDangNhap == null) {
                        System.out.println("Ban can dang nhap truoc.");
                        break;
                    }

                    System.out.println("So du hien tai: " + taiKhoanDangNhap.getSoDu() + " VND");
                    break;
                }

                case 7: {
                    if (taiKhoanDangNhap == null) {
                        System.out.println("Ban can dang nhap truoc.");
                        break;
                    }

                    System.out.println("So lan rut tien cua " + taiKhoanDangNhap.getTenDangNhap() + ": " + taiKhoanDangNhap.getSoLanGiaoDich());
                    break;
                }

                case 0:
                    luuDuLieuVaoFile(danhSachTaiKhoan);
                    System.out.println("Cam on ban da su dung dich vu.");
                    break;

                case 8: {
                    if (taiKhoanDangNhap == null) {
                        System.out.println("Ban can dang nhap truoc.");
                        break;
                    }

                    System.out.print("Nhap mat khau de xac nhan xoa tai khoan: ");
                    String matKhauXacNhan = sc.nextLine();

                    if (!taiKhoanDangNhap.kiemTraDangNhap(taiKhoanDangNhap.getTenDangNhap(), matKhauXacNhan)) {
                        System.out.println("Mat khau khong dung. Khong the xoa tai khoan.");
                        break;
                    }

                    System.out.print("Ban co chac muon xoa tai khoan? Nhap YES de xac nhan: ");
                    String xacNhan = sc.nextLine();

                    if (xacNhan.equals("YES")) {
                        String tenDangNhap = taiKhoanDangNhap.getTenDangNhap();

                        danhSachTaiKhoan.remove(tenDangNhap);

                        luuDuLieuVaoFile(danhSachTaiKhoan);

                        taiKhoanDangNhap = null;

                        System.out.println("Da xoa tai khoan thanh cong.");
                    } else {
                        System.out.println("Da huy xoa tai khoan.");
                    }

                    break;
                }

                default:
                    if (luaChon != -1) {
                        System.out.println("Lua chon khong hop le.");
                    }
                    break;
            }

        } while (luaChon != 0);

        sc.close();
    }

    public static void docDuLieuTuFile(HashMap<String, TaiKhoan1> danhSachTaiKhoan) {
        // Tạo đường dẫn tới file data.txt
        Path path = Path.of(TEN_FILE);

        try {
            // Nếu file chưa tồn tại thì chưa có dữ liệu để đọc
            // Thoát khỏi hàm luôn
            if (!Files.exists(path)) {
                return;
            }

            // Đọc toàn bộ các dòng trong file
            // Mỗi dòng trong file sẽ trở thành 1 phần tử String trong List
            List<String> danhSachDong = Files.readAllLines(path);

            // Duyệt lần lượt từng dòng trong file
            for (String dong : danhSachDong) {

                // Nếu dòng đang rỗng thì bỏ qua
                if (dong.trim().isEmpty()) {
                    continue;
                }

                // Tách dòng bằng dấu |
                // Ví dụ:
                // kien|123|500000.0|2
                // sẽ thành:
                // duLieu[0] = kien
                // duLieu[1] = 123
                // duLieu[2] = 500000.0
                // duLieu[3] = 2
                String[] duLieu = dong.split("\\|");

                // Một tài khoản phải có đúng 4 phần dữ liệu
                // Nếu file sai định dạng thì bỏ qua dòng đó
                if (duLieu.length != 4) {
                    continue;
                }

                // Lấy từng dữ liệu từ mảng duLieu
                String tenDangNhap = duLieu[0];
                String matKhau = duLieu[1];

                // Đổi String trong file thành double
                double soDu = Double.parseDouble(duLieu[2]);

                // Đổi String trong file thành int
                int soLanGiaoDich = Integer.parseInt(duLieu[3]);

                // Tạo object TaiKhoan1 từ dữ liệu vừa đọc được
                TaiKhoan1 taiKhoan = new TaiKhoan1(tenDangNhap, matKhau, soDu, soLanGiaoDich);

                // Đưa tài khoản vào HashMap
                // Key là tên đăng nhập
                // Value là object TaiKhoan1
                danhSachTaiKhoan.put(tenDangNhap, taiKhoan);
            }
        } catch (IOException e) {
            // Lỗi khi đọc file, ví dụ file đang bị khóa hoặc không đọc được
            System.out.println("Khong the doc file data.txt");
        } catch (NumberFormatException e) {
            // Lỗi khi dữ liệu số trong file không đúng
            // Ví dụ soDu lại ghi là abc
            System.out.println("Du lieu trong file data.txt bi loi.");
        }
    }

    public static void luuDuLieuVaoFile(HashMap<String, TaiKhoan1> danhSachTaiKhoan) {
        // Tạo đường dẫn tới file data.txt
        Path path = Path.of(TEN_FILE);

        // Tạo List để chứa các dòng chuẩn bị ghi xuống file
        List<String> danhSachDong = new ArrayList<>();

        // Duyệt qua tất cả object TaiKhoan1 trong HashMap
        for (TaiKhoan1 taiKhoan : danhSachTaiKhoan.values()) {

            // Ghép thông tin tài khoản thành một dòng để lưu file
            // Ví dụ:
            // kien|123|500000.0|2
            String dong = taiKhoan.getTenDangNhap() + "|"
                    + taiKhoan.getMatKhau() + "|"
                    + taiKhoan.getSoDu() + "|"
                    + taiKhoan.getSoLanGiaoDich();

            // Thêm dòng vừa tạo vào List
            danhSachDong.add(dong);
        }

        try {
            // Ghi toàn bộ List xuống file data.txt
            // Nếu file chưa có thì Java tự tạo file2
            // Nếu file đã có thì nội dung cũ sẽ bị ghi đè bằng dữ liệu mới
            Files.write(path, danhSachDong);
        } catch (IOException e) {
            // Lỗi khi ghi file
            System.out.println("Khong the luu file data.txt");
        }
    }
}