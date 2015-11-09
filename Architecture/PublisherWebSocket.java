import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class PublisherWebSocket{
    private Session session;
    CountDownLatch latch = new CountDownLatch(1);
    GameWithPublisherSocket gameWithSocket;

    public PublisherWebSocket(Game gameToSendMessagesTo) {
        gameWithSocket = gameToSendMessagesTo;
    }

    @OnWebSocketMessage
    public void onText(Session session, String message) throws IOException {
        System.out.println("Received Publisher Message: " + message);
        gameWithSocket.ReceivedMessageFromPublisherSocket(message);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected to server");
        this.session=session;
        latch.countDown();
        gameWithSocket.ConnectionMadeWithPublisherSocket();
    }
     
    public void sendMessage(String str) {
        try {
            System.out.println("We are sending " + str + " to Publisher Web Socket");
            session.getRemote().sendString(str);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
     
    public CountDownLatch getLatch() {
        return latch;
    }
}
