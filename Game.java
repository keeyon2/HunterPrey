import java.util.ArrayList;
import java.util.Iterator;
import java.awt.Point;
import java.net.Socket;
import java.io.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public abstract class Game {
 
    // Grid values
    // Wall:  0 - (M - 1)
    // Empty: -1
    // Hunter: -2
    // Prey: -3
    public Integer[][] grid;
    public ArrayList<Wall> walls;
    public int N;
    public int M;
    public int time;
    public int timeSinceLastMove;
    public Hunter hunter;
    public Prey prey;

    public Socket publisherSocket;
    public Socket playerSocket;
    
    public PrintWriter publisherOut = null;
    public PrintWriter playerOut = null;

    public BufferedReader publisherIn = null;
    public BufferedReader playerIn = null;

    JSONParser parser = new JSONParser();

    public final int BOARD_SIZE = 301;
    public boolean gameOver = false;

    public Game(int N, int M) throws Exception {
        this.N = N;
        this.M = M;
        this.time = 0;
        this.timeSinceLastMove = N; // Make sure we set to 0 when we make move
        grid = new Integer[301][301];
        walls = new ArrayList<Wall>();

        prey = new Prey();
        hunter = new Hunter();

        for (int x = 0; x < BOARD_SIZE; x++) {
           for (int y = 0; y < BOARD_SIZE; y++) {
              grid[x][y] = -1; 
           }
        }

        grid[0][0] = -2;
        grid[230][200] = -3;
    }

    public void startGame () throws Exception {
        JSONObject decisionJSONObject;
        while(!gameOver) {
            System.out.println("Start of new turn");
            decisionJSONObject = MakeDecision();
            System.out.println("Made Decision");
            sendDecision(decisionJSONObject);
            System.out.println("Sent Decision");
            readPublisher(); 
            System.out.println("We have updated our game");
        }
    }

    public void sendDecision(JSONObject decision) {
        playerOut.println(decision.toJSONString());
    }

    public void updateGame(Point hunterPoint, Point preyPoint, ArrayList<Wall> walls, 
            int serverTime, boolean gameOver) {

        // Update Time
        int timeDifference = Math.abs(serverTime - this.time);
        this.time = serverTime;
        this.timeSinceLastMove -= timeDifference;

        this.gameOver = gameOver;
        
        //Update Grid
        resetGrid();
        grid[hunterPoint.x][hunterPoint.y] = -2;
        grid[preyPoint.x][preyPoint.y] = -3;

        this.walls = walls;
        for (Wall wall : walls) {
            updateGridWithWall(wall, true);
        }
    }

    public void resetGrid() {
        for (int x = 0; x < BOARD_SIZE; x++) {
           for (int y = 0; y < BOARD_SIZE; y++) {
              grid[x][y] = -1; 
           }
        }
    }

    public boolean connectToSockets(int port) throws Exception {
        boolean publisherConnectionWorked = false;
        boolean playerConnectionWorked = false;
        publisherConnectionWorked = connectToPublisherSocket();
        playerConnectionWorked = connectToPlayerSocket(port);
        return (publisherConnectionWorked && playerConnectionWorked); 

    }

    public boolean connectToPublisherSocket() throws Exception{
        try {
            publisherSocket = new Socket("localhost", 1990);
        }
        catch (Exception e) {
            System.out.println("Error connecting to Publisher Socket: " + e);
            return false;
        }

        try {
            publisherOut = new PrintWriter(publisherSocket.getOutputStream(), true);
            publisherIn = new BufferedReader(new InputStreamReader(publisherSocket.getInputStream()));
        } catch (IOException eIO) {
            System.out.println("Exception creating new Input/Output Streams for publisher: " + eIO);
            return false;
        }
        return true;
    } 

    public boolean connectToPlayerSocket(int port) throws Exception{
        try {
            playerSocket = new Socket("localhost", port);
        }
        catch (Exception e) {
            System.out.println("Error connecting to Player Socket: " + e);
            return false;
        }

        try {
            playerOut = new PrintWriter(playerSocket.getOutputStream(), true);
            playerIn = new BufferedReader(new InputStreamReader(playerSocket.getInputStream()));
        } catch (IOException eIO) {
            System.out.println("Exception creating new Input/Output Streams for Player: " + eIO);
            return false;
        }
        return true;
    }

    public boolean CheckIfCanMakeMove() {
        return timeSinceLastMove >= N;
    }

    // Call at start of turn
    /*
    public void updateTime() {
        time += 1;
        timeSinceLastMove += 1;
    }
    */

    public boolean canBuildWall() {
        return walls.size() < M;
    }

    public boolean canDeleteWall() {
        return !walls.isEmpty();
    }

    public void addWall(Wall currentWall) {
        if (!canBuildWall()) {
            System.out.println("Error, we have too many walls");
        } 
        else {
            walls.add(currentWall);
            updateGridWithWall(currentWall, true);
        }
    }

    /*
     * Dont ever need to call, we update walls when we need
    public void deleteWall(Wall currentWall) {
        if (!canDeleteWall()) {
            System.out.println("Error, we dont have any walls to delete");
        } 
        else {
            walls.remove(currentWall);
            updateGridWithWall(currentWall, false);
        }
    }
    */

    public void updateGridWithWall(Wall wall, boolean add) {
        int startX = wall.start.x;
        int startY = wall.start.y;
        int directionX = wall.direction.x;
        int directionY = wall.direction.y;
        int length = wall.length;
        int id = wall.id;
        Point wallDirection = wall.direction;
        
        for (int i = 0; i < length; i++) {
            int gridXLocation = startX + i * directionX;
            int gridYLocation = startY + i * directionY;

            // Error Checking
            if (gridXLocation > 300 || gridXLocation < 0) {
                System.out.println("Error: Trying to create a wall id: " + id + " at X point: " 
                        + gridXLocation); 
                continue;
            }

            if (gridYLocation > 300 || gridYLocation < 0) {
                System.out.println("Error: Trying to create a wall id: " + id + " at Y point: " + 
                        gridYLocation); 
                continue;
            }

            else if (grid[gridXLocation][gridYLocation] != -1 && add) {
                System.out.println("Error: Trying to create a wall id: " + id + " at X point: " + 
                        gridXLocation + " and Y point: " + gridYLocation + ", but it is already: " + 
                        grid[gridXLocation][gridYLocation]);
                continue;
            }

            else if (grid[gridXLocation][gridYLocation] == -1 && !add) {
                System.out.println("Error: Trying to remove a wall id: " + id + " at X point: " 
                        + gridXLocation + " and Y point: " + gridYLocation + ", but it is empty.");
                continue;
            }

            if (add) {
                grid[gridXLocation][gridYLocation] = id;
            }

            else {
                grid[gridXLocation][gridYLocation] = -1;
            }
        }
    }

    public void printGrid() {
        for (int x = 0; x <= 300; x++) {
            for (int y = 0; y <= 300; y++) {
                System.out.print(grid[x][y]);
            }
            System.out.println();
        }
    }

    public void readPublisher() throws Exception{
        String commandJsonString;
        while (true) {
            System.out.println("We are waiting for the server");
            System.out.println((commandJsonString = publisherIn.readLine()) == null);
            while ((commandJsonString = publisherIn.readLine()) != null) {
                System.out.println("We have something from the server");
                Object obj = parser.parse(commandJsonString);
                JSONObject jsonObject = (JSONObject) obj;

                String hunterPosition = (String) jsonObject.get("hunter");
                Point hunterPoint = stringToPoint(hunterPosition);
                String preyPosition = (String) jsonObject.get("prey");
                Point preyPoint = stringToPoint(preyPosition);

                int time = (Integer) jsonObject.get("time");
                boolean gameOver = (Boolean) jsonObject.get("gameover");

                JSONArray walls = (JSONArray) jsonObject.get("wall");
                Iterator<JSONObject> jsonWallIterator = walls.iterator();
                ArrayList<Wall> readInWalls = new ArrayList<Wall>();
                int wallID = 0;
                while (jsonWallIterator.hasNext()) {
                    JSONObject currentWallJsonObject = jsonWallIterator.next();
                    int currentWallLength = (Integer) currentWallJsonObject.get("length");

                    String currentWallPosition = (String) currentWallJsonObject.get("position");
                    Point wallStart = stringToPoint(currentWallPosition);

                    String currentWallDirection = (String) currentWallJsonObject.get("direction");
                    Point wallDirection = serverDirectionToPoint(currentWallDirection);

                    Wall tempWall = new Wall(wallDirection, wallStart, currentWallLength,
                            wallID);
                    readInWalls.add(tempWall);
                    wallID += 1;
                }
                updateGame(hunterPoint, preyPoint, readInWalls, time, gameOver);
                return;
            }
        }
    }

    // Returns String like [1, 2] into point(1, 2)
    public Point stringToPoint(String inputString) {
        String allNumbers = inputString.replaceAll("[^0-9]", "");
        String[] arrayString = inputString.split("[0-9]");
        int x = Integer.parseInt(arrayString[0]);
        int y = Integer.parseInt(arrayString[1]);
        return new Point(x, y);
    }

    public Point serverDirectionToPoint(String inputString) {
        Point shouldNeverReturnThisPoint = new Point(0, 0);
        switch(inputString) {
            case "E":
                return new Point(1, 0);
            case "W":
                return new Point(-1, 0);
            case "N":
                return new Point(0, 1);
            case "S":
                return new Point(0, -1);
            case "NE":
                return new Point(1, 1);
            case "NW":
                return new Point(1, -1);
            case "SE":
                return new Point(-1, 1);
            case "SW":
                return new Point(-1, 1);
            default:
                throw new IllegalArgumentException("Invalid Direction: " + inputString);
        }
    }

    public abstract JSONObject MakeDecision();
}
