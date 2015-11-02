import java.awt.Point;

public class Prey {
    public Point currentDirection; 
    public Point location;

    public Prey() throws Exception {
        this.location = new Point(230, 200);
        this.currentDirection = new Point(0, 0);
    }

    public void invertXDirection() {
        this.currentDirection.x *= -1;
    }

    public void invertYDirection() {
        this.currentDirection.y *= -1;
    }

    public void invertBothDirection() {
        this.invertXDirection();
        this.invertYDirection();
    }

    // Make sure before I call this, I have updated my direction
    public void moveForward() {
        location = getNextLocation();
    }   

    public Point getNextLocation() {
        int x = location.x + currentDirection.x;
        int y = location.y + currentDirection.y;
        return new Point(x, y);
    }
}
