import org.json.simple.JSONObject;
import java.util.ArrayList;
import java.awt.Point;

public class HunterGame extends Game {
    public int timeSinceLastBuild;

    public HunterGame(int N, int M, String playerDest) throws Exception {
        super(N, M, playerDest);
        this.timeSinceLastBuild = N;
        startGame();
    } 

    @Override
    public JSONObject MakeDecision() {
        System.out.println("Prey is behind: " + preyIsBehind());
        System.out.println("Hunter direction: " + this.hunter.currentDirection);
        timeSinceLastBuild += 1; 

        if (timeSinceLastBuild < N) {
            return jsonCreator.MovingWOBorDWalls();
        }

        if (preyIsBehind()) {
            return jsonCreator.MovingWOBorDWalls();
        }

        //check if in range
        if (inBuildWallRange()) {
            timeSinceLastBuild = 0;
            
            boolean buildV = rangeIsN(hunter.location.x, prey.location.x, N + 1);
            boolean buildH = rangeIsN(hunter.location.y, prey.location.y, N + 1);

            if (this.walls.size() < M) {
                if (buildV) {
                    return jsonCreator.BuildVerticleWall();
                }
                else {
                    return jsonCreator.BuildHorizontalWall();
                }
            }

            else {
                ArrayList<Integer> deleteMeIdArrayL = wallsToDelete();

                if (buildV) {
                    return jsonCreator.BuildAndDeleteWall("V", deleteMeIdArrayL); 
                }
                else {
                    return jsonCreator.BuildAndDeleteWall("H", deleteMeIdArrayL); 
                }
            }
        }

        else {
            return jsonCreator.MovingWOBorDWalls();
        }
    }

    public ArrayList<Integer> wallsToDelete() {
        Point preyLocation = this.prey.location;

        ArrayList<Integer> allWalls = new ArrayList<Integer>();
        ArrayList<Integer> keepWalls = new ArrayList<Integer>();
        ArrayList<Integer> deleteWalls = new ArrayList<Integer>();

        for (Wall wall : walls) {
            allWalls.add(wall.id);
        }

        keepWalls.add(wallIdInDirection(preyLocation, new Point(-1, 0)));
        keepWalls.add(wallIdInDirection(preyLocation, new Point(1, 0)));
        keepWalls.add(wallIdInDirection(preyLocation, new Point(0, 1)));
        keepWalls.add(wallIdInDirection(preyLocation, new Point(0, 1)));

        for (Integer id : allWalls) {
            if (!keepWalls.contains(id)) {
                deleteWalls.add(id);
            }
        }

        return deleteWalls;
    }

    public int wallIdInDirection(Point checkLocation, Point moveDirection) {

        //edge check
        if (moveDirection.x == 1) {
            if (checkLocation.x == 301) {
                return -1;
            } 
        }

        else if(moveDirection.x == -1) {
            if (checkLocation.x == -1) {
                return -1;
            }
        }

        else if (moveDirection.y == 1) {
            if (checkLocation.y == 301) {
                return -1;
            }
        }

        else if (moveDirection.y == -1) {
            if (checkLocation.y == -1) {
                return -1;
            }
        }

        //Check if foundWall
        if (grid[checkLocation.x][checkLocation.y] >= 0) {
            return grid[checkLocation.x][checkLocation.y];
        }

        else {
            Point newCheckLocation = pointsAdded(checkLocation, moveDirection); 
            return wallIdInDirection(newCheckLocation, moveDirection);
        }
    }

    public Point pointsAdded(Point point1, Point point2) {
        int newX = point1.x + point2.x;
        int newY = point1.y + point2.y;
        return new Point(newX, newY);
    }

    public boolean inBuildWallRange() {
        int rangeX = 0;
        int rangeY = 0;

        if (preyIsBehind()) {
            return false;
        }

        else {
            rangeX = Math.abs(hunter.location.x - prey.location.x);
            rangeY = Math.abs(hunter.location.y - prey.location.y);
        }

        boolean rangeIsN = (rangeX <= N + 1 || rangeY <= N + 1); 
        return rangeIsN;
    }

    public boolean rangeIsN(int hunterC, int preyC, int N) {
        return (Math.abs(hunter.location.x - prey.location.x)  <= N);
    }

    public boolean preyIsBehind() {
        Point preyLocation = this.prey.location;
        Point hunterLocation = this.hunter.location;
        Point hunterDirection = this.hunter.currentDirection;

        boolean xBehind = behindOnCoordinate(hunterLocation.x, preyLocation.x,
                hunterDirection.x);

        boolean yBehind = behindOnCoordinate(hunterLocation.y, preyLocation.y,
                hunterDirection.y);

        return (xBehind || yBehind);
    }

    public boolean behindOnCoordinate( int hunterC, int preyC, int hunterDirC) {
        int resultC = hunterC - preyC;

        if (resultC > 0) {
            if (hunterDirC > 0) {
                return true;
            }
        }

        else if (resultC < 0) {
            if (hunterDirC < 0) {
                return true;
            }
        }
        
        return false;
    }
}
