package entity;

import java.util.ArrayList;

import constant.Constants;
import constant.Constants.DIRECTION;
import entity.Cell;

public class Map {

	private Cell[][] mapArena;
	private Cell startGoal;
	private Cell endGoal;
	private Cell wayPoint;
	
	public Map() {
		this.mapArena = new Cell[Constants.MAX_ROW][Constants.MAX_COL];
		
		for(int i=0; i<Constants.MAX_ROW; i++) {
			for(int y=0; y<Constants.MAX_COL; y++) {
				this.mapArena[i][y] = new Cell(i,y,false);
			}
		}
		//Setting start/end goal as explored 
		startGoal = this.mapArena[1][1];
		endGoal = this.mapArena[18][13];
		wayPoint = this.mapArena[1][1];		
	}
	
	public Map(Cell[][] sMap) {
		this.mapArena = sMap;
	}
	
	
	public Cell[][] getMapGrid(){
		return this.mapArena;
	}
	
	public Cell getStartGoalPosition() {
		return this.startGoal;
	}
	
	public Cell getEndGoalPosition() {
		return this.endGoal;
	}
	
	public Cell getWayPoint() {
		return this.wayPoint;
	}
	
	public void setWayPoint(int rowPos, int colPos) {
		this.wayPoint = new Cell(rowPos, colPos);
	}
	
	//---ZW ADDED
	
	public boolean checkIfRobotAtStartPos(Robot r) {
		
		if(r.getPosRow() == startGoal.getRowPos() && r.getPosCol() == startGoal.getColPos()) {
			return true;
		}
		return false;
	}
	//-------------
	
	public boolean checkIfRobotHere(Robot robot, int cellRow, int cellCol){
        if(cellRow == robot.getPosRow() || cellRow == robot.getPosRow() + 1 || cellRow == robot.getPosRow() - 1){
            if(cellCol == robot.getPosCol() || cellCol == robot.getPosCol() + 1 || cellCol == robot.getPosCol() - 1){
                return true;
            }
        }
        return false;
    }

    public boolean checkIfRobotFrontHere(Robot robot, int cellRow, int cellCol){
        int robotFrontRow = -1;
        int robotFrontCol = -1;

        switch(robot.getCurrDir()){
            case NORTH:
                robotFrontRow = robot.getPosRow() + 1;
                robotFrontCol = robot.getPosCol();
                break;
            case SOUTH:
                robotFrontRow = robot.getPosRow() - 1;
                robotFrontCol = robot.getPosCol();
                break;
            case EAST:
                robotFrontRow = robot.getPosRow();
                robotFrontCol = robot.getPosCol() + 1;
                break;
            case WEST:
                robotFrontRow = robot.getPosRow();
                robotFrontCol = robot.getPosCol() -1;
                break;    
        }
        
        return (robotFrontRow == cellRow && robotFrontCol == cellCol);
    }
    
    //Set what the robot can see(from its direction & sensor)from its current position against the real map
    public void setExploredCells(Robot robot, Map realMap){
        ArrayList<Cell> explorableCells =new ArrayList<Cell>();

        //add cells occupied by robot as explorable cells (9)
        // Original code, commented off becos need to set start as unexplored to achieve loop for exploration
        for(int r : Constants.WITHIN_3BY3){
            for(int c : Constants.WITHIN_3BY3){
           
            	Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
            	explorableCells.add(tempCell);
            	
            }
        }

        if(robot.getCurrDir() == DIRECTION.NORTH){
            explorableCells.addAll(this.northExploredCells(robot, realMap));
           
        }
        else if(robot.getCurrDir() == DIRECTION.EAST){
            explorableCells.addAll(this.eastExploredCells(robot, realMap));
           
        }
        else if(robot.getCurrDir() == DIRECTION.SOUTH){
            explorableCells.addAll(this.southExploredCells(robot, realMap));
           
        }
        else if(robot.getCurrDir() == DIRECTION.WEST){
            explorableCells.addAll(this.westExploredCells(robot, realMap));
         
        }

       
        for(int i=0; i< explorableCells.size(); i++){
            //System.out.println("Row: " + explorableCells.get(i).getRow() + " Col: " + explorableCells.get(i).getCol());
            Cell explorableCell = explorableCells.get(i);
       
            this.getMapGrid()[explorableCell.getRowPos()][explorableCell.getColPos()].setExploredState(true);
            //if the cell is an obstacle in the realmap, set it as obstacle in the robot's map.
            if(realMap.getMapGrid()[explorableCell.getRowPos()][explorableCell.getColPos()].isObstacle()) {
            	this.getMapGrid()[explorableCell.getRowPos()][explorableCell.getColPos()].setObstacle(true);
            	 for(int r : Constants.WITHIN_3BY3){ //set cells around obstacle cell as virtual wall
                     for(int c : Constants.WITHIN_3BY3){                     
                    	  Cell tempCell = new Cell(explorableCell.getRowPos() + r, explorableCell.getColPos() + c);
                          if(tempCell.isCellValid()){ //check if virtual wall is within arena
                              this.getMapGrid()[tempCell.getRowPos()][tempCell.getColPos()].setVirtualWall(true);
                             // System.out.println("virtual wall: "+tempCell.getRowPos() + "_"+tempCell.getColPos());
                          }  
                     }
                 }
            }
          
            
        }
    }
    
    public ArrayList<Cell> northExploredCells(Robot robot, Map realMap){
        ArrayList<Cell> explorableCells =new ArrayList<Cell>();

        //find explorable cells in front of robot:
        //      X X X
        //      X X X
        //      R F R
        //      R R R
        //      R R R
        for(int c : Constants.WITHIN_3BY3){
            for(int r : Constants.SHORT_SENSOR_ADD){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
              
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }

        //find explorable cells at right of robot:
        //      R F R X X
        //      R R R X X
        //      R R R X X
        for(int r : Constants.WITHIN_3BY3){
            for(int c : Constants.SHORT_SENSOR_ADD){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
              
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                
                    break;
                }
            }
        }

        //find explorable cells at left of robot:
        //      X X R F R
        //  X X X X R R R
        //  X X X X R R R
        for(int r = -1; r < 1; r++){
            for(int c : Constants.LONG_SENSOR_SUB){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }
        for(int c : Constants.SHORT_SENSOR_SUB){
                Cell tempCell = new Cell(robot.getPosRow() + 1, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + 1][robot.getPosCol() + c].isObstacle()){
                    break;
                }
        }

        return explorableCells;
    }

    public ArrayList<Cell> eastExploredCells(Robot robot, Map realMap){
        ArrayList<Cell> explorableCells =new ArrayList<Cell>();

        //find explorable cells in front of robot:
        //      R R R X X
        //      R R F X X
        //      R R R X X
        for(int r: Constants.WITHIN_3BY3){
            for(int c : Constants.SHORT_SENSOR_ADD){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }

        //find explorable cells in right of robot:
        //      R R R
        //      R R F
        //      R R R
        //      X X X
        //      X X X
        for(int c : Constants.WITHIN_3BY3){
            for(int r : Constants.SHORT_SENSOR_SUB){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }

        //find explorable cells in left of robot:
        //      X X 
        //      X X 
        //      X X X
        //      X X X
        //      R R R
        //      R R F
        //      R R R
        for(int c = -1; c < 1; c++){
            for(int r: Constants.LONG_SENSOR_ADD){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }
        for(int r: Constants.SHORT_SENSOR_ADD){
            Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + 1);
            if(tempCell.isCellValid()){
                explorableCells.add(tempCell);
            }
            if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + 1].isObstacle()){
                break;
            }
        }

        return explorableCells;
    }

    public ArrayList<Cell> southExploredCells(Robot robot, Map realMap){
        ArrayList<Cell> explorableCells =new ArrayList<Cell>();

        //find explorable cells in front of robot:
        //      R R R
        //      R R R
        //      R F R
        //      X X X
        //      X X X
        for(int c : Constants.WITHIN_3BY3){
            for(int r : Constants.SHORT_SENSOR_SUB){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }

        //find explorable cells in right of robot:
        //  X X R R R
        //  X X R R R
        //  X X R F R
        for(int r: Constants.WITHIN_3BY3){
            for(int c: Constants.SHORT_SENSOR_SUB){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }

        //find explorable cells in left of robot:
        //      R R R X X X X
        //      R R R X X X X
        //      R F R X X
        for(int r = 0; r < 2; r++){
            for(int c: Constants.LONG_SENSOR_ADD){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }
        for(int c: Constants.SHORT_SENSOR_ADD){
            Cell tempCell = new Cell(robot.getPosRow() - 1, robot.getPosCol() + c);
            if(tempCell.isCellValid()){
                explorableCells.add(tempCell);
            }
            if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() - 1][robot.getPosCol() + c].isObstacle()){
                break;
            }
        }


        return explorableCells;
    }

    public ArrayList<Cell> westExploredCells(Robot robot, Map realMap){
        ArrayList<Cell> explorableCells =new ArrayList<Cell>();

        //find explorable cells in front of robot:
        //  X X R R R
        //  X X F R R
        //  X X R R R
        for(int r: Constants.WITHIN_3BY3){
            for(int c: Constants.SHORT_SENSOR_SUB){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }

        //find explorable cells in right of robot:
        //      X X X
        //      X X X
        //      R R R
        //      F R R
        //      R R R
        for(int c : Constants.WITHIN_3BY3){
            for(int r : Constants.SHORT_SENSOR_ADD){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }

        //find explorable cells in left of robot:
        //      R R R
        //      F R R
        //      R R R
        //      X X X
        //      X X X
        //        X X
        //        X X
        for(int c = 0; c < 2; c++){
            for(int r: Constants.LONG_SENSOR_SUB){
                Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() + c);
                if(tempCell.isCellValid()){
                    explorableCells.add(tempCell);
                }
                if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() + c].isObstacle()){
                    break;
                }
            }
        }
        for(int r: Constants.SHORT_SENSOR_SUB){
            Cell tempCell = new Cell(robot.getPosRow() + r, robot.getPosCol() - 1);
            if(tempCell.isCellValid()){
                explorableCells.add(tempCell);
            }
            if(tempCell.isCellValid() && realMap.getMapGrid()[robot.getPosRow() + r][robot.getPosCol() - 1].isObstacle()){
                break;
            }
        }

        return explorableCells;
    }
 
    public String getMDF1() {
    	String msg="11";
    	String mdfString ="";
		for(int i=0; i<Constants.MAX_ROW; i++) {
			for(int y=0; y<Constants.MAX_COL; y++) {
				msg += (this.mapArena[i][y].getExploredState())? 1:0;
			}
		}
	
    	msg +=11;
    	for(int i=0;i<msg.length();i+=4) {
    		mdfString += Integer.toHexString(Integer.parseInt(msg.substring(i, i+4),2));
    	}
    	
    	
    	return mdfString;
    }
    
    public String getMDF2() {
    	String msg="";
    	String mdfString ="";
		for(int i=0; i<Constants.MAX_ROW; i++) {
			for(int y=0; y<Constants.MAX_COL; y++) {
				if(this.mapArena[i][y].getExploredState()) {
					msg+= (mapArena[i][y].isObstacle())? 1:0; 	//if cell is explored but is not an obstacle, it must be a path.
				}
				
			}
		}
		int bytelength= msg.length() % 4;
		System.out.println("Byte:" + bytelength);
		if(bytelength!=0) {
			switch(bytelength) {
			case 1: msg += "000"; break;
			case 2: msg += "00";  break;
			case 3: msg += "0"; break;
	
			}	
		}
		
    	for(int i=0;i<msg.length();i+=4) {
    		mdfString += Integer.toHexString(Integer.parseInt(msg.substring(i, i+4),2));
    	}
    	
    	
    	return mdfString;
    }
    
    
    
    //----------------------------------------------------------------------------------------------------------
    public String setExploredCells(Robot robot, String sensorDataInString){
        //sensorData refers to the data received from the sensor
        //sensorData[0] refers to right back sensor
        //sensorData[1] refers to right front sensor
        //sensorData[2] refers to left sensor
        //sensorData[3] refers to front right sensor
        //sensorData[4] refers to front left sensor
        //sensorData[5] refers to front middle sensor
    	
    	for(int r : Constants.WITHIN_3BY3){
            for(int c : Constants.WITHIN_3BY3){
           
            	this.mapArena[robot.getPosRow() + r][robot.getPosCol() + c].setExploredState(true);
            	
            }
        }
    	
    	String result = "";
    	
    	
    	
        ArrayList<Integer> sensorData = new ArrayList<>();
        for (int i = 0; i < sensorDataInString.length(); i++) {
            sensorData.add(Character.getNumericValue(sensorDataInString.charAt(i)));
        }

        ArrayList<Cell> obstacleCells = new ArrayList<Cell>();
        ArrayList<Cell> emptyCells = new ArrayList<Cell>();

        if(robot.getCurrDir() == DIRECTION.NORTH){
            obstacleCells = northObstacleCells(robot, sensorData);
            emptyCells = northEmptyCells(robot, sensorData);
        }
        else if(robot.getCurrDir() == DIRECTION.SOUTH){
            obstacleCells = southObstacleCells(robot, sensorData);
            emptyCells = southEmptyCells(robot, sensorData);
        }
        else if(robot.getCurrDir() == DIRECTION.EAST){
            obstacleCells = eastObstacleCells(robot, sensorData);
            emptyCells = eastEmptyCells(robot, sensorData);
        }
        else if(robot.getCurrDir() == DIRECTION.WEST){
            obstacleCells = westObstacleCells(robot, sensorData);
            emptyCells = westEmptyCells(robot, sensorData);
        }
        
       
        
        for(int i=0; i<obstacleCells.size(); i++){
            int tempRow = obstacleCells.get(i).getRowPos();
            int tempCol = obstacleCells.get(i).getColPos();

            if(isCellValid(tempRow, tempCol) && !this.getMapGrid()[tempRow][tempCol].getExploredState()){
                this.getMapGrid()[tempRow][tempCol].setExploredState(true);
                this.getMapGrid()[tempRow][tempCol].setObstacle(true);
                setVirtualWall(this.getMapGrid()[tempRow][tempCol]);
                result += tempCol +"," + tempRow + "|";
     
            }
        }

        for(int i=0; i<emptyCells.size(); i++){
            int tempRow = emptyCells.get(i).getRowPos();
            int tempCol = emptyCells.get(i).getColPos();

            if(isCellValid(tempRow, tempCol) && !this.getMapGrid()[tempRow][tempCol].getExploredState()){
                this.getMapGrid()[tempRow][tempCol].setExploredState(true);
                this.getMapGrid()[tempRow][tempCol].setObstacle(false);
            }
        }
        System.out.println(result);
        return result;
    }

    public ArrayList<Cell> northObstacleCells(Robot robot, ArrayList<Integer> sensorData){
        
        ArrayList<Cell> obstacleCells = new ArrayList<Cell>();

        int row = robot.getPosRow();
        int col = robot.getPosCol();

        Integer rightBack = sensorData.get(0);
        Integer rightFront = sensorData.get(1);
        Integer left = sensorData.get(2);
        Integer frontRight = sensorData.get(3);
        Integer frontLeft = sensorData.get(4);
        Integer frontMiddle = sensorData.get(5);

        switch(frontRight){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+2, col+1));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+3, col+1));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(frontMiddle){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+2, col));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+3, col));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(frontLeft){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+2, col-1));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+3, col-1));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(rightBack){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-1, col+2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-1, col+3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(rightFront){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+1, col+2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+1, col+3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(left){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row, col-4));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row, col-5));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        
        return obstacleCells;
    }

    public ArrayList<Cell> northEmptyCells(Robot robot, ArrayList<Integer> sensorData){
        
        ArrayList<Cell> emptyCells = new ArrayList<Cell>();

        int row = robot.getPosRow();
        int col = robot.getPosCol();

        Integer rightBack = sensorData.get(0);
        Integer rightFront = sensorData.get(1);
        Integer left = sensorData.get(2);
        Integer frontRight = sensorData.get(3);
        Integer frontLeft = sensorData.get(4);
        Integer frontMiddle = sensorData.get(5);

        switch(frontRight){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+2, col+1));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+2, col+1));
                emptyCells.add(new Cell(row+3, col+1));
                break;
        }

        switch(frontMiddle){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+2, col));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+2, col));
                emptyCells.add(new Cell(row+3, col));
                break;
        }

        switch(frontLeft){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+2, col-1));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+2, col-1));
                emptyCells.add(new Cell(row+3, col-1));
                break;
        }

        switch(rightBack){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-1, col+2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-1, col+2));
                emptyCells.add(new Cell(row-1, col+3));
                break;
        }

        switch(rightFront){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+1, col+2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+1, col+2));
                emptyCells.add(new Cell(row+1, col+3));
                break;
        }

        switch(left){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row, col-4));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row, col-4));
                emptyCells.add(new Cell(row, col-5));
                break;
        }

        return emptyCells;
    }

    public ArrayList<Cell> southObstacleCells(Robot robot, ArrayList<Integer> sensorData){
        
        ArrayList<Cell> obstacleCells = new ArrayList<Cell>();

        int row = robot.getPosRow();
        int col = robot.getPosCol();

        Integer rightBack = sensorData.get(0);
        Integer rightFront = sensorData.get(1);
        Integer left = sensorData.get(2);
        Integer frontRight = sensorData.get(3);
        Integer frontLeft = sensorData.get(4);
        Integer frontMiddle = sensorData.get(5);

        switch(frontRight){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-2, col-1));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                 obstacleCells.add(new Cell(row-3, col-1));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(frontMiddle){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-2, col));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-3, col));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(frontLeft){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-2, col+1));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-3, col+1));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(rightBack){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+1, col-2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+1, col-3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(rightFront){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-1, col-2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-1, col-3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(left){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row, col+4));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row, col+5));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        
        return obstacleCells;
    }

    public ArrayList<Cell> southEmptyCells(Robot robot, ArrayList<Integer> sensorData){
        
        ArrayList<Cell> emptyCells = new ArrayList<Cell>();

        int row = robot.getPosRow();
        int col = robot.getPosCol();

        Integer rightBack = sensorData.get(0);
        Integer rightFront = sensorData.get(1);
        Integer left = sensorData.get(2);
        Integer frontRight = sensorData.get(3);
        Integer frontLeft = sensorData.get(4);
        Integer frontMiddle = sensorData.get(5);

        switch(frontRight){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-2, col-1));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-2, col-1));
                emptyCells.add(new Cell(row-3, col-1));
                break;
        }

        switch(frontMiddle){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-2, col));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-2, col));
                emptyCells.add(new Cell(row-3, col));
                break;
        }

        switch(frontLeft){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-2, col+1));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-2, col+1));
                emptyCells.add(new Cell(row-3, col+1));
                break;
        }

        switch(rightBack){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+1, col-2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+1, col-2));
                emptyCells.add(new Cell(row+1, col-3));
                break;
        }

        switch(rightFront){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-1, col-2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-1, col-2));
                emptyCells.add(new Cell(row-1, col-3));
                break;
        }

        switch(left){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row, col+4));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row, col+4));
                emptyCells.add(new Cell(row, col+5));
                break;
        }

        return emptyCells;
    }

    public ArrayList<Cell> eastObstacleCells(Robot robot, ArrayList<Integer> sensorData){
        
        ArrayList<Cell> obstacleCells = new ArrayList<Cell>();

        int row = robot.getPosRow();
        int col = robot.getPosCol();

        Integer rightBack = sensorData.get(0);
        Integer rightFront = sensorData.get(1);
        Integer left = sensorData.get(2);
        Integer frontRight = sensorData.get(3);
        Integer frontLeft = sensorData.get(4);
        Integer frontMiddle = sensorData.get(5);

        switch(frontRight){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-1, col+2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-1, col+3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(frontMiddle){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row, col+2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row, col+3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(frontLeft){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+1, col+2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+1, col+3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(rightBack){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-2, col-1));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-3, col-1));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(rightFront){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-2, col+1));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-3, col+1));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(left){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+4, col));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+5, col));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        
        return obstacleCells;
    }

    public ArrayList<Cell> eastEmptyCells(Robot robot, ArrayList<Integer> sensorData){
        
        ArrayList<Cell> emptyCells = new ArrayList<Cell>();

        int row = robot.getPosRow();
        int col = robot.getPosCol();

        Integer rightBack = sensorData.get(0);
        Integer rightFront = sensorData.get(1);
        Integer left = sensorData.get(2);
        Integer frontRight = sensorData.get(3);
        Integer frontLeft = sensorData.get(4);
        Integer frontMiddle = sensorData.get(5);

        switch(frontRight){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-1, col+2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-1, col+2));
                emptyCells.add(new Cell(row-1, col+3));
                break;
        }

        switch(frontMiddle){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row, col+2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row, col+2));
                emptyCells.add(new Cell(row, col+3));
                break;
        }

        switch(frontLeft){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+1, col+2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+1, col+2));
                emptyCells.add(new Cell(row+1, col+3));
                break;
        }

        switch(rightBack){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-2, col-1));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-2, col-1));
                emptyCells.add(new Cell(row-3, col-1));
                break;
        }

        switch(rightFront){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-2, col+1));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-2, col+1));
                emptyCells.add(new Cell(row-3, col+1));
                break;
        }

        switch(left){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+4, col));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+4, col));
                emptyCells.add(new Cell(row+5, col));
                break;
        }

        return emptyCells;
    }

    public ArrayList<Cell> westObstacleCells(Robot robot, ArrayList<Integer> sensorData){
        
        ArrayList<Cell> obstacleCells = new ArrayList<Cell>();

        int row = robot.getPosRow();
        int col = robot.getPosCol();

        Integer rightBack = sensorData.get(0);
        Integer rightFront = sensorData.get(1);
        Integer left = sensorData.get(2);
        Integer frontRight = sensorData.get(3);
        Integer frontLeft = sensorData.get(4);
        Integer frontMiddle = sensorData.get(5);

        switch(frontRight){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+1, col-2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+1, col-3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(frontMiddle){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row, col-2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row, col-3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(frontLeft){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-1, col-2));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-1, col-3));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(rightBack){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+2, col+1));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+3, col+1));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(rightFront){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row+2, col-1));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row+3, col-1));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        switch(left){
            case Constants.OBSTACLE_IMMEDIATE:
                obstacleCells.add(new Cell(row-4, col));
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                obstacleCells.add(new Cell(row-5, col));
                break;
            case Constants.NO_OBSTACLE:
                break;
        }

        
        return obstacleCells;
    }
    
    public ArrayList<Cell> westEmptyCells(Robot robot, ArrayList<Integer> sensorData){
        
        ArrayList<Cell> emptyCells = new ArrayList<Cell>();

        int row = robot.getPosRow();
        int col = robot.getPosCol();

        Integer rightBack = sensorData.get(0);
        Integer rightFront = sensorData.get(1);
        Integer left = sensorData.get(2);
        Integer frontRight = sensorData.get(3);
        Integer frontLeft = sensorData.get(4);
        Integer frontMiddle = sensorData.get(5);

        switch(frontRight){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+1, col-2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+1, col-2));
                emptyCells.add(new Cell(row+1, col-3));
                break;
        }

        switch(frontMiddle){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row, col-2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row, col-2));
                emptyCells.add(new Cell(row, col-3));
                break;
        }

        switch(frontLeft){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-1, col-2));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-1, col-2));
                emptyCells.add(new Cell(row-1, col-3));
                break;
        }

        switch(rightBack){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+2, col+1));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+2, col+1));
                emptyCells.add(new Cell(row+3, col+1));
                break;
        }

        switch(rightFront){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row+2, col-1));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row+2, col-1));
                emptyCells.add(new Cell(row+3, col-1));
                break;
        }

        switch(left){
            case Constants.OBSTACLE_IMMEDIATE:
                break;
            case Constants.OBSTACLE_ONE_BLOCK_AWAY:
                emptyCells.add(new Cell(row-4, col));
                break;
            case Constants.NO_OBSTACLE:
                emptyCells.add(new Cell(row-4, col));
                emptyCells.add(new Cell(row-5, col));
                break;
        }

        return emptyCells;
    }

    public boolean isCellValid(int row, int col){
        if(row >= Constants.MAX_ROW){
            return false;
        }
        if(col >= Constants.MAX_COL){
            return false;
        }
        if(row < 0){
            return false;
        }
        if(col < 0){
            return false;
        }
        return true;
    }

    public void setVirtualWall(Cell cell){
        for(int r : Constants.WITHIN_3BY3){
            for(int c : Constants.WITHIN_3BY3){
                Cell tempCell = new Cell(cell.getRowPos() + r, cell.getColPos() + c);
                if(tempCell.isCellValid()){
                    this.getMapGrid()[tempCell.getRowPos()][tempCell.getColPos()].setVirtualWall(true);
                }
            }
        }
    }
}
