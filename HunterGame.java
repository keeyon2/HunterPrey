import org.json.simple.JSONObject;

public class HunterGame extends Game {

    public HunterGame(int N, int M) throws Exception {
        super(N, M);
        connectToSockets(1991);
        startGame();
    } 

    @Override
    public JSONObject MakeDecision() {
        return jsonCreator.MovingWOBorDWalls();
    }

    public boolean preyIsBehind() {
        return false;
    }
}
