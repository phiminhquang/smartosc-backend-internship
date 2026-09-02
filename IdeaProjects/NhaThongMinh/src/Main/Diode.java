package Main;

public class Diode extends LinhKien {
    private static final double VTH = 0.7;
    public Diode(String tenlk){
        super(tenlk);
    }
    @Override
    public void phat(){
        System.out.println("Can cap dien ap lon hon "+VTH+" de diode "+Tenlk+ " hoat dong binh thuong");
    }
}
