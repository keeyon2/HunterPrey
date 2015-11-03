import java.util.List;

public class PlayerWData {
    String command;
    List<WallData> walls;

    public PlayerWData() {
    }

    public PlayerWData(String command, List<WallData> walls) {
        this.command = command;
        this.walls = walls;
    }

    public String getCommand() {
        return this.command;
    }

    public void setCommand(String command) {
        this.command = command;
    }   

    public List<WallData> getWalls() {
        return this.walls;
    }

    public void setWalls(List<WallData> walls) {
        this.walls = walls;
    }

}

