package entity;

import constant.Constants;

/**
 * @author Nicholas Yeo Ming Jie
 * @author Neo Zhao Wei
 * @author David Loh Shun Hao
 * @version 1.0
 * @since 2020-10-27
 */
public class Cell {

    private int rowPos;                         // Specifies row of map
    private int colPos;                         // Specifies column of map
    private boolean exploredState;              // True for explored, false for unexplored
    private boolean isObstacle;                 // True for wall/obstacle,false for path
    private boolean isVirtualWall;
    private String sensor;

    /**
     * Non-default Constructor
     *
     * @param rowValue row value of cell
     * @param colValue column value of cell
     */
    public Cell(int rowValue, int colValue) {
        this.rowPos = rowValue;
        this.colPos = colValue;
        this.sensor = "none";
    }

    /**
     * Non-default Constructor
     *
     * @param rowValue  row value of cell
     * @param colValue  column value of cell
     * @param explState explored status of cell
     */
    public Cell(int rowValue, int colValue, boolean explState) {
        this.rowPos = rowValue;
        this.colPos = colValue;
        this.exploredState = explState;
        this.sensor = "none";
    }

    /**
     * Non-default Constructor
     *
     * @param rowValue    row value of cell
     * @param colValue    column value of cell
     * @param sensorValue Sensor value that read the cell
     */
    public Cell(int rowValue, int colValue, String sensorValue) {
        this.rowPos = rowValue;
        this.colPos = colValue;
        this.sensor = sensorValue;
    }

    /**
     * @param sensorValue The new value specify which sensor the cell is read by
     */
    public void setSensor(String sensorValue) {
        this.sensor = sensorValue;
    }

    /**
     * @return sensor which read the cell
     */
    public String getSensor() {
        return this.sensor;
    }

    /**
     * @param rowValue The new value to set the row value of cell
     */
    public void setRowPos(int rowValue) {
        this.rowPos = rowValue;
    }

    /**
     * @param colValue The new value to set the column value of cell
     */
    public void setColPos(int colValue) {
        this.colPos = colValue;
    }

    /**
     * @param explState The new value to specify explored status of cell
     */
    public void setExploredState(boolean explState) {

        this.exploredState = explState;
    }

    /**
     * @return row value of cell
     */
    public int getRowPos() {
        return this.rowPos;
    }

    /**
     * @return column value of cell
     */
    public int getColPos() {
        return this.colPos;
    }

    /**
     * @return explored status of cell
     */
    public boolean getExploredState() {
        return this.exploredState;
    }

    /**
     * @param state The new value to specify if cell is obstacle
     */
    public void setObstacle(boolean state) {
        this.isObstacle = state;
    }

    /**
     * @return the obstacle status of cell
     */
    public boolean isObstacle() {
        return this.isObstacle;
    }

    /**
     * @return the virtual wall status of cell
     */
    public boolean isVirtualWall() {
        return isVirtualWall;
    }

    /**
     * @param isVirtualWall The new value to specify if cell is a virtual wall
     */
    public void setVirtualWall(boolean isVirtualWall) {
        this.isVirtualWall = isVirtualWall;
    }

    /**
     * @return true if cell is within the width and length of arena
     */
    public boolean isCellValid() {
        if (this.rowPos >= Constants.MAX_ROW) {
            return false;
        }
        if (this.colPos >= Constants.MAX_COL) {
            return false;
        }
        if (this.rowPos < 0) {
            return false;
        }
        if (this.colPos < 0) {
            return false;
        }

        return true;
    }

}
