import java.util.ArrayList;
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
    }  

    public void ReceivedMessageFromPublisherSocket(String message) {
        parsePublisherMessage(message);
        okToMakeMove = true;
    }

    public void ConnectionMadeWithPlayerSocket() {
        this.connectedToPlayer = true;
    }

    public void ConnectionMadeWithPublisherSocket() {
        this.connectedToPublisher = true;
    }

    public void startGame () throws Exception {
        JSONObject decisionJSONObject;
        while(!gameOver) {
            System.out.println("Doing work");
            if (connectedToPublisher && connectedToPlayer && okToMakeMove) {
                System.out.println("Start of new turn");
                System.out.println("Time is: " + time);
                sendPToPlayerServer();
                decisionJSONObject = MakeDecision();
                System.out.println("Made Decision");
                System.out.println("Decision is: ");
                System.out.println(decisionJSONObject.toJSONString());
                sendDecision(decisionJSONObject);
                okToMakeMove = false;
                
                // Keep this here
                // Waiting for server Response
                while (!okToMakeMove) {
                    // okToMakeMove is updated to true
                    // Whenever the publisher socket 
                    // returns to us the end up a move
                }
            }
        }

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
    }

    public void sendPToPlayerServer() {
        JSONObject positions = jsonCreator.GetPositions();
        playerSocket.sendMessage(positions.toJSONString());
    }

    public void sendDecision(JSONObject decision) {
        playerSocket.sendMessage(decision.toJSONString());
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
        updatePositions(hunterPoint, preyPoint);

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

    /*
    public boolean connectToSockets(int port) throws Exception {
        boolean publisherConnectionWorked = false;
        boolean playerConnectionWorked = false;
        publisherConnectionWorked = connectToPublisherSocket();
        playerConnectionWorked = connectToPlayerSocket(port);
        System.out.println("Connected to publisher Socket: " + publisherConnectionWorked);
        System.out.println("Connected to player Socket: " + playerConnectionWorked);
        return (publisherConnectionWorked && playerConnectionWorked); 
    }
    */

    /*
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
    */

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

    public void parsePlayerMessage(String message) {
        System.out.println("Parsing Player Message: ");
        System.out.println(message);
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                Object obj = parser.parse(message);
                jsonObject = (JSONObject) obj;
            }

            catch (ParseException pe) {
                System.out.println("PARSE ERROR");
                System.out.println(pe);
            }

            String commandValue = (String) jsonObject.get("command");

            if (commandValue.equals("W")) {
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

                updateWalls(readInWalls);
            }

            else if (commandValue.equals("P")) {
                String hunterPosition = (String) jsonObject.get("hunter");
                Point hunterPoint = stringToPoint(hunterPosition);
                String preyPosition = (String) jsonObject.get("prey");
                Point preyPoint = stringToPoint(preyPosition);

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

    public void parsePublisherMessage(String message) {
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                Object obj = parser.parse(message);
                jsonObject = (JSONObject) obj;
            }

            catch (ParseException pe) {
                System.out.println("PARSE ERROR");
                System.out.println(pe);
            }

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
        }

        catch (Exception e) {
            System.out.println(e);
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
