import java.util.ArrayList;
import java.awt.Point;
import java.net.Socket;
import java.io.*;
import org.json.simple.JSONObject;

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

    public Game(int N, int M) {
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

    public boolean connectToSockets(int port) throws Exception {
        connectToPublisherSocket();
        connectToPlayerSocket(port);
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
    }

    public boolean CheckIfCanMakeMove() {
        return timeSinceLastMove >= N;
    }

    // Call at start of turn
    public void updateTime() {
        time += 1;
        timeSinceLastMove += 1;
    }

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

    public void deleteWall(Wall currentWall) {
        if (!canDeleteWall()) {
            System.out.println("Error, we dont have any walls to delete");
        } 
        else {
            walls.remove(currentWall);
            updateGridWithWall(currentWall, false);
        }
    }

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

    public void readPublisher() {
        String commandJsonString;
        while (commandJsonString = publisherIn.readline() != null) {
            Object obj = parser.parse(commandJsonString);
            JSONObject jsonObject = (JSONObject) obj;
            String hunterPosition = (String) jsonObject.get("hunter");
            String preyPosition = (String) jsonObject.get("prey");
            int time = (Integer) jsonObject.get("time");
            boolean gameOver = (Boolean) jsonObject.get("gameover");

            JSONArray walls = (JSONArray) jsonObject.get("wall");
            Iterator<JSONObject> jsonWallIterator = walls.iterator();
            while (jsonWallIterator.hasNext()) {
                JSONObject currentWallJsonObject = jsonWallIterator.next();
                int currentWallLength = (Integer) currentWallJsonObject.get("length");
                String currentWallPosition = (String) currentWallJsonObject.get("position");
                String currentWallDirection = (String) currentWallJsonObject.get("direction");
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
}
