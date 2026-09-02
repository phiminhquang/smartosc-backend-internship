

public class Student extends User {

    public Student() {
        super();
    }

    public Student(String id, String name) {
        super(id, name);
    }

    @Override
    public String getTitle() {
        return "Student";
    }
}