package Exception.datsan;

public class DatSan {
    private String name;
    private double hours;
    public DatSan(String name,double hours){
        this.name = name;
        this.hours = hours;
    }
    public String getName(){
        return name;
    }
    public double getHours(){
        return hours;
    }
    public void show(){
        System.out.println("Da dat san: "+name+"\nvoi so gio la: "+hours);
    }
}
