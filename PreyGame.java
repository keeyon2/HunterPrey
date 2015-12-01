import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.awt.Point;
import java.lang.Override;

public class PreyGame extends Game {

    public PreyGame(int N, int M, String playerDest) throws Exception {
        super(N, M, playerDest);
        startGame();
    }

    @Override
    public JSONObject MakeDecision() {
        Point hunterDirection = hunter.currentDirection;
        Point hunterLocation = hunter.location;
        Point preyLocation = prey.location;
        Point direction;

        if ((Math.abs(hunterLocation.x - preyLocation.x) > 1 &&
             Math.abs(hunterLocation.y - preyLocation.y) > 1) ||
             preyLocation.distance(hunterLocation) <= 6) {

            if (hunterDirection.x * hunterDirection.y > 0) {
                if (preyLocation.y > hunterLocation.y + (preyLocation.x - hunterLocation.x)) {
                    direction = new Point(-1, 1);
                } else {
                    direction = new Point(1, -1);
                }
            } else {
                if (preyLocation.y > hunterLocation.y - (preyLocation.x - hunterLocation.x)) {
                    direction = new Point(1, 1);
                } else {
                    direction = new Point(-1, -1);
                }
            }
        } else {
            direction = new Point(-hunterDirection.x, -hunterDirection.y);
        }

        int newX = preyLocation.x + direction.x;
        int newY = preyLocation.y + direction.y;

        if (newX < 0 || newX > 300) {
            direction.x = 0;
        }
        if (newY < 0 || newY > 300) {
            direction.y = 0;
        }
        if (direction.x == 0 && direction.y == 0) {
            return jsonCreator.NotMoving();
        } else if (direction.x == 1 && direction.y == 0) {
            return jsonCreator.Moving("E");
        } else if (direction.x == 1 && direction.y == 1) {
            return jsonCreator.Moving("SE");
        } else if (direction.x == 1 && direction.y == -1) {
            return jsonCreator.Moving("NE");
        } else if (direction.x == -1 && direction.y == 0) {
            return jsonCreator.Moving("W");
        } else if (direction.x == -1 && direction.y == 1) {
            return jsonCreator.Moving("SW");
        } else if (direction.x == -1 && direction.y == -1) {
            return jsonCreator.Moving("NW");
        } else if (direction.x == 0 && direction.y == 1) {
            return jsonCreator.Moving("S");
        } else if (direction.x == 0 && direction.y == -1) {
            return jsonCreator.Moving("N");
        }
        return jsonCreator.NotMoving();
    }

}
