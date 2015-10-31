import java.awt.Point;
import java.util.Objects;

public class Wall {

    public Point direction;
    public Point start;
    public int length;
    public int id;

    public Wall(Point direction, Point start, int length, int id) {
        this.direction = direction;
        this.start = start;
        this.length = length;
        this.id = id;
    }

    @Override
    public boolean equals(Object entryObject) {
        if (entryObject == null) {
            return false;
        }

        if (!(entryObject instanceof Wall)) {
            return false;
        }
        
        Wall entryWall = (Wall) entryObject;

        if (this.direction == entryWall.direction &&
                this.start == entryWall.start &&
                this.length == entryWall.length &&
                this.id == id) {
            return true;
                }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction.hashCode(), start.hashCode(), length, id);
    }
}
