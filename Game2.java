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

public abstract class Game2 implements GameWithPublisherSocket, GameWithPlayerSocket {
 
    JSONParser parser = new JSONParser();

    WebSocketClient publClient = new WebSocketClient();
    WebSocketClient playerClient = new WebSocketClient();
    PublisherWebSocket publSocket = new PublisherWebSocket(this);
    PlayerWebSocket playerSocket = new PlayerWebSocket(this);


    public Game() throws Exception {
        String playerDest = "ws://localhost:1991";
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
    
    // Receives JSon object as string
    public void ReceivedMessageFromPlayerSocket(String message) {
        // This will update your game
        parsePlayerMessage(message);
    }  

    public void ReceivedMessageFromPublisherSocket(String message) {
        // Received publisher message
        // Turn is over, Time to make new turn
        parsePublisherMessage(message);
        // USE this to decide your move
        playerMakeMove();
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

    public synchronized void parsePlayerMessage(String message) {
        System.out.println(message);
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                Object obj = parser.parse(message);
                jsonObject = (JSONObject) obj;
            }

            catch (ParseException pe) {
                System.out.println(pe);
            }

            String commandValue = (String) jsonObject.get("command");

            if (commandValue.equals("W")) {
                JSONArray walls = (JSONArray) jsonObject.get("walls");
                ArrayList<Wall> readInWalls = parseJSONArrayWalls(walls);

                updateWalls(readInWalls);
            }

            else if (commandValue.equals("P")) {
                JSONArray hunterCoordinates = (JSONArray) jsonObject.get("hunter");
                Point hunterPoint = parseJSONArrayCoordinates(hunterCoordinates);

                JSONArray preyCoordinates = (JSONArray) jsonObject.get("prey");
                Point preyPoint = parseJSONArrayCoordinates(preyCoordinates);

                updatePositions(hunterPoint, preyPoint);
            }

            else {
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Wall> parseJSONArrayWalls(JSONArray walls) {
        ArrayList<Wall> returnWalls = new ArrayList<Wall>();
        //Update walls count
        if (walls != null) {
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
        }

        return returnWalls;
    }

    public synchronized void parsePublisherMessage(String message) {
        message.replace("false", "\"false\"");
        message.replace("true", "\"true\"");
        try {
            JSONObject jsonObject = new JSONObject();
            try {
                Object obj = parser.parse(message);
                jsonObject = (JSONObject) obj;
            }

            catch (ParseException pe) {
                System.out.println(pe);
            }

            JSONArray hunterCoordinates = (JSONArray) jsonObject.get("hunter");
            Point hunterPoint = parseJSONArrayCoordinates(hunterCoordinates);

            JSONArray preyCoordinates = (JSONArray) jsonObject.get("prey");
            Point preyPoint = parseJSONArrayCoordinates(preyCoordinates);

            String huntDirection = (String) jsonObject.get("hunterDir");
            Point hunterDir = serverDirectionToPoint(huntDirection);


            long timeL = (Long) jsonObject.get("time");
            int time = (int) timeL;

            boolean gameOver = (Boolean) jsonObject.get("gameover");

            String wallsString = (String) jsonObject.get("wall");
            JSONArray walls = (JSONArray) jsonObject.get("wall");
            ArrayList<Wall> readInWalls = new ArrayList<Wall>();
            readInWalls = parseJSONArrayWalls(walls);

            // You should update the game
            updateGame(hunterPoint, preyPoint, hunterDir , readInWalls, time, gameOver);
        }

        catch (Exception e) {
            System.out.println(e);
        }
    }

    public abstract JSONObject MakeDecision();
}
