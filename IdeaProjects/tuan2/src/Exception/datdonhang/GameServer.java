package Exception.datdonhang;

public class GameServer {
    public void createCharacter  (String characterName, int currentPlayers)throws ServerFullException{
        if (characterName.length()<3){
            throw new InvalidNameException("Ten phai lon hon hoac bang 3 ky tu");
        }
        if (currentPlayers>=100){
            throw new ServerFullException("Sever da bi qua tai");
        }
        else {
            System.out.println("Tao nhan vat "+characterName+" thanh cong");
        }
    }
}
