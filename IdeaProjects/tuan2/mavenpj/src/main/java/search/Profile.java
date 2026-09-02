package search;

public class Profile {
    private String fullName;
    private int age;
    private String gender;
    private String hometown;

    public Profile(String fullName, int age, String gender, String hometown) {
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.hometown = hometown;
    }

    public String getWorkingAgeStatus() {
        if (age >= 18 && age <= 30) {
            return "Working age";
        }

        return "Not working age";
    }

    public String getFullName() {
        return fullName;
    }

    public int getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getHometown() {
        return hometown;
    }
}