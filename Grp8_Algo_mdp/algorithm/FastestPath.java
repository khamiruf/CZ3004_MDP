package algorithm;

import constant.Constants;
import constant.Constants.DIRECTION;
import entity.Cell;
import entity.Map;
import entity.Robot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * @author Nicholas Yeo Ming Jie
 * @author Neo Zhao Wei
 * @author David Loh Shun Hao
 * @version 1.0
 * @since 2020-10-27
 */
public class FastestPath {

    private Cell current;
    private ArrayList<Cell> toVisit;        // array of Cells to be visited (frontier for a* search)
    private ArrayList<Cell> visited;        // array of visited Cells
    private HashMap<Cell, Cell> parents;    // HashMap of Child --> Parent
    private int[][] gCosts;                 // 2d int array to store gCost of cells in exploredMap
    private Robot mockRobot;                // mockRobot in algorithm
    private Cell[] neighbors;               // array of neighbors of current Cell

    /**
     * Non default constructor
     *
     * @param robot       The robot object that contain information of robot
     * @param exploredMap The map object that have been explored by the robot
     */
    public FastestPath(Robot robot, Map exploredMap) {

        //setting current with the cell that robot is at.
        current = exploredMap.getMapGrid()[robot.getPosRow()][robot.getPosCol()];
        this.toVisit = new ArrayList<>();
        this.visited = new ArrayList<>();
        this.parents = new HashMap<>();
        this.gCosts = new int[Constants.MAX_ROW][Constants.MAX_COL];
        this.mockRobot = new Robot(robot.getPosRow(), robot.getPosCol(), robot.getCurrDir());
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
        // Adding current cell of robot from explored map into Arraylist<cell> toVisit
        toVisit.add(current);
    }

    /**
     * This method compute the g cost and h cost from the list of cell that need to be visited in order to reach
     * the destination.
     *
     * @param goalRow The row coordinate of the final destination to reach.
     * @param goalCol The column coordinate of the final destination to reach.
     * @return the selected cell with the lowest cost to reach the destination
     */
    private Cell minCostCell(int goalRow, int goalCol) {

        //get current size of toVisit
        int size = toVisit.size();
        int minCost = Constants.INFINITE_COST;
        Cell result = null;

        for (int i = size - 1; i >= 0; i--) {
            int cost = gCosts[toVisit.get(i).getRowPos()][toVisit.get(i).getColPos()]
                    + hCost(toVisit.get(i).getRowPos(), toVisit.get(i).getColPos(), goalRow, goalCol);

            if (cost < minCost) {
                minCost = cost;
                result = toVisit.get(i);
            }
        }

        return result;
    }

    /**
     * This method compute the actual cost of robot moving to 1 out of 4 of its neighbors (up, down, left or right)
     *
     * @param neighborRow The row coordinate of the neighbor cell
     * @param neighborCol The column coordinate of the neighbor cell
     * @return The cost of moving to the neighbor cell from the robot's position
     */
    public int gCost(int neighborRow, int neighborCol) {

        int posRow = mockRobot.getPosRow();
        int posCol = mockRobot.getPosCol();

        if (posRow == neighborRow && posCol == neighborCol) {
            return 0;
        }

        switch (mockRobot.getCurrDir()) {
            case NORTH:
                if (posRow < neighborRow && posCol == neighborCol) {
                    return Constants.FORWARD_COST;
                } else if (posRow > neighborRow && posCol == neighborCol) {
                    return Constants.BACKWARD_COST;
                } else if (posRow == neighborRow) {
                    return Constants.RIGHT_LEFT_COST;
                }
                break;
            case EAST:
                if (posRow == neighborRow && posCol < neighborCol) {
                    return Constants.FORWARD_COST;
                } else if (posRow == neighborRow && posCol > neighborCol) {
                    return Constants.BACKWARD_COST;
                } else if (posCol == neighborCol) {
                    return Constants.RIGHT_LEFT_COST;
                }
                break;
            case SOUTH:
                if (posRow > neighborRow && posCol == neighborCol) {
                    return Constants.FORWARD_COST;
                } else if (posRow < neighborRow && posCol == neighborCol) {
                    return Constants.BACKWARD_COST;
                } else if (posRow == neighborRow) {
                    return Constants.RIGHT_LEFT_COST;
                }
                break;
            case WEST:
                if (posRow == neighborRow && posCol > neighborCol) {
                    return Constants.FORWARD_COST;
                } else if (posRow == neighborRow && posCol < neighborCol) {
                    return Constants.BACKWARD_COST;
                } else if (posCol == neighborCol) {
                    return Constants.RIGHT_LEFT_COST;
                }
                break;
        }

        return Constants.INFINITE_COST;

    }

    /**
     * This method computes the heuristic cost between 2 specified cell.
     *
     * @param neighborRow The row coordinate of the first cell.
     * @param neighborCol The column coordinate of the first cell.
     * @param destRow     The row coordinate of the second cell.
     * @param destCol     The column coordinate of the second cell.
     * @return the heuristic cost of moving from the first to second cell.
     */

    public int hCost(int neighborRow, int neighborCol, int destRow, int destCol) {
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

    /**
     * This method will get direction that the robot should be facing using its new cell with respect to its parent cell
     *
     * @param current The new cell that the robot should be facing
     * @return the direction that the robot will be facing
     */
    public DIRECTION getCurrDir(Cell current) {

        Cell child = current;
        Cell parent = null;

        DIRECTION result = null;

        if (parents.containsKey(current)) {
            parent = parents.get(current);
        }
        ;

        if (child.getRowPos() < parent.getRowPos() && child.getColPos() == parent.getColPos()) {
            result = DIRECTION.SOUTH;
        } else if (child.getRowPos() > parent.getRowPos() && child.getColPos() == parent.getColPos()) {
            result = DIRECTION.NORTH;
        } else if (child.getRowPos() == parent.getRowPos() && child.getColPos() > parent.getColPos()) {
            result = DIRECTION.EAST;
        } else if (child.getRowPos() == parent.getRowPos() && child.getColPos() < parent.getColPos()) {
            result = DIRECTION.WEST;
        }

        return result;

    }

    /**
     * This method perform the A* search computation for the shortest path from the robot's current location
     * to the specified destination
     *
     * @param exploredMap The map object that was explored by the robot.
     * @param goalRow     The row coordinate of the destination
     * @param goalCol     The column coordinate of the destination
     * @return An arraylist of cells that signifies the path to the destination from the robot's location.
     * A null value will be return when the shortest path could not be determined.
     */
    public ArrayList<Cell> calculateFastestPath(Map exploredMap, int goalRow, int goalCol) {

        do {

            current = minCostCell(goalRow, goalCol);

            // move mockRobot to current cell by setting its row, column and direction
            if (parents.containsKey(current)) {
                mockRobot.setCurrDir(getCurrDir(current));
            }

            mockRobot.setPosRow(current.getRowPos());
            mockRobot.setPosCol(current.getColPos());

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            if (visited.contains(exploredMap.getMapGrid()[goalRow][goalCol])) {

                ArrayList<Cell> cellsInPath = new ArrayList<>();
                Cell targetCell = exploredMap.getMapGrid()[goalRow][goalCol];

                do {

                    cellsInPath.add(targetCell);
                    targetCell = parents.get(targetCell);

                } while (targetCell != null);

                Collections.reverse(cellsInPath);
                printCellArray(cellsInPath);
                return cellsInPath;
            }
            //adding valid neighbors
            //neighbors[north, south, east, west]
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.NORTH)) {
                neighbors[0] = exploredMap.getMapGrid()[mockRobot.getPosRow() + 1][mockRobot.getPosCol()];
            } else {
                neighbors[0] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.SOUTH)) {
                neighbors[1] = exploredMap.getMapGrid()[mockRobot.getPosRow() - 1][mockRobot.getPosCol()];
            } else {
                neighbors[1] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.EAST)) {
                neighbors[2] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() + 1];
            } else {
                neighbors[2] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.WEST)) {
                neighbors[3] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() - 1];
            } else {
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
                        gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()] = gCosts[current.getRowPos()][current.getColPos()]
                                + gCost(neighbors[i].getRowPos(), neighbors[i].getColPos());
                        toVisit.add(neighbors[i]);
                    } else {
                        int currentGCost = gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()];
                        int newGCost = gCosts[current.getRowPos()][current.getColPos()]
                                + gCost(neighbors[i].getRowPos(), neighbors[i].getColPos());
                        if (newGCost < currentGCost) {
                            gCosts[neighbors[i].getRowPos()][neighbors[i].getColPos()] = newGCost;
                            parents.put(neighbors[i], current);
                        }
                    }
                }
            }

        } while (!toVisit.isEmpty());

        System.out.println("Path not found!");
        return null;
    }

    /**
     * This method is print the list of cell on the system console and is used for debugging purposes.
     *
     * @param cellsInPath The arraylist of cells to be printed on console.
     */
    public void printCellArray(ArrayList<Cell> cellsInPath) {

        System.out.println("Cells in Fastest Path:");

        for (int i = 0; i < cellsInPath.size(); i++) {
            System.out.println("[" + cellsInPath.get(i).getRowPos() + "]" + "[" + cellsInPath.get(i).getColPos() + "]");
        }
        System.out.println();
    }


    /**
     * This method perform the A* search computation for the shortest path from start to goal through the specified WayPoint
     *
     * @param exploredMap The map object that was explored by the robot.
     * @return An arraylist of cells that signifies the path to the destination from the robot's location. <br>
     * A null value will be return when the shortest path could not be determined.
     */
    public ArrayList<Cell> findAllWPEndPaths(Map exploredMap) {

        boolean foundWayPoint = false;
        ArrayList<Cell> cellsInPath = new ArrayList<>();
        ArrayList<Cell> tempHolder;

        do {
            if (!foundWayPoint) {
                current = minCostCell(exploredMap.getWayPoint().getRowPos(), exploredMap.getWayPoint().getColPos());
            } else {
                current = minCostCell(exploredMap.getEndGoalPosition().getRowPos(), exploredMap.getEndGoalPosition().getColPos());
            }


            if (parents.containsKey(current)) {
                mockRobot.setCurrDir(getCurrDir(current));
            }

            mockRobot.setPosRow(current.getRowPos());
            mockRobot.setPosCol(current.getColPos());

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            if (!foundWayPoint && visited.contains(exploredMap.getMapGrid()[exploredMap.getWayPoint().getRowPos()][exploredMap.getWayPoint().getColPos()])) {
                tempHolder = new ArrayList<>();

                Cell targetCell = exploredMap.getMapGrid()[exploredMap.getWayPoint().getRowPos()][exploredMap.getWayPoint().getColPos()];
                do {
                    tempHolder.add(targetCell);
                    targetCell = parents.get(targetCell);

                } while (targetCell != null);

                Collections.reverse(tempHolder);
                cellsInPath.addAll(tempHolder);
                printCellArray(cellsInPath);

                foundWayPoint = true;
                this.toVisit = new ArrayList<Cell>();
                this.visited = new ArrayList<Cell>();
                this.parents = new HashMap<>();
                this.toVisit.add(exploredMap.getMapGrid()[exploredMap.getWayPoint().getRowPos()][exploredMap.getWayPoint().getColPos()]);

            } else if (foundWayPoint && visited.contains(exploredMap.getMapGrid()[exploredMap.getEndGoalPosition().getRowPos()][exploredMap.getEndGoalPosition().getColPos()])) {
                tempHolder = new ArrayList<>();
                Cell targetCell = exploredMap.getMapGrid()[exploredMap.getEndGoalPosition().getRowPos()][exploredMap.getEndGoalPosition().getColPos()];

                do {

                    tempHolder.add(targetCell);
                    targetCell = parents.get(targetCell);

                } while (targetCell != null);

                Collections.reverse(tempHolder);
                cellsInPath.addAll(tempHolder);
                printCellArray(cellsInPath);

                return cellsInPath;
            }

            //adding valid neighbors
            //neighbors[north, south, east, west]
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.NORTH)) {
                neighbors[0] = exploredMap.getMapGrid()[mockRobot.getPosRow() + 1][mockRobot.getPosCol()];
            } else {
                neighbors[0] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.SOUTH)) {
                neighbors[1] = exploredMap.getMapGrid()[mockRobot.getPosRow() - 1][mockRobot.getPosCol()];
            } else {
                neighbors[1] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.EAST)) {
                neighbors[2] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() + 1];
            } else {
                neighbors[2] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.WEST)) {
                neighbors[3] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() - 1];
            } else {
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

        } while (!toVisit.isEmpty());

        System.out.println("Path not found!");
        return null;


    }

    /**
     * This method perform the A* search computation for the shortest path from the robot's current location
     * to one of the surrounding cells that are 2 grids away from the specified destination
     *
     * @param exploredMap The map object that was explored by the robot.
     * @param destRow     The row coordinate of the destination
     * @param destCol     The column coordinate of the destination
     * @return An arraylist of cells that signifies the path to the destination from the robot's location.
     * A null value will be return when the shortest path could not be determined.
     */
    public ArrayList<Cell> calculateFastestPath2(Map exploredMap, int destRow, int destCol) {

        ArrayList<Cell> surroundCellList = can_reach(destRow, destCol);

        do {
            current = minCostCell(destRow, destCol);

            if (parents.containsKey(current)) {
                mockRobot.setCurrDir(getCurrDir(current));
            }

            mockRobot.setPosRow(current.getRowPos());
            mockRobot.setPosCol(current.getColPos());

            visited.add(current);       // add current to visited
            toVisit.remove(current);    // remove current from toVisit

            if (checkSurroundCells(surroundCellList, current)) {

                ArrayList<Cell> cellsInPath = new ArrayList<>();

                Cell targetCell = current;
                do {

                    cellsInPath.add(targetCell);
                    targetCell = parents.get(targetCell);

                } while (targetCell != null);

                Collections.reverse(cellsInPath);
                printCellArray(cellsInPath);
                return cellsInPath;

            }

            //adding valid neighbors
            //neighbors[north, south, east, west]
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.NORTH)) {
                neighbors[0] = exploredMap.getMapGrid()[mockRobot.getPosRow() + 1][mockRobot.getPosCol()];
            } else {
                neighbors[0] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.SOUTH)) {
                neighbors[1] = exploredMap.getMapGrid()[mockRobot.getPosRow() - 1][mockRobot.getPosCol()];
            } else {
                neighbors[1] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.EAST)) {
                neighbors[2] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() + 1];
            } else {
                neighbors[2] = null;
            }
            if (mockRobot.isDisplacementValid(exploredMap, DIRECTION.WEST)) {
                neighbors[3] = exploredMap.getMapGrid()[mockRobot.getPosRow()][mockRobot.getPosCol() - 1];
            } else {
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

        } while (!toVisit.isEmpty());

        System.out.println("Path not found!");
        return null;
    }

    /**
     * This method return an arraylist of surrounding cells that are 2 grid away from the input cell.
     *
     * @param goalRow The row coordinate of input cell
     * @param goalCol The column coordinate of the input cell
     * @return Arraylist of all surrounding cells that are 2 grid away from the input cell
     */
    private ArrayList<Cell> can_reach(int goalRow, int goalCol) {

        ArrayList<Cell> surroundCellList = new ArrayList<Cell>();

        //surroundCellList.add(new Cell(goalRow,goalCol)); 		//add end goal into list
        //surroundCellList.add(new Cell(goalRow-2,goalCol-1));    //
        surroundCellList.add(new Cell(goalRow - 2, goalCol));
        //surroundCellList.add(new Cell(goalRow-2,goalCol+1));    //

        //surroundCellList.add(new Cell(goalRow-1,goalCol+2));    //
        surroundCellList.add(new Cell(goalRow, goalCol + 2));
        //surroundCellList.add(new Cell(goalRow+1,goalCol+2));    //

        //surroundCellList.add(new Cell(goalRow+2,goalCol-1));    //
        surroundCellList.add(new Cell(goalRow + 2, goalCol));
        //surroundCellList.add(new Cell(goalRow+2,goalCol+1));    //

        //surroundCellList.add(new Cell(goalRow-1,goalCol-2));    //
        surroundCellList.add(new Cell(goalRow, goalCol - 2));
        //surroundCellList.add(new Cell(goalRow+1,goalCol-2));    //

        return surroundCellList;
    }

    /**
     * This method check if the input cell exist in the arraylist of cell.
     *
     * @param clist The arraylist of cell
     * @param ac    The cell that needed to be check if it exist in the list
     * @return True when the cell exist in arraylist , else return false.
     */
    private boolean checkSurroundCells(ArrayList<Cell> clist, Cell ac) {

        for (int i = 0; i < clist.size(); i++) {
            if (clist.get(i).getRowPos() == ac.getRowPos() && clist.get(i).getColPos() == ac.getColPos()) {
                return true;
            }
        }
        return false;
    }


}
