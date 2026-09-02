import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            Path duongDan = Path.of("data", "x1.json");

            String noiDungJson = Files.readString(duongDan);

            System.out.println("Noi dung file JSON:");
            System.out.println(noiDungJson);
            System.out.println("Da doc duoc file JSON.");

        } catch (IOException e) {
            System.out.println("Khong doc duoc file JSON.");
            System.out.println(e.getMessage());
        }
    }
}