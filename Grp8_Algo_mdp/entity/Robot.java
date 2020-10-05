package entity;

import constant.Constants;
import constant.Constants.DIRECTION;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;

public class Robot {
	  
	private int posRow;					// Y/Row coord of the robot
	private int posCol;					// X/Col coord of the robot
	private DIRECTION currDir;			//Direction the robot is facing
	private boolean reachedGoal;
	    
	public Robot(int posRow, int posCol, DIRECTION curDir) {
	    this.posRow = posRow;
	    this.posCol = posCol;
	    this.currDir =curDir;
	   }
	
	 public Robot(int posRow, int posCol, DIRECTION currDir, boolean reachedGoal) {
	        this.posRow = posRow;
	        this.posCol = posCol;
	        this.currDir = currDir;
	        this.reachedGoal = reachedGoal;
	    }  
	 public int getPosRow() {
	        return posRow;
	 }

	 public void setPosRow(int posRow) {
	        this.posRow = posRow;
	    }

	    public int getPosCol() {
	        return posCol;
	    }

	    public void setPosCol(int posCol) {
	        this.posCol = posCol;
	    }

	    public DIRECTION getCurrDir() {
	        return currDir;
	    }

	    public void setCurrDir(DIRECTION currDir) {
	        this.currDir = currDir;
	    }
	    /*
	    public boolean isReachedGoal() {
	        return reachedGoal;
	    }

	    public void setReachedGoal(boolean reachedGoal) {
	        this.reachedGoal = reachedGoal;
	    }
	    */
	    
	    public void move(MOVEMENT m){
	        switch(m){
	            case FORWARD:
	                if(this.currDir == DIRECTION.NORTH) this.setPosRow(posRow + 1);
	                else if(this.currDir == DIRECTION.SOUTH) this.setPosRow(posRow - 1);
	                else if(this.currDir == DIRECTION.EAST) this.setPosCol(posCol + 1);
	                else if(this.currDir == DIRECTION.WEST) this.setPosCol(posCol - 1);
	                break;
	            case BACKWARD:
	                // if(this.currDir == DIRECTION.NORTH) this.setPosRow(posRow - 1);
	                // else if(this.currDir == DIRECTION.SOUTH) this.setPosRow(posRow + 1);
	                // else if(this.currDir == DIRECTION.EAST) this.setPosCol(posCol - 1);
	                // else if(this.currDir == DIRECTION.WEST) this.setPosCol(posCol + 1);
	                // break;
	            case LEFT:
	            case RIGHT:
	                break;
	        }
	    }
	    
	    public void turn(MOVEMENT m){
	        switch(this.currDir){
	            case NORTH:
	                if(m == MOVEMENT.RIGHT) this.setCurrDir(DIRECTION.EAST);
	                else if(m == MOVEMENT.LEFT) this.setCurrDir(DIRECTION.WEST);
	                break;
	            case SOUTH:
	                if(m == MOVEMENT.RIGHT) this.setCurrDir(DIRECTION.WEST);
	                else if(m == MOVEMENT.LEFT) this.setCurrDir(DIRECTION.EAST);
	                break;
	            case EAST:
	                if(m == MOVEMENT.RIGHT) this.setCurrDir(DIRECTION.SOUTH);
	                else if(m == MOVEMENT.LEFT) this.setCurrDir(DIRECTION.NORTH);
	                break;
	            case WEST:
	                if(m == MOVEMENT.RIGHT) this.setCurrDir(DIRECTION.NORTH);
	                else if(m == MOVEMENT.LEFT) this.setCurrDir(DIRECTION.SOUTH);
	                break;
	        }
	    }
	    
	   
	    //Check is 2 cell ahead of robot is valid for moving.
	    public boolean isMovementValid(Map exploredMap, MOVEMENT m){
	        Cell destinationCell_1 = new Cell(-1, -1);
	        Cell destinationCell_2 = new Cell(-1, -1);
	        Cell destinationCell_3 = new Cell(-1, -1);

	        Constants.DIRECTION absoluteDir = null;

	        switch(this.currDir){
	            case NORTH:
	                if(m == MOVEMENT.FORWARD) absoluteDir = DIRECTION.NORTH;
	                else if(m == MOVEMENT.BACKWARD) absoluteDir = DIRECTION.SOUTH;
	                else if(m == MOVEMENT.RIGHT) absoluteDir = DIRECTION.EAST;
	                else if(m == MOVEMENT.LEFT) absoluteDir = DIRECTION.WEST;
	                break;
	            case SOUTH:
	                if(m == MOVEMENT.FORWARD) absoluteDir = DIRECTION.SOUTH;
	                else if(m == MOVEMENT.BACKWARD) absoluteDir = DIRECTION.NORTH;
	                else if(m == MOVEMENT.RIGHT) absoluteDir = DIRECTION.WEST;
	                else if(m == MOVEMENT.LEFT) absoluteDir = DIRECTION.EAST;
	                break;
	            case EAST:
	                if(m == MOVEMENT.FORWARD) absoluteDir = DIRECTION.EAST;
	                else if(m == MOVEMENT.BACKWARD) absoluteDir = DIRECTION.WEST;
	                else if(m == MOVEMENT.RIGHT) absoluteDir = DIRECTION.SOUTH;
	                else if(m == MOVEMENT.LEFT) absoluteDir = DIRECTION.NORTH;
	                break;
	            case WEST:
	                if(m == MOVEMENT.FORWARD) absoluteDir = DIRECTION.WEST;
	                else if(m == MOVEMENT.BACKWARD) absoluteDir = DIRECTION.EAST;
	                else if(m == MOVEMENT.RIGHT) absoluteDir = DIRECTION.NORTH;
	                else if(m == MOVEMENT.LEFT) absoluteDir = DIRECTION.SOUTH;
	                break;
	        }
	        
	        //determine position of cell that are 2 blocks infront of robot in the direction it is heading
	        switch(absoluteDir){
	            case NORTH:
	                destinationCell_1.setRowPos(this.getPosRow() + 2);
	                destinationCell_1.setColPos(this.getPosCol());
	                destinationCell_2.setRowPos(this.getPosRow() + 2);
	                destinationCell_2.setColPos(this.getPosCol() - 1);
	                destinationCell_3.setRowPos(this.getPosRow() + 2);
	                destinationCell_3.setColPos(this.getPosCol() + 1);
	                break;
	            case SOUTH:
	                destinationCell_1.setRowPos(this.getPosRow() - 2);
	                destinationCell_1.setColPos(this.getPosCol());
	                destinationCell_2.setRowPos(this.getPosRow() - 2);
	                destinationCell_2.setColPos(this.getPosCol() + 1);
	                destinationCell_3.setRowPos(this.getPosRow() - 2);
	                destinationCell_3.setColPos(this.getPosCol() - 1);
	                break;
	            case WEST:
	                destinationCell_1.setRowPos(this.getPosRow());
	                destinationCell_1.setColPos(this.getPosCol() - 2);
	                destinationCell_2.setRowPos(this.getPosRow() - 1);
	                destinationCell_2.setColPos(this.getPosCol() - 2);
	                destinationCell_3.setRowPos(this.getPosRow() + 1);
	                destinationCell_3.setColPos(this.getPosCol() - 2);
	                break;
	            case EAST:
	                destinationCell_1.setRowPos(this.getPosRow());
	                destinationCell_1.setColPos(this.getPosCol() + 2);
	                destinationCell_2.setRowPos(this.getPosRow() + 1);
	                destinationCell_2.setColPos(this.getPosCol() + 2);
	                destinationCell_3.setRowPos(this.getPosRow() - 1);
	                destinationCell_3.setColPos(this.getPosCol() + 2);
	                break;
	        }
	        
	        if(destinationCell_1.isCellValid() && destinationCell_2.isCellValid() && destinationCell_3.isCellValid()){
	            if(!exploredMap.getMapGrid()[destinationCell_1.getRowPos()][destinationCell_1.getColPos()].isObstacle() &&
	            !exploredMap.getMapGrid()[destinationCell_2.getRowPos()][destinationCell_2.getColPos()].isObstacle() && 
	            !exploredMap.getMapGrid()[destinationCell_3.getRowPos()][destinationCell_3.getColPos()].isObstacle()){
	                return true;
	            }
	        }
	        return false;
	    }
	    
	    public boolean isDisplacementValid(Map exploredMap, DIRECTION d){

	        Cell destinationCell_1 = new Cell(-1, -1);
	        Cell destinationCell_2 = new Cell(-1, -1);
	        Cell destinationCell_3 = new Cell(-1, -1);

	        switch(d){
	            case NORTH:
	                destinationCell_1.setRowPos(this.getPosRow() + 2);
	                destinationCell_1.setColPos(this.getPosCol());
	                destinationCell_2.setRowPos(this.getPosRow() + 2);
	                destinationCell_2.setColPos(this.getPosCol() - 1);
	                destinationCell_3.setRowPos(this.getPosRow() + 2);
	                destinationCell_3.setColPos(this.getPosCol() + 1);
	                break;
	            case SOUTH:
	                destinationCell_1.setRowPos(this.getPosRow() - 2);
	                destinationCell_1.setColPos(this.getPosCol());
	                destinationCell_2.setRowPos(this.getPosRow() - 2);
	                destinationCell_2.setColPos(this.getPosCol() + 1);
	                destinationCell_3.setRowPos(this.getPosRow() - 2);
	                destinationCell_3.setColPos(this.getPosCol() - 1);
	                break;
	            case WEST:
	                destinationCell_1.setRowPos(this.getPosRow());
	                destinationCell_1.setColPos(this.getPosCol() - 2);
	                destinationCell_2.setRowPos(this.getPosRow() - 1);
	                destinationCell_2.setColPos(this.getPosCol() - 2);
	                destinationCell_3.setRowPos(this.getPosRow() + 1);
	                destinationCell_3.setColPos(this.getPosCol() - 2);
	                break;
	            case EAST:
	                destinationCell_1.setRowPos(this.getPosRow());
	                destinationCell_1.setColPos(this.getPosCol() + 2);
	                destinationCell_2.setRowPos(this.getPosRow() + 1);
	                destinationCell_2.setColPos(this.getPosCol() + 2);
	                destinationCell_3.setRowPos(this.getPosRow() - 1);
	                destinationCell_3.setColPos(this.getPosCol() + 2);
	                break;
	        }

	        if(destinationCell_1.isCellValid() && destinationCell_2.isCellValid() && destinationCell_3.isCellValid()){
	            if(!exploredMap.getMapGrid()[destinationCell_1.getRowPos()][destinationCell_1.getColPos()].isObstacle() &&
	            !exploredMap.getMapGrid()[destinationCell_2.getRowPos()][destinationCell_2.getColPos()].isObstacle() && 
	            !exploredMap.getMapGrid()[destinationCell_3.getRowPos()][destinationCell_3.getColPos()].isObstacle()){
	                return true;
	            }
	        }
	        return false;
	    }
}
