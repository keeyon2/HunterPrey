import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class jsonCreator {

//    public static void main(String ... orange) {
//        JSONObject j = MovingWOBorDWalls();
//        System.out.println(j);
//		j = BuildWalls("N", 22);
//		System.out.println(j);
//		j = DeleteWalls(20);
//		System.out.println(j);
//		j = GetPositions();
//		System.out.println(j);
//
//    }

    //for both Hunter and Prey
	public static JSONObject GetPositions(){
		JSONObject obj = new JSONObject();
		obj.put("command", "P");
		return obj;
	}

	public static JSONObject GetWalls(){
		JSONObject obj = new JSONObject();
		obj.put("command", "W");
		return obj;
	}


	//for Prey
	public static JSONObject NotMoving(){
		JSONObject obj = new JSONObject();
		obj.put("command", "M");
		return obj;
	}


	//direction can be "N", "S", "NE", etc
	public static JSONObject Moving(String direction){
		JSONObject obj = new JSONObject();
		obj.put("command", "M");
		obj.put("direction", direction);
		return obj;
	}

	//for Hunter
	public static JSONObject MovingWOBorDWalls(){
		JSONObject obj = new JSONObject();
		obj.put("command", "M");
		return obj;
	}


	public static JSONObject DeleteWalls(int wallIndex) {
		JSONObject obj = new JSONObject();
		obj.put("command", "D");
		obj.put("wallIndex", wallIndex);
		return obj;
	}

	//direction can be "N", "S", "NE", etc
	public static JSONObject BuildWalls(String direction, int length) {
		JSONObject obj = new JSONObject();
		JSONObject walls = new JSONObject();
		obj.put("command", "B");
		walls.put("length", length);
		walls.put("direction", direction);
		obj.put("wall", walls);
		return obj;
	}




}
