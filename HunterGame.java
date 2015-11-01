public class HunterGame extends Game {

    public HunterGame(int N, int M) {
        super(N, M);
        connectToSockets(1991);
    } 
}
