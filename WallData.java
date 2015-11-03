import java.util.List;

public class WallData {
    private Integer length;
    private List<Integer> wallPosition;
    private String direction;

    public WallData() {
    }

    public WallData(Integer length, List<Integer> wallPos, String direction) {
        this.length = length;
        this.wallPosition = wallPos;
        this.direction = direction;
    }

    public Integer getLength() {
        return this.length;
    } 

    public void setLength(Integer len) {
        this.length = len;
    }

    public List<Integer> getWallPosition() {
        return this.wallPosition;
    }

    public void setWallPosition(List<Integer> wallPos) {
        this.wallPosition = wallPos;
    }

    public String getDirection() {
        return this.direction;
    }

    public void setDirection(String dir) {
        this.direction = dir;
    }
}
