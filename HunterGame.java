import org.json.simple.JSONObject;

public class HunterGame extends Game {

    public HunterGame(int N, int M, String playerDest) throws Exception {
        super(N, M, playerDest);
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
