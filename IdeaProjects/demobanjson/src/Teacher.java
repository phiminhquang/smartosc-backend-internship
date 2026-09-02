

public class Teacher extends User {

    public Teacher() {
        super();
    }

    public Teacher(String id, String name) {
        super(id, name);
    }

    @Override
    public String getTitle() {
        return "Teacher";
    }
}