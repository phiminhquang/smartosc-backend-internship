import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Path duongDan = Path.of("data", "courses.json");

        try {
            String noiDungJson = Files.readString(duongDan, StandardCharsets.UTF_8);

            Gson gson = new Gson();

            Course[] courses = gson.fromJson(noiDungJson, Course[].class);

            if (courses == null || courses.length == 0) {
                System.out.println("Khong co khoa hoc nao trong file JSON.");
                return;
            }

            for (Course course : courses) {
                course.showInfor();
            }

        } catch (IOException e) {
            System.out.println("Khong doc duoc file courses.json.");
            System.out.println(e.getMessage());

        } catch (JsonSyntaxException e) {
            System.out.println("File JSON sai cu phap.");
            System.out.println(e.getMessage());
        }
    }
}