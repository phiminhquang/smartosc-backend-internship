package Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class in {
    private LocalDate birthday =  LocalDate.of(2005,3,25);
    public void show(){
        System.out.println(birthday.getDayOfMonth());
        System.out.println(birthday.getMonth());
        System.out.println(birthday.getYear());
        System.out.println(birthday.getDayOfWeek());
    }
    public void show1(){
        LocalDate a = LocalDate.of(2005,3,23);
        System.out.println(birthday.plusDays(10));
        System.out.println(birthday.plusMonths(5));
        System.out.println(birthday.isBefore(a));
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        System.out.println(birthday.format(format));
    }
}
