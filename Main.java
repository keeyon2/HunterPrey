import java.awt.Point;

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

            Game game;
            if (player.equals("hunter")) {
                game = new HunterGame(N, M);
            }

            else {
                game = new PreyGame(N, M);
            }

            Wall test = new Wall(new Point(1, 0), new Point(1, 1), 200, 0);
            game.addWall(test);
            game.printGrid(); 
        }
    }
}
