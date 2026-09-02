package search;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

public class Search {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Path filePath = Path.of("Data.json");

        Gson gson = new Gson();

        try {
            String jsonContent = Files.readString(filePath);

            Type profileListType = new TypeToken<List<Profile>>() {
            }.getType();

            List<Profile> profileList = gson.fromJson(jsonContent, profileListType);

            while (true) {
                System.out.print("Enter name to search (enter 0 to exit): ");
                String keyword = scanner.nextLine();

                if (keyword.equals("0")) {
                    System.out.println("Program ended.");
                    break;
                }

                boolean notFound = true;

                System.out.format("%-25s%-10s%-15s%-15s%-30s%n",
                        "FULL NAME",
                        "AGE",
                        "GENDER",
                        "HOMETOWN",
                        "WORKING STATUS");

                for (Profile profile : profileList) {
                    if (profile.getFullName().toLowerCase().contains(keyword.toLowerCase())) {
                        notFound = false;

                        System.out.format("%-25s%-10d%-15s%-15s%-30s%n",
                                profile.getFullName(),
                                profile.getAge(),
                                profile.getGender(),
                                profile.getHometown(),
                                profile.getWorkingAgeStatus());
                    }
                }

                if (notFound) {
                    System.out.println("No matching result found.");
                }
            }

        } catch (IOException e) {
            System.out.println("Cannot find or read Data.json.");
        } catch (JsonSyntaxException e) {
            System.out.println("Data.json has invalid JSON format.");
        }

        scanner.close();
    }
}