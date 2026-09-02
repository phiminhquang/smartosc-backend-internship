package search;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Scanner;

public class Search {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // JSON string
        String json = "[\n" +
                "  {\n" +
                "    \"fullName\": \"Nguyen Van An\",\n" +
                "    \"age\": 20,\n" +
                "    \"gender\": \"Male\",\n" +
                "    \"hometown\": \"Ha Noi\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"fullName\": \"Tran Thi Huyen\",\n" +
                "    \"age\": 35,\n" +
                "    \"gender\": \"Female\",\n" +
                "    \"hometown\": \"Da Nang\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"fullName\": \"Le Van Chien\",\n" +
                "    \"age\": 17,\n" +
                "    \"gender\": \"Male\",\n" +
                "    \"hometown\": \"Hai Phong\"\n" +
                "  }\n" +
                "]";

        Gson gson = new Gson();

        try {

            // Tell Gson to convert JSON into List<Profile>
            Type profileListType = new TypeToken<List<Profile>>() {
            }.getType();

            // Convert JSON string into Java objects
            List<Profile> profileList = gson.fromJson(json, profileListType);

            while (true) {

                System.out.print("Enter name (0 to exit): ");
                String keyword = scanner.nextLine();

                if (keyword.equals("0")) {
                    System.out.println("Program ended.");
                    break;
                }

                boolean found = false;

                System.out.printf("%-25s%-10s%-15s%-15s%-25s%n",
                        "FULL NAME",
                        "AGE",
                        "GENDER",
                        "HOMETOWN",
                        "STATUS");

                for (Profile profile : profileList) {

                    if (profile.getFullName().toLowerCase().contains(keyword.toLowerCase())) {

                        found = true;

                        System.out.printf("%-25s%-10d%-15s%-15s%-25s%n",
                                profile.getFullName(),
                                profile.getAge(),
                                profile.getGender(),
                                profile.getHometown(),
                                profile.getWorkingAgeStatus());
                    }
                }

                if (!found) {
                    System.out.println("No matching result found.");
                }
            }

        } catch (JsonSyntaxException e) {
            System.out.println("Invalid JSON format.");
        }

        scanner.close();
    }
}