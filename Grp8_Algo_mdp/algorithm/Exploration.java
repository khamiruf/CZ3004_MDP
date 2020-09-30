package algorithm;

import constant.Constants.MOVEMENT;
import entity.Map;
import entity.Robot;
import util.NextMove;

public class Exploration {
	 public Exploration() {
	    }

	    public NextMove nextMove(Map exploreMap, Robot robot, int moveCounter){

	        NextMove nextMove = new NextMove(false, null);

	        if(moveCounter > 100){
	            nextMove.setHasNextMove(false); 	            
	        }
	        
	        else if(robot.isMovementValid(exploreMap, MOVEMENT.RIGHT)){
	            nextMove.setHasNextMove(true);
	            nextMove.setNextMove(MOVEMENT.RIGHT);
	        }
	        else if(robot.isMovementValid(exploreMap, MOVEMENT.FORWARD)){
	            nextMove.setHasNextMove(true);
	            nextMove.setNextMove(MOVEMENT.FORWARD);
	        }
	        else if(robot.isMovementValid(exploreMap, MOVEMENT.LEFT)){
	            nextMove.setHasNextMove(true);
	            nextMove.setNextMove(MOVEMENT.LEFT);
	        }
	        else if(robot.isMovementValid(exploreMap, MOVEMENT.BACKWARD)){
	            nextMove.setHasNextMove(true);
	            nextMove.setNextMove(MOVEMENT.BACKWARD);
	        }
	         
	        return nextMove;

	    }
}
