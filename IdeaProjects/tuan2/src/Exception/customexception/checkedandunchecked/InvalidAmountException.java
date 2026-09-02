package Exception.customexception.checkedandunchecked;

// InvalidAmountException: Lỗi Số tiền không hợp lệ
// extends RuntimeException -> UNCHECKED (Java sẽ không ép phải gỡ mìn)
public class InvalidAmountException extends RuntimeException {

    public InvalidAmountException(String message) {
        super(message);
    }
}
