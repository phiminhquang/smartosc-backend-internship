package Exception.customexception.checkedandunchecked;

public class BankAccount {
    // dailyLimit: Hạn mức chuyển tiền 1 ngày (50 triệu)
    private int dailyLimit = 50000000;

    // -------------------------------------------------------------
    // HÀM 1: Gài bom tàng hình (Unchecked)
    // KHÔNG CẦN chữ 'throws' ở trên tên hàm!
    // -------------------------------------------------------------
    public void validateAmount(int amountToTransfer) {
        if (amountToTransfer <= 0) {
            // Ném thẳng tay, trình biên dịch Java không hề phàn nàn.
            throw new InvalidAmountException("Transfer amount must be greater than zero!");
        }
        System.out.println("[SYSTEM] Amount is valid.");
    }

    // -------------------------------------------------------------
    // HÀM 2: Gài bom hạng nặng (Checked)
    // BẮT BUỘC phải dán nhãn 'throws LimitExceededException'
    // Nếu bạn xóa chữ throws đi, dòng chữ 'throw new...' bên trong sẽ bị gạch đỏ lòm!
    // -------------------------------------------------------------
    public void checkDailyLimit(int amountToTransfer) throws LimitExceededException {
        if (amountToTransfer > dailyLimit) {
            throw new LimitExceededException("You cannot transfer more than 50,000,000 VND per day!");
        }
        System.out.println("[SYSTEM] Within daily limit. Transfer successful!");
    }
}
