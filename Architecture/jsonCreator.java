import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    // New Wall Functions
    public static JSONObject BuildVerticleWall() {
        JSONObject obj = new JSONObject();
        JSONObject wallObj = new JSONObject();

        wallObj.put("direction", "V");
        obj.put("command", "B");
        obj.put("wall", wallObj);
        return obj;
    } 

    // New Wall Functions
    public static JSONObject BuildHorizontalWall() {
        JSONObject obj = new JSONObject();
        JSONObject wallObj = new JSONObject();

        wallObj.put("direction", "H");
        obj.put("command", "B");
        obj.put("wall", wallObj);
        return obj;
    } 

    // Build and Delete
    public static JSONObject BuildAndDeleteWall(String dir, ArrayList<Integer> deleteWallIDs) {
       JSONObject obj = new JSONObject();
       JSONObject wallObj = new JSONObject();
       JSONArray wallIDs = new JSONArray(); 

       for (int id : deleteWallIDs) {
           wallIDs.add(id);
       }

       wallObj.put("direction", dir);
       obj.put("command", "BD");
       obj.put("wall", wallObj);
       obj.put("wallIds", wallIDs);

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
