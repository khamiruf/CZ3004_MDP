package entity;

import constant.Constants;

/**
 * Cell represents the 10cm ✕ 10cm on the arena.
 */
public class Cell {

	private int rowPos;						//specifies row of map
	private int colPos;						//specifies column of map
	private boolean exploredState;			// true for explored, false for unexplored
	private boolean isObstacle;				//true for wall/obstacle,false for path
	private boolean isVirtualWall;
	
	public Cell(int rowValue, int colValue) {
		this.rowPos = rowValue;
		this.colPos = colValue;
	}

	public Cell(int rowValue, int colValue, boolean explState) {
		this.rowPos = rowValue;
		this.colPos = colValue;
		this.exploredState = explState;
	}
	
	public void setRowPos(int rowValue) {
		this.rowPos = rowValue;
	}
	
	public void setColPos(int colValue) {
		this.colPos = colValue;
	}
		
	public void setExploredState(boolean explState) {
		
		this.exploredState = explState;
	}
	
	public int getRowPos() {
		return this.rowPos;
	}
	
	public int getColPos() {
		return this.colPos;
	}
	
	public boolean getExploredState() {
		return this.exploredState;
	}
	
	public void setObstacle(boolean state) {
		this.isObstacle = state;
	}
	
	
	public boolean isObstacle() {
		return this.isObstacle;
	}
	public boolean isVirtualWall() {
	    return isVirtualWall;
	}

	public void setVirtualWall(boolean isVirtualWall) {
	     this.isVirtualWall = isVirtualWall;
	}
	
	//Check if this cell's row and col position exist outside of arena 
    public boolean isCellValid(){
        if(this.rowPos >= Constants.MAX_ROW){
            return false;
        }
        if(this.colPos >= Constants.MAX_COL){
            return false;
        }
        if(this.rowPos < 0){
            return false;
        }
        if(this.colPos < 0){
            return false;
        }
      
        return true;
    }

}
