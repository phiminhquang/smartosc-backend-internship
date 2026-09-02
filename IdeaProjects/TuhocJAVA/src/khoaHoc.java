import java.util.ArrayList;
import java.util.List;

class user{
    private int id;
    private String name;
    private String email;
    private String tieude;
    public user(int id1, String name1,String email1,String tieude1){
            id = id1;
            name = name1;
            email =email1;
            tieude = tieude1;
    }
    public String getName1(){
        return name;
    }
    public int getId1(){
        return id;
    }
    public String getMail1(){
        return email;
    }
    public String getTieude(){
        return tieude;
    }
}
class Teacher extends user {
    public Teacher(int id, String name, String email) {
        super(id, name, email, "Teacher");
    }
}
class student extends user {
        public student(int id, String name, String email) {
            super(id, name, email, "Student");
        }
    }
class course{
    private String name;
    private Teacher teacher;
    private List<student> students =new ArrayList<>();
    public course(String name1 ,Teacher teacher1){
        name = name1;
        teacher = teacher1;
        students = new ArrayList<>();;
    }
    public void them(student newStudent){
        students.add(newStudent);
    }
    public void hienThi(){
        System.out.println("ten khoa hoc: "+name);
        System.out.println("ten giang vien: "+ teacher.getName1()+"\nID: "+teacher.getId1()+"\nemail: "+teacher.getMail1()+"\nVai trò: "+teacher.getTieude());
        System.out.println("danh sach hoc sinh:");
        for (student hs : students ){
            System.out.println(hs.getName1()+" "+hs.getId1()+" " +hs.getMail1());
        }
    }
}
public class khoaHoc {
    public static void main(String[] args) {
        Teacher A= new Teacher(1,"Quang","quangpm@smartosc.com");
        student b=new student(23021888,"BDQ","phiminhquang2@gmail.com");
        student c=new student(23021890,"PMQ","phiminhquang3@gmail.com");
        course khoaHoc = new course("Java",A);
        khoaHoc.them(b);
        khoaHoc.them(c);
        khoaHoc.hienThi();
    }
}
