import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.awt.Point;
import java.lang.Object;
import java.net.Socket;
import java.io.*;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import java.net.URI;
 
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class Game implements GameWithPublisherSocket, GameWithPlayerSocket {
 
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

    JSONParser parser = new JSONParser();

    public final int BOARD_SIZE = 301;
    public boolean gameOver = false;

    WebSocketClient publClient = new WebSocketClient();
    WebSocketClient playerClient = new WebSocketClient();
    PublisherWebSocket publSocket = new PublisherWebSocket(this);
    PlayerWebSocket playerSocket = new PlayerWebSocket(this);

    public boolean connectedToPublisher = false;
    public boolean connectedToPlayer = false;
    public boolean okToMakeMove = true;

    public Game(int N, int M, String playerDest) throws Exception {
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

        setUpConnection(playerDest);
    }

    public void setUpConnection(String playerDest) {
        try {
            publClient.start();
            playerClient.start();
            URI publURI = new URI("ws://localhost:1990");
            URI playerURI = new URI(playerDest);
            ClientUpgradeRequest publRequest = new ClientUpgradeRequest();
            ClientUpgradeRequest playerRequest = new ClientUpgradeRequest();
            publClient.connect(publSocket, publURI, publRequest);
            playerClient.connect(playerSocket, playerURI, playerRequest);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void ReceivedMessageFromPlayerSocket(String message) {
        //Received message from Player
        parsePlayerMessage(message);
        //parsePlayerMessageGSON(message);
    }  

    public void checkGameOver() {
        if (gameOver) {
            System.out.println("Game is now over");

            try {
                System.out.println("Stopping Servers");
                publClient.stop();
                playerClient.stop();
                connectedToPublisher = false;
                connectedToPlayer= false;
            }

            catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            }

            catch (Exception e) {
                e.printStackTrace();
            }

            System.exit(0);
        }
    }

    public void ReceivedMessageFromPublisherSocket(String message) {
        parsePublisherMessage(message);
        checkGameOver();
        playerMakeMove();

        //parsePublisherMessageGSON(message);
    }
    

    public void ConnectionMadeWithPlayerSocket() {
        this.connectedToPlayer = true;
    }

    public void ConnectionMadeWithPublisherSocket() {
        this.connectedToPublisher = true;
    }

    public void waitForConnections() {
        boolean notConnected = true;
        while(notConnected) {
            if (this.connectedToPublisher && this.connectedToPlayer) {
                notConnected = false;
            }
            else {
                notConnected = true;
                try {
                    Thread.sleep(500);
                }
                catch (Exception e) {
                    e.printStackTrace();
                } 
            }
        }
    }

    public Boolean canMakeMove() {
        if (this instanceof HunterGame) {
            return this.okToMakeMove;
        }

        else if (this instanceof PreyGame) {
            boolean isEvenTime = (this.time % 2 == 0);
            if (this.time == 1) {
                if (this.hunter.location.x == 0 && this.hunter.location.y == 0) {
                    return this.okToMakeMove;
                }
            }
            return this.okToMakeMove && isEvenTime;
        }

        System.out.println("We do not know the type of this game, error");
        return false;
    }

    public void startGame() {
        waitForConnections();
        JSONObject decisionJSONObject;

        if (this instanceof HunterGame) {
            decisionJSONObject = MakeDecision();
            sendDecision(decisionJSONObject);
        }

        else {
            if (this.time == 0) {
                sendPToPlayerServer(); 

                try {
                    Thread.sleep(500);
                }
                catch (Exception e) {
                    System.out.println(e);
                }

                int hunterX = this.hunter.location.x;
                int hunterY = this.hunter.location.y;

                boolean hunterHasMoved = (hunterX != 0 || hunterY != 0);

                if (hunterHasMoved) {
                    decisionJSONObject = MakeDecision();
                    sendDecision(decisionJSONObject);
                }
            }
        }
    } 

    public void playerMakeMove() {
        JSONObject decisionJSONObject;

        if (this instanceof HunterGame) {
            decisionJSONObject = MakeDecision();
            sendDecision(decisionJSONObject);
        }

        else {
            if (this.time % 2 == 1) {
                decisionJSONObject = MakeDecision();
                sendDecision(decisionJSONObject);
            }
        }
    }

    public void sendPToPlayerServer() {
        JSONObject positions = jsonCreator.GetPositions();
        playerSocket.sendMessage(positions.toJSONString());
    }

    public void sendDecision(JSONObject decision) {
        playerSocket.sendMessage(decision.toJSONString());
    }

    public void updateGame(Point hunterPoint, Point preyPoint, Point hunterDir,
            ArrayList<Wall> walls, int serverTime, boolean gameOver) {

        // Update Time
        int timeDifference = Math.abs(serverTime - this.time);
        this.time = serverTime;
        this.timeSinceLastMove -= timeDifference;

        this.gameOver = gameOver;
        
        //Update Grid
        resetGrid();
        updatePositions(hunterPoint, hunterDir, preyPoint);

        updateWalls(walls);
        System.out.println("Game updated from Publisher");
    }

    public void resetGrid() {
        for (int x = 0; x < BOARD_SIZE; x++) {
           for (int y = 0; y < BOARD_SIZE; y++) {
              grid[x][y] = -1; 
           }
        }
    }


    public boolean CheckIfCanMakeMove() {
        return timeSinceLastMove >= N;
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

    public synchronized void parsePlayerMessage(String message) {
        System.out.println("Parsing Player Message: ");
        System.out.println(message);
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                Object obj = parser.parse(message);
                jsonObject = (JSONObject) obj;
            }

            catch (ParseException pe) {
                System.out.println("PLAYER PARSE ERROR");
                System.out.println("player message is");
                System.out.println(message);
                System.out.println(pe);
            }

            String commandValue = (String) jsonObject.get("command");

            if (commandValue.equals("W")) {
                System.out.println("Parsing Player W");
                JSONArray walls = (JSONArray) jsonObject.get("walls");
                ArrayList<Wall> readInWalls = parseJSONArrayWalls(walls);

                System.out.println("Finished Parsing W");
                updateWalls(readInWalls);
            }

            else if (commandValue.equals("P")) {
                System.out.println("Parsing Player P");
                JSONArray hunterCoordinates = (JSONArray) jsonObject.get("hunter");
                Point hunterPoint = parseJSONArrayCoordinates(hunterCoordinates);

                JSONArray preyCoordinates = (JSONArray) jsonObject.get("prey");
                Point preyPoint = parseJSONArrayCoordinates(preyCoordinates);

                System.out.println("Finished Parsing P");
                updatePositions(hunterPoint, preyPoint);
            }

            else {
                System.out.println("Received player Message with command not P or W");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateWalls(ArrayList<Wall> walls) {
        this.walls = walls;
        for (Wall wall : walls) {
            updateGridWithWall(wall, true);
        }
    }

    public void updatePositions(Point hunterPoint, Point hunterDir, Point preyPoint) {
        this.hunter.currentDirection = hunterDir;
        updatePositions(hunterPoint, preyPoint);
    }

    public void updatePositions(Point hunterPoint, Point preyPoint) {
        if (grid[hunter.location.x][hunter.location.y] == -2) {
            grid[hunter.location.x][hunter.location.y] = -1;
        }

        hunter.location = hunterPoint;
        grid[hunterPoint.x][hunterPoint.y] = -2;

        if (grid[prey.location.x][prey.location.y] == -3) {
            grid[prey.location.x][prey.location.y] = -1;
        }

        prey.location = preyPoint;
        grid[preyPoint.x][preyPoint.y] = -3;
    }

    public Point parseJSONArrayCoordinates(JSONArray coordinates) {
        long x = (Long) coordinates.get(0);
        long y = (Long) coordinates.get(1);

        int xi = Math.toIntExact(x);
        int yi = Math.toIntExact(y);
        return new Point(xi, yi);
    }

    public ArrayList<Wall> parseJSONArrayWalls(JSONArray walls) {
        ArrayList<Wall> returnWalls = new ArrayList<Wall>();
        for (int id = 0; id < walls.size(); id++) {
            JSONObject currentWallJsonObj = (JSONObject) walls.get(id);

            long currentWallLengthL = (Long) currentWallJsonObj.get("length");
            int currentWallLength = (int) currentWallLengthL;

            JSONArray currentWallCoordinates = (JSONArray) currentWallJsonObj.get(
                    "position");
            Point wallStart = parseJSONArrayCoordinates(currentWallCoordinates);

            String currentWallDirection = (String) currentWallJsonObj.get("direction");
            Point wallDirection = serverDirectionToPoint(currentWallDirection);

            Wall tempWall = new Wall(wallDirection, wallStart, currentWallLength,
                    id);
            returnWalls.add(tempWall);
        }

        return returnWalls;
    }

    public synchronized void parsePublisherMessage(String message) {
        System.out.println("Begginning to parse Publisher Message");
        message.replace("false", "\"false\"");
        message.replace("true", "\"true\"");
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                Object obj = parser.parse(message);
                jsonObject = (JSONObject) obj;
            }

            catch (ParseException pe) {
                System.out.println("PARSE ERROR");
                System.out.println("Message is");
                System.out.println(message);
                System.out.println(pe);
            }

            JSONArray hunterCoordinates = (JSONArray) jsonObject.get("hunter");
            Point hunterPoint = parseJSONArrayCoordinates(hunterCoordinates);

            JSONArray preyCoordinates = (JSONArray) jsonObject.get("prey");
            Point preyPoint = parseJSONArrayCoordinates(preyCoordinates);

            String huntDirection = (String) jsonObject.get("hunterDir");
            System.out.println("Mid publ parse hunterDirection is: " + huntDirection);
            Point hunterDir = serverDirectionToPoint(huntDirection);

            System.out.println("hunterPoint is null: " + (hunterPoint == null));

            long timeL = (Long) jsonObject.get("time");
            int time = (int) timeL;

            boolean gameOver = (Boolean) jsonObject.get("gameover");

            String wallsString = (String) jsonObject.get("wall");
            JSONArray walls = (JSONArray) jsonObject.get("wall");
            ArrayList<Wall> readInWalls = new ArrayList<Wall>();
            if (walls != null) {
                readInWalls = parseJSONArrayWalls(walls);
            }

            System.out.println("Done parsing Publisher message"); 
            updateGame(hunterPoint, preyPoint, hunterDir , readInWalls, time, gameOver);
            System.out.println("Done updating game with Publisher message"); 
        }

        catch (Exception e) {
            //System.out.println(e);
        }
    }

    public Point integerListToPoint(List<Integer> points) {
        Integer x = points.get(0);
        Integer y = points.get(1);
        return new Point(x, y);
    }

    public ArrayList<Wall> wallDataListToWallArrayList(List<WallData> wallsData) {
        int i = 0;
        ArrayList<Wall> returnWalls = new ArrayList<Wall>();
        for (WallData wd : wallsData) {
            int length = wd.getLength();
            Point wallDir = serverDirectionToPoint(wd.getDirection());
            Point wallStart = integerListToPoint(wd.getWallPosition());
            int id = i;
            Wall tempWall = new Wall(wallDir, wallStart, length, id);
            returnWalls.add(tempWall);
        }

        return returnWalls;
    }

    public void parsePublisherMessageGSON(String message) {
        System.out.println("Starting Publisher Message Parse");
        Gson gson = new Gson();
        PublisherData publisherDataObj = gson.fromJson(message,
                PublisherData.class);

        System.out.println(publisherDataObj.getHunterCoordinates());

        Point hunterPoint = integerListToPoint(publisherDataObj.getHunterCoordinates());
        Point preyPoint = integerListToPoint(publisherDataObj.getPreyCoordinates());
        Point hunterDir = serverDirectionToPoint(publisherDataObj.getHunterDirection());
        List<WallData> wallsData = publisherDataObj.getWalls();
        ArrayList<Wall> readInWalls = wallDataListToWallArrayList(wallsData);
        int time = publisherDataObj.getTime();
        boolean gameover = publisherDataObj.getGameover();
        
        System.out.println("Finished Publisher Message Parse");
        updateGame(hunterPoint, preyPoint, hunterDir, readInWalls, time, gameover);
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
