package Exception.ex.ex1;

public class BankAccount {
    private int balance;
    public BankAccount(int balance){
        this.balance = balance;
    }
    public void withdraw(int amount) throws InvalidAmountException,InsufficientFundsException{
        if (amount>balance){
            throw new InsufficientFundsException("So du cua quy khach khong du");
        }
        if (amount<=0){
            throw new InsufficientFundsException("Nhap so lon hon khong");
        }else {
            System.out.println("Rut tien thanh cong voi so du con lai: "+(balance-amount));
        }
    }
}
