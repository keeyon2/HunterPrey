import java.awt.Point;
import java.net.URI;
 
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;

public class Main {
    public Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Please pass in N and M and Team");
        }

        else {
            int N = Integer.parseInt(args[0]);
            int M = Integer.parseInt(args[1]);
            String player = args[2];
            /*
            String publisherDest = "ws://localhost:1990";
            String hunterDest = "ws://localhost:1991";
            String playerDest = "ws://localhost:1992";
            WebSocketClient publClient = new WebSocketClient();
            WebSocketClient hunterClient = new WebSocketClient();
            WebSocketClient preyClient = new WebSocketClient();
            
            try { 
                EvasionWebSocket socket = new EvasionWebSocket();
                publClient.start();
                URI publURI = new URI(publisherDest);
                ClientUpgradeRequest request = new ClientUpgradeRequest();
                publClient.connect(socket, publURI, request);
                //socket.sendMessage("echo");
                //socket.sendMessage("test");
                //Thread.sleep(10000l);
            } 
            catch (Throwable t) {
                t.printStackTrace();
            } 
            finally {
                try {
                    publClient.stop();
                } 
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            */
            Game game;
            if (player.equals("hunter")) {
                game = new HunterGame(N, M, "ws://localhost:1991");
            }

            else {
                game = new PreyGame(N, M, "ws://localhost:1992");
            }
            
            /*
            Wall test = new Wall(new Point(1, 0), new Point(1, 1), 200, 0);
            game.addWall(test);
            game.printGrid(); 
            */
        }
    }
}
