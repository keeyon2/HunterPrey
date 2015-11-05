import java.util.List;

public class PublisherData {
    private List<Integer> hunterCoordinates;
    private String hunterDirection;
    private List<Integer> preyCoordinates;
    private List<WallData> walls;
    private List<ErrorData> errors;
    private Integer time;
    private Boolean gameover;

    public PublisherData() {
    }

    public PublisherData(List<Integer> hunterCoords, String hunterDirection, 
            List<Integer> preyCoords, List<WallData> walls, 
            List<ErrorData> errors, Integer time, Boolean gameover) {

        this.hunterCoordinates = hunterCoords;
        this.hunterDirection = hunterDirection;
        this.preyCoordinates = preyCoords;
        this.walls = walls;
        this.errors = errors;
        this.time = time;
        this.gameover = gameover;
    }

    public Integer getTime() {
        return this.time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Boolean getGameover() {
        return this.gameover;
    }

    public void setGameover(Boolean gameover) {
        this.gameover = gameover;
    }

    public String getHunterDirection() {
        return this.hunterDirection;
    }

    public void setHunterDirection(String direction) {
        this.hunterDirection = direction;
    }

    public List<Integer> getHunterCoordinates() {
        return this.hunterCoordinates;
    }

    public void setHunterCoordinates(List<Integer> coordinates) {
        this.hunterCoordinates = coordinates;
    }

    public List<Integer> getPreyCoordinates() {
        return this.preyCoordinates;
    }

    public void setPreyCoordinates(List<Integer> coordinates) {
        this.preyCoordinates = coordinates;
    }

    public List<WallData> getWalls() {
        return this.walls;
    }

    public void setWalls(List<WallData> walls) {
        this.walls = walls;
    }

    public List<ErrorData> getErrors() {
        return this.errors;
    }

    public void setErros(List<ErrorData> errors) {
        this.errors = errors;
    }
}
