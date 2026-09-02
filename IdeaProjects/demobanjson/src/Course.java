import java.util.ArrayList;
import java.util.List;


public class Course {
    private String id;
    private String schoolName;
    private String name;
    private Teacher teacher;
    private List<Student> students;

    public Course() {
        students = new ArrayList<>();
    }

    public Course(String id, String name, Teacher teacher, String schoolName) {
        this.id = id;
        this.name = name;
        this.teacher = teacher;
        this.schoolName = schoolName;
        this.students = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public String getName() {
        return name;
    }

    public Teacher getTeacher() {
        return teacher;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTeacher(Teacher teacher) {
        this.teacher = teacher;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void showInfor() {
        System.out.println("Ten khoa hoc: " + name + " | ID: " + id + " | Truong: " + schoolName);

        if (teacher != null) {
            System.out.println("Giang vien: " + teacher.getName() + " | ID: " + teacher.getId());
        }

        for (Student student : students) {
            System.out.println("Sinh vien: " + student.getName() + " | ID: " + student.getId() + " | Que quan: " + student.getHomeTown() + " | Email: " + student.getEmail());
        }

        System.out.println("--------------------");
    }
}