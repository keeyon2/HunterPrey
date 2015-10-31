import java.awt.Point;

public class Hunter {
    public Point currentDirection; 
    public Point location;

    public Hunter() {
        this.location = new Point(0, 0);
        this.currentDirection = new Point(1, 1);
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
