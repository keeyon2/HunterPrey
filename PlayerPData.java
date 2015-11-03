import java.util.List;

public class PlayerPData {
    String command;
    List<Integer> hunterCoords;
    List<Integer> preyCoords;

    public PlayerPData() {
    }

    public PlayerPData(String command, List<Integer> hunterCoords,
            List<Integer> preyCoords) {
        this.command = command;
        this.hunterCoords = hunterCoords;
        this.preyCoords = preyCoords;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }   

    public List<Integer> getHunterCoords() {
        return this.hunterCoords;
    }

    public void setHunterCoords(List<Integer> hunterCoords) {
        this.hunterCoords = hunterCoords;
    }

    public List<Integer> getPreyCoords() {
        return this.preyCoords;
    }

    public void setPreyCoords(List<Integer> preyCoords) {
        this.preyCoords = preyCoords;
    }
} 
