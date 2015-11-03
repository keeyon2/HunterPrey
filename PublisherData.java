import java.util.List;

public class PublisherData {
    private List<Integer> hunterCoordinates;
    private List<Integer> preyCoordinates;
    private List<WallData> walls;
    private List<ErrorData> errors;

    public PublisherData() {
    }

    public PublisherData(List<Integer> hunterCoords, List<Integer> preyCoords, 
            List<WallData> walls, List<ErrorData> errors) {
        this.hunterCoordinates = hunterCoords;
        this.preyCoordinates = preyCoords;
        this.walls = walls;
        this.errors = errors;
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
