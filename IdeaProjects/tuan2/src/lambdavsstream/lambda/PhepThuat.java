package lambdavsstream.lambda;

public interface PhepThuat {

    // Hàm default
    default void hoiMauTuDong(Tuong t) {
        System.out.println("✨ Đang buff máu cho " + t.getTen() + " (+50 HP)");
    }

    // Hàm static
    static boolean conSong(Tuong t) {
        return t.getHp() > 0;
    }
}
