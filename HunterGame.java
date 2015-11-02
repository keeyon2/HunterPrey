import org.json.simple.JSONObject;

public class HunterGame extends Game {

    public HunterGame(int N, int M) throws Exception {
        super(N, M);
        connectToSockets(1991);
        startGame();
        System.out.println("Hunter Game");
    } 

    @Override
    public JSONObject MakeDecision() {
        return jsonCreator.MovingWOBorDWalls();
    }

    public boolean preyIsBehind() {
        return false;
    }
}
