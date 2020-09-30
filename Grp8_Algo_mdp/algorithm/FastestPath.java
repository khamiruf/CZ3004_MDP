package algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import constant.Constants;
import constant.Constants.DIRECTION;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;
import entity.Robot;

public class FastestPath {
	
	 private Cell current;
	    private ArrayList<Cell> toVisit;        // array of Cells to be visited (frontier for a* search)
	    private ArrayList<Cell> visited;        // array of visited Cells 
	    private HashMap<Cell, Cell> parents;    // HashMap of Child --> Parent
	    private int[][] gCosts;                 // 2d int array to store gCost of cells in exploredMap
	    private Robot mockRobot;                // mockRobot in alogrithm
	    private Cell[] neighbors;               // array of neighbors of current Cell
	   
	    
	    public FastestPath(Robot robot, Map exploredMap){
	    	
	    	//setting current with the cell that robot is at.
	        current = exploredMap.getMapGrid()[robot.getPosRow()][robot.getPosCol()];
	        this.toVisit = new ArrayList<>();
	        this.visited = new ArrayList<>();
	        this.parents = new HashMap<>();
	        this.gCosts = new int[Constants.MAX_ROW][Constants.MAX_COL];
	        this.mockRobot = new Robot(robot.getPosRow(), robot.getPosCol(), robot.getCurrDir(), false);
	        this.neighbors = new Cell[4];
	       
	        
	        // Initialise gCosts array
	        for (int r = 0; r < Constants.MAX_ROW; r++) {
	            for (int c = 0; c < Constants.MAX_COL; c++) {
	                Cell cell = exploredMap.getMapGrid()[r][c];
	                //setting all unexplored, walls & virtual wall with infinite cost
	                if (!cell.getExploredState() || cell.isVirtualWall() || cell.isObstacle()) {
	                    gCosts[r][c] = Constants.INFINITE_COST;
	                } else {
	                	//setting all explored paths with cost 0
	                    gCosts[r][c] = 0;
	                }
	            }
	        }
	        //adding current cell of robot from explored map into Arraylist<cell> toVisit
	        toVisit.add(current);
	    }

	    //return minimum cost cell from toVisit
	    private Cell minCostCell(int goalRow, int goalCol) {
	    	
	    	//get current size of toVisit 
	        int size = toVisit.size();
	        int minCost = Constants.INFINITE_COST;
	        Cell result = null;
	        
	        for (int i = size - 1; i >= 0; i--) {
	            int cost = gCosts[toVisit.get(i).getRowPos()][toVisit.get(i).getColPos()] + hCost(toVisit.get(i).getRowPos(), toVisit.get(i).getColPos(), goalRow, goalCol);
	            if (cost < minCost) {
	                minCost = cost;
	                result = toVisit.get(i);
	            }
	        }

	        return result;
	    }

	    //calculates actual cost of robot moving to 1 out of 4 of its neighbors (up, down, left or right)
	    public int gCost(int neighborRow, int neighborCol){

	        int posRow = mockRobot.getPosRow();
	        int posCol = mockRobot.getPosCol();

	        if(posRow == neighborRow && posCol == neighborCol){return 0;}

	        switch(mockRobot.getCurrDir()){
	            case NORTH:
	                if(posRow < neighborRow && posCol == neighborCol){ return Constants.FORWARD_COST;}
	                else if(posRow > neighborRow && posCol == neighborCol){ return Constants.BACKWARD_COST;}
	                else if(posRow == neighborRow){return Constants.RIGHT_LEFT_COST;}
	                break;
	            case EAST:
	                if(posRow == neighborRow && posCol < neighborCol){ return Constants.FORWARD_COST;}
	                else if(posRow == neighborRow && posCol > neighborCol){ return Constants.BACKWARD_COST;}
	                else if(posCol == neighborCol){return Constants.RIGHT_LEFT_COST;}
					break;
	            case SOUTH:
	                if(posRow > neighborRow && posCol == neighborCol){ return Constants.FORWARD_COST;}
	                else if(posRow < neighborRow && posCol == neighborCol){ return Constants.BACKWARD_COST;}
	                else if(posRow == neighborRow){return Constants.RIGHT_LEFT_COST;}
					break;
	            case WEST:
	                if(posRow == neighborRow && posCol > neighborCol){ return Constants.FORWARD_COST;}
	                else if(posRow == neighborRow && posCol < neighborCol){ return Constants.BACKWARD_COST;}
	                else if(posCol == neighborCol){return Constants.RIGHT_LEFT_COST;}
	                break;
	        }

	        return Constants.INFINITE_COST;

	    }

	    //calculate heuristics from neighbour cell to goal cell
	    public int hCost(int neighborRow, int neighborCol, int destRow, int destCol){
	        // movementCost is the total number of cells away vertically and horizontally multiply by movement cost
	        int movementCost = (Math.abs(destCol - neighborCol) + Math.abs(destRow - neighborRow)) * Constants.MOVE_COST;

	        if (movementCost == 0) return 0;

	        // turnCost is the number of turns needed, assuming one turn is needed if cell is either not on the same row or not on the same column as goal cell
	        int turnCost = 0;
	        if (destCol - neighborCol != 0 || destRow - neighborRow != 0) {
	            turnCost = Constants.TURN_COST;
	        }

	        return movementCost + turnCost;
	    }

	    //get direction of robot using its current position with respect to its parent cell
	    public Constants.DIRECTION getCurrDir(Cell current){

	        Cell child = current;
	        Cell parent = null;

	        Constants.DIRECTION result = null;

	        if(parents.containsKey(current)){parent = parents.get(current);};

	        if(child.getRowPos() < parent.getRowPos() && child.getColPos() == parent.getColPos()){
	            result = DIRECTION.SOUTH;
	        }
	        else if(child.getRowPos() > parent.getRowPos() && child.getColPos() == parent.getColPos()){
	            result = DIRECTION.NORTH;
	        }
	        else if(child.getRowPos() == parent.getRowPos() && child.getColPos() > parent.getColPos()){
	            result = DIRECTION.EAST;
	        }
	        else if(child.getRowPos() == parent.getRowPos() && child.getColPos() < parent.getColPos()){
	            result = DIRECTION.WEST;
	        }

	        return result;

	    }


	    public ArrayList<Cell> calculateFastestPath(Map exploredMap, int goalRow, int goalCol){

	        do{
	            
	            current = minCostCell(goalRow, goalCol); 

	            // move mockRobot to current cell by setting its row, column and direction
	            if (parents.containsKey(current)) {
	                mockRobot.setCurrDir(getCurrDir(current));
	            }

	            mockRobot.setPosRow(current.getRowPos());
	            mockRobot.setPosCol(current.getColPos());
	            //System.out.println("MOCKLOC: " + mockRobot.getPosRow() + ":" + mockRobot.getPosCol());
	            visited.add(current);       // add current to visited
	            toVisit.remove(current);    // remove current from toVisit        

	            if (visited.contains(exploredMap.getMapGrid()[goalRow][goalCol])) {

	                ArrayList<Cell> cellsInPath = new ArrayList<>();
	                Cell targetCell = exploredMap.getMapGrid()[goalRow][goalCol];
	                
	                do{
	                    
	                    cellsInPath.add(targetCell);
	                    targetCell = parents.get(targetCell);

	                }while(targetCell != null);
	                
	                Collections.reverse(cellsInPath);
	                printCellArray(cellsInPath);
	                return cellsInPath;
	                
	            }

	            //adding valid neighbors 
	            //neighbors[north, south, east, west]
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.NORTH)){
	                neighbors[0] = exploredMap.getMapGrid()[mockRobot.getPosRow() + 1][mockRobot.getPosCol()];
	            }
	            else{
	                neighbors[0] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.SOUTH)){
	                neighbors[1] = exploredMap.getMapGrid()[mockRobot.getPosRow() - 1][mockRobot.getPosCol()];
	            }
	            else{
	                neighbors[1] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.EAST)){
	                neighbors[2] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() + 1];
	            }
	            else{
	                neighbors[2] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.WEST)){
	                neighbors[3] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() - 1];
	            }
	            else{
	                neighbors[3] = null;
	            }

	            // Iterate through neighbors and update the g(n) values of each.
	            for (int i = 0; i < 4; i++) {
	                if (neighbors[i] != null) {
	                    if (visited.contains(neighbors[i])) { //skip if visited
	                        continue;
	                    }
	                    if (!(toVisit.contains(neighbors[i]))) {
	                        parents.put(neighbors[i], current); //neighbor added as child from current position as parent cell
	                        gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()] = gCosts[current.getRowPos()][current.getColPos()] + gCost(neighbors[i].getRowPos(), neighbors[i].getColPos());
	                        toVisit.add(neighbors[i]);
	                    } else {
	                        int currentGCost = gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()];
	                        int newGCost = gCosts[current.getRowPos()][current.getColPos()] + gCost(neighbors[i].getRowPos(), neighbors[i].getColPos());
	                        if (newGCost < currentGCost) {
	                            gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()] = newGCost;
	                            parents.put(neighbors[i], current);
	                        }
	                    }
	                }
	            }
	        
	        }while(!toVisit.isEmpty());

	        System.out.println("Path not found!");
	        return null;
	    }

	    public void printCellArray(ArrayList<Cell> cellsInPath){

	        System.out.println("Cells in Fastest Path:");

	        for(int i = 0; i < cellsInPath.size(); i++){
	            System.out.println("[" + cellsInPath.get(i).getRowPos() + "]" + "[" + cellsInPath.get(i).getColPos() + "]");
	        }
	        System.out.println();
	    }
/*
	    public void convertCellsToMovements(Robot robot, ArrayList<Cell> cellsInPath){

	        int currRow = robot.getPosRow();
	        int currCol = robot.getPosCol();

	        ArrayList<MOVEMENT> fastestPathMovements = new ArrayList<MOVEMENT>();

	        for(int i=0; i < cellsInPath.size(); i++){
	            int destRow = cellsInPath.get(i).getRowPos();
	            int destCol = cellsInPath.get(i).getColPos();
	            switch(robot.getCurrDir()){
	                case NORTH:
	                    if(currCol == destCol){
	                        if(currRow < destRow){fastestPathMovements.add(MOVEMENT.FORWARD); robot.move(MOVEMENT.FORWARD);}
	                        else if(currRow > destRow){fastestPathMovements.add(MOVEMENT.BACKWARD); robot.turn(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); robot.move(MOVEMENT.FORWARD);}
	                    }
	                    else if(currRow == destRow){
	                        if(currCol < destCol){fastestPathMovements.add(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); robot.move(MOVEMENT.FORWARD);}
	                        else if(currCol > destCol){fastestPathMovements.add(MOVEMENT.LEFT); robot.turn(MOVEMENT.LEFT); robot.move(MOVEMENT.FORWARD);}
	                    }
	                    break;
	                case SOUTH:
	                    if(currCol == destCol){
	                        if(currRow < destRow){fastestPathMovements.add(MOVEMENT.BACKWARD); robot.turn(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); robot.move(MOVEMENT.FORWARD);}
	                        else if(currRow > destRow){fastestPathMovements.add(MOVEMENT.FORWARD); robot.move(MOVEMENT.FORWARD);}
	                    }
	                    else if(currRow == destRow){
	                        if(currCol < destCol){fastestPathMovements.add(MOVEMENT.LEFT); robot.turn(MOVEMENT.LEFT); robot.move(MOVEMENT.FORWARD);}
	                        else if(currCol > destCol){fastestPathMovements.add(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); robot.move(MOVEMENT.FORWARD);}
	                    }
	                    break;
	                case EAST:
	                    if(currCol == destCol){
	                        if(currRow < destRow){fastestPathMovements.add(MOVEMENT.LEFT); robot.turn(MOVEMENT.LEFT); robot.move(MOVEMENT.FORWARD);}
	                        else if(currRow > destRow){fastestPathMovements.add(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); robot.move(MOVEMENT.FORWARD);}
	                    }
	                    else if(currRow == destRow){
	                        if(currCol < destCol){fastestPathMovements.add(MOVEMENT.FORWARD); robot.move(MOVEMENT.FORWARD);}
	                        else if(currCol > destCol){fastestPathMovements.add(MOVEMENT.BACKWARD); robot.turn(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); robot.move(MOVEMENT.FORWARD);}
	                    }
	                    break;
	                case WEST:
	                if(currCol == destCol){
	                    if(currRow < destRow){fastestPathMovements.add(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); robot.move(MOVEMENT.FORWARD);}
	                    else if(currRow > destRow){fastestPathMovements.add(MOVEMENT.LEFT); robot.turn(MOVEMENT.LEFT); robot.move(MOVEMENT.FORWARD);}
	                }
	                else if(currRow == destRow){
	                    if(currCol < destCol){fastestPathMovements.add(MOVEMENT.BACKWARD); robot.turn(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); robot.move(MOVEMENT.FORWARD);}
	                    else if(currCol > destCol){fastestPathMovements.add(MOVEMENT.FORWARD); robot.move(MOVEMENT.FORWARD);}
	                }
	                break;
	            }
	            
	            currRow = robot.getPosRow();
	            currCol = robot.getPosCol();
	        }

	        printMovementArray(fastestPathMovements);

	    }

	    public void printMovementArray(ArrayList<MOVEMENT> fastestPathMovements){

	        System.out.println("Movements in Fastest Path:");
	        int i = 0;
	        int counter = 1;
	        //while(i<fastestPathMovements.size()){System.out.println(fastestPathMovements.get(i)); i++;}
	        
	        while(true){
	            System.out.print(fastestPathMovements.get(i));
	            System.out.print(": ");
	            for(int j = i+1; j< fastestPathMovements.size(); j++){
	                if(fastestPathMovements.get(i) == fastestPathMovements.get(j)){
	                    counter++;
	                }
	                else{
	                    System.out.print(counter);
	                    System.out.print(" | ");
	                    counter = 1;
	                    i=j;
	                    break;
	                }
	            }
	            if(i==fastestPathMovements.size() - 1){
	                System.out.println();
	                System.out.println();
	                break;
	            }
	        }

	    }
	
*/	    
	//--------------------------------------------------ZW-----------------------------------------
	    
	    public ArrayList<Cell> testone(Map exploredMap){
	    		
	    	boolean foundWayPoint = false;
	    	 ArrayList<Cell> cellsInPath = new ArrayList<>();
	        do{
	            if(!foundWayPoint) {
	            	  current = minCostCell(exploredMap.getWayPoint().getRowPos(), exploredMap.getWayPoint().getColPos()); 
	            }
	            else {
	            	 current = minCostCell(exploredMap.getEndGoalPosition().getRowPos(), exploredMap.getEndGoalPosition().getColPos()); 
	            }

	            // move mockRobot to current cell by setting its row, column and direction
	            if (parents.containsKey(current)) {
	                mockRobot.setCurrDir(getCurrDir(current));
	            }

	            mockRobot.setPosRow(current.getRowPos());
	            mockRobot.setPosCol(current.getColPos());
	            System.out.println("MOCKLOC: " + mockRobot.getPosRow() + ":" + mockRobot.getPosCol());
	            visited.add(current);       // add current to visited
	            toVisit.remove(current);    // remove current from toVisit        

	            if (!foundWayPoint && visited.contains(exploredMap.getMapGrid()[exploredMap.getWayPoint().getRowPos()][exploredMap.getWayPoint().getColPos()])) {
	            	 ArrayList<Cell> tempHolder = new ArrayList<>();
	                Cell targetCell = exploredMap.getWayPoint();
	                
	                do{
	                    
	                	tempHolder.add(targetCell);
	                    targetCell = parents.get(targetCell);

	                }while(targetCell != null);
	                
	                Collections.reverse(tempHolder);
	                printCellArray(tempHolder);
	                cellsInPath.addAll(tempHolder);
	                printCellArray(cellsInPath);
	                //return cellsInPath;
	                foundWayPoint = true;
	                System.out.println("END " + tempHolder.size() + "-" + cellsInPath.size());
	            }
	            else if(foundWayPoint && visited.contains(exploredMap.getMapGrid()[18][13])) {
	            	 ArrayList<Cell> tempHolder = new ArrayList<>();
		                Cell targetCell = exploredMap.getEndGoalPosition();
		                
		                do{
		                    
		                	tempHolder.add(targetCell);
		                    targetCell = parents.get(targetCell);

		                }while(targetCell != null);
		                
		                Collections.reverse(tempHolder);
		                cellsInPath.addAll(tempHolder);
		                printCellArray(cellsInPath);
		            System.out.println("END2 " + tempHolder.size() + "-" + cellsInPath.size());
	            	return cellsInPath;
	            }

	            //adding valid neighbors 
	            //neighbors[north, south, east, west]
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.NORTH)){
	                neighbors[0] = exploredMap.getMapGrid()[mockRobot.getPosRow() + 1][mockRobot.getPosCol()];
	            }
	            else{
	                neighbors[0] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.SOUTH)){
	                neighbors[1] = exploredMap.getMapGrid()[mockRobot.getPosRow() - 1][mockRobot.getPosCol()];
	            }
	            else{
	                neighbors[1] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.EAST)){
	                neighbors[2] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() + 1];
	            }
	            else{
	                neighbors[2] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.WEST)){
	                neighbors[3] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() - 1];
	            }
	            else{
	                neighbors[3] = null;
	            }

	            // Iterate through neighbors and update the g(n) values of each.
	            for (int i = 0; i < 4; i++) {
	                if (neighbors[i] != null) {
	                    if (visited.contains(neighbors[i])) { //skip if visited
	                        continue;
	                    }
	                    if (!(toVisit.contains(neighbors[i]))) {
	                        parents.put(neighbors[i], current); //neighbor added as child from current position as parent cell
	                        gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()] = gCosts[current.getRowPos()][current.getColPos()] + gCost(neighbors[i].getRowPos(), neighbors[i].getColPos());
	                        toVisit.add(neighbors[i]);
	                    } else {
	                        int currentGCost = gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()];
	                        int newGCost = gCosts[current.getRowPos()][current.getColPos()] + gCost(neighbors[i].getRowPos(), neighbors[i].getColPos());
	                        if (newGCost < currentGCost) {
	                            gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()] = newGCost;
	                            parents.put(neighbors[i], current);
	                        }
	                    }
	                }
	            }
	        
	        }while(!toVisit.isEmpty());

	        System.out.println("Path not found!");
	        return null;
	    
	    	
	    }
	    
	    public ArrayList<Cell> calculateAllFastestPath(Map mapObj){
	    	ArrayList<Cell> cellsInPath = new ArrayList<>();
	    	System.out.println(mapObj.getWayPoint().getRowPos()+"_"+mapObj.getWayPoint().getColPos());
	    	System.out.println(mapObj.getEndGoalPosition().getRowPos()+"_"+mapObj.getEndGoalPosition().getColPos());
	    	cellsInPath.addAll(calculateFastestPath(mapObj,mapObj.getWayPoint().getRowPos(),mapObj.getWayPoint().getColPos()));
	    	cellsInPath.addAll(calculateFastestPath(mapObj,mapObj.getEndGoalPosition().getRowPos(),mapObj.getEndGoalPosition().getColPos()));
	    	
	    	return cellsInPath;
	    	
	    }
	    
	    public ArrayList<Cell> calculateFastestPath2(Map exploredMap, int destRow, int destCol){

	    	ArrayList<Cell> surroundCellList = can_reach(destRow, destCol);
	    	this.toVisit = new ArrayList<Cell>();
	        this.visited = new ArrayList<Cell>();
	        this.toVisit.add(new Cell(mockRobot.getPosRow(),mockRobot.getPosCol()));
	    	//System.out.println()
	        do{
	            
	            current = minCostCell(destRow, destCol); 

	            // move mockRobot to current cell by setting its row, column and direction
	            if (parents.containsKey(current)) {
	                mockRobot.setCurrDir(getCurrDir(current));
	            }

	            mockRobot.setPosRow(current.getRowPos());
	            mockRobot.setPosCol(current.getColPos());

	            visited.add(current);       // add current to visited
	            toVisit.remove(current);    // remove current from toVisit        
	            
	          
	            // checkV(surroundCellList,current);
	            if (checkV(surroundCellList,current)) {

	            	
	                ArrayList<Cell> cellsInPath = new ArrayList<>();
	                //Cell targetCell = exploredMap.getMapGrid()[goalRow][goalCol];
	                Cell targetCell = current;
	                do{
	                    
	                    cellsInPath.add(targetCell);
	                    targetCell = parents.get(targetCell);

	                }while(targetCell != null);
	                
	                Collections.reverse(cellsInPath);
	                printCellArray(cellsInPath);
	                return cellsInPath;
	                
	            }

	            //adding valid neighbors 
	            //neighbors[north, south, east, west]
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.NORTH)){
	                neighbors[0] = exploredMap.getMapGrid()[mockRobot.getPosRow() + 1][mockRobot.getPosCol()];
	            }
	            else{
	                neighbors[0] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.SOUTH)){
	                neighbors[1] = exploredMap.getMapGrid()[mockRobot.getPosRow() - 1][mockRobot.getPosCol()];
	            }
	            else{
	                neighbors[1] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.EAST)){
	                neighbors[2] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() + 1];
	            }
	            else{
	                neighbors[2] = null;
	            }
	            if(mockRobot.isDisplacementValid(exploredMap, DIRECTION.WEST)){
	                neighbors[3] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() - 1];
	            }
	            else{
	                neighbors[3] = null;
	            }

	            // Iterate through neighbors and update the g(n) values of each.
	            for (int i = 0; i < 4; i++) {
	                if (neighbors[i] != null) {
	                    if (visited.contains(neighbors[i])) { //skip if visited
	                        continue;
	                    }
	                    if (!(toVisit.contains(neighbors[i]))) {
	                        parents.put(neighbors[i], current); //neighbor added as child from current position as parent cell
	                        gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()] = gCosts[current.getRowPos()][current.getColPos()] + gCost(neighbors[i].getRowPos(), neighbors[i].getColPos());
	                        toVisit.add(neighbors[i]);
	                    } else {
	                        int currentGCost = gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()];
	                        int newGCost = gCosts[current.getRowPos()][current.getColPos()] + gCost(neighbors[i].getRowPos(), neighbors[i].getColPos());
	                        if (newGCost < currentGCost) {
	                            gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()] = newGCost;
	                            parents.put(neighbors[i], current);
	                        }
	                    }
	                }
	            }
	        
	        }while(!toVisit.isEmpty());

	        System.out.println("Path not found!");
	        return null;
	    }
	    
	    private ArrayList<Cell> can_reach(int goalRow, int goalCol) {
	        //int x = end[0];
	        // int y = end[1];
	        ArrayList<Cell> surroundCellList = new ArrayList<Cell>();
	        //int[][] pos;

	        /*
	        if (first) {
	            pos = new int[][] {{x-1, y-2}, {x, y-2}, {x+1, y-2}, {x+2, y-1}, {x+2, y}, {x+2, y+1},
	                                 {x+1, y+2}, {x, y+2}, {x-1, y+2}, {x-2, y+1}, {x-2, y}, {x-2, y-1}};
	        } else {
	            pos = new int[][] {{x-1, y-3}, {x, y-3}, {x+1, y-3}, {x+3, y-1}, {x+3, y}, {x+3, y+1},
	                               {x+1, y+3}, {x, y+3}, {x-1, y+3}, {x-3, y+1}, {x-3, y}, {x-3, y-1}};
	        }
	        */
	        surroundCellList.add(new Cell(goalRow,goalCol)); 		//add end goal into list
	        surroundCellList.add(new Cell(goalRow-2,goalCol-1));    //
	        surroundCellList.add(new Cell(goalRow-2,goalCol));
	        surroundCellList.add(new Cell(goalRow-2,goalCol+1));    //
	        
	        surroundCellList.add(new Cell(goalRow-1,goalCol+2));    //
	        surroundCellList.add(new Cell(goalRow,goalCol+2));
	        surroundCellList.add(new Cell(goalRow+1,goalCol+2));    //
	        
	        surroundCellList.add(new Cell(goalRow+2,goalCol-1));    //
	        surroundCellList.add(new Cell(goalRow+2,goalCol));
	        surroundCellList.add(new Cell(goalRow+2,goalCol+1));    //
	        
	        surroundCellList.add(new Cell(goalRow-1,goalCol-2));    //
	        surroundCellList.add(new Cell(goalRow,goalCol-2));
	        surroundCellList.add(new Cell(goalRow+1,goalCol-2));    //
	        
	      //  for(int i=0;i<surroundCellList.size();i++) {
	      //  	System.out.println(surroundCellList.get(i).getRowPos() + "_" + surroundCellList.get(i).getColPos() +"_"+"_"+surroundCellList.get(i).getExploredState());
	       // }
	        
	        return surroundCellList;
	    }
	    
	    private void printV(ArrayList<Cell> clist) {
	    	
	    	for(int i=0;i<clist.size();i++) {
	    		System.out.println(clist.get(i).getRowPos() + "_" + clist.get(i).getColPos() +"_"+clist.get(i).getExploredState()
	    				);
	  	      	
	    	}
	    	   
	    }
       private boolean checkV(ArrayList<Cell> clist, Cell ac) {
    		
    	   for(int i=0;i<clist.size();i++) {
	    		if(clist.get(i).getRowPos()==ac.getRowPos() && clist.get(i).getColPos()==ac.getColPos()) {
	    			return true;
	    		}	    		
	    	}
	    	return false;
	    }
       
       public void setMockRobot(Robot curBot) {
    	   this.mockRobot = curBot;
       }
}
