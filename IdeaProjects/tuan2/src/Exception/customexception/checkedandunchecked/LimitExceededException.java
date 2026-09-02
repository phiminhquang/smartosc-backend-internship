package Exception.customexception.checkedandunchecked;

// LimitExceededException: Lỗi Vượt quá hạn mức
// extends Exception -> CHECKED (Java bắt ép 100% phải dùng throws và try-catch)
public class LimitExceededException extends Exception {

    public LimitExceededException(String message) {
        super(message);
    }
}
